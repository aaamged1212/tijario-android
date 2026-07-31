package app.tijario.features.backup

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.config.Supabase
import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.BackupSettingsEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupRuntime
import app.tijario.features.backup.drive.BackupDriveContainer
import app.tijario.features.backup.drive.DriveAuthorizationOutcome
import app.tijario.features.backup.drive.DriveConnectionState
import app.tijario.features.backup.drive.DriveBackupFile
import app.tijario.features.backup.drive.DriveBackupRepository
import app.tijario.features.backup.drive.DriveFolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class BackupUiState(
    val isBusy: Boolean = false,
    val latestBackup: BackupRecordEntity? = null,
    val settings: BackupSettingsEntity? = null,
    val history: List<BackupRecordEntity> = emptyList(),
    val backupPlanPolicy: BackupPlanPolicy = BackupPlanPolicy.ManualOnly,
    val driveConnectionState: DriveConnectionState = DriveConnectionState.NotConfigured,
    val driveBackups: List<DriveBackupFile> = emptyList(),
    val openDriveFolderUrl: String? = null,
    val exportFileName: String? = null,
    val phoneBackup: PhoneBackupFile? = null,
    val phoneBackupDestination: String = "",
    val phoneBackupDestinationDisplayName: String? = null,
    val shareIntent: Intent? = null,
    val driveAuthorizationIntentSender: IntentSender? = null,
    val messageKey: String? = null,
    val operationTarget: BackupTarget? = null,
    val operationStage: String? = null,
    val operationPercent: Int? = null,
)

enum class BackupTarget { PHONE, GOOGLE_DRIVE, AUTOMATIC }

internal fun isUserVisibleBackupStatus(status: String): Boolean = status != "RESTORE_SAFETY_SNAPSHOT"

internal fun restoreErrorMessageKeyFor(errorCode: String?): String = when (errorCode) {
    BackupRestoreException.Code.DRIVE_AUTH_REQUIRED.name -> "backup_drive_reauthorization_required"
    BackupRestoreException.Code.DRIVE_FILE_NOT_FOUND.name -> "backup_drive_file_not_found"
    BackupRestoreException.Code.DRIVE_DOWNLOAD_FAILED.name -> "backup_drive_download_failed"
    BackupRestoreException.Code.DRIVE_FILE_EMPTY.name -> "backup_restore_file_empty"
    BackupRestoreException.Code.DRIVE_FILE_SIZE_MISMATCH.name -> "backup_restore_file_size_mismatch"
    BackupRestoreException.Code.BACKUP_HASH_MISMATCH.name -> "backup_hash_mismatch"
    BackupRestoreException.Code.BACKUP_HEADER_INVALID.name -> "backup_restore_header_invalid"
    BackupRestoreException.Code.BACKUP_FORMAT_UNSUPPORTED.name -> "backup_restore_format_unsupported"
    BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH.name -> "backup_account_mismatch"
    BackupRestoreException.Code.BACKUP_KEY_VERSION_UNAVAILABLE.name -> "backup_key_unavailable"
    BackupRestoreException.Code.BACKUP_DEVICE_KEY_INVALID.name -> "backup_device_key_invalid"
    BackupRestoreException.Code.BACKUP_DECRYPTION_FAILED.name -> "backup_restore_decryption_failed"
    BackupRestoreException.Code.BACKUP_CONTENT_INVALID.name -> "backup_restore_content_invalid"
    BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED.name -> "backup_restore_safety_backup_failed"
    BackupRestoreException.Code.BACKUP_RESTORE_TRANSACTION_FAILED.name -> "backup_restore_transaction_failed"
    BackupRestoreException.Code.BACKUP_ASSET_RESTORE_FAILED.name -> "backup_restore_assets_failed"
    BackupRestoreException.Code.RESTORE_FILE_PERMISSION_LOST.name -> "backup_restore_file_permission_lost"
    BackupRestoreException.Code.RESTORE_ASSET_STAGE_FAILED.name -> "backup_restore_asset_stage_failed"
    BackupRestoreException.Code.RESTORE_ASSET_APPLY_FAILED.name -> "backup_restore_asset_apply_failed"
    BackupRestoreException.Code.RESTORE_DB_SCHEMA_INCOMPATIBLE.name -> "backup_restore_schema_incompatible"
    BackupRestoreException.Code.RESTORE_DB_DELETE_FAILED.name -> "backup_restore_db_delete_failed"
    BackupRestoreException.Code.RESTORE_DB_INSERT_FAILED.name -> "backup_restore_db_insert_failed"
    BackupRestoreException.Code.RESTORE_DB_CONSTRAINT_FAILED.name -> "backup_restore_db_constraint_failed"
    BackupRestoreException.Code.RESTORE_DB_FOREIGN_KEY_FAILED.name -> "backup_restore_db_foreign_key_failed"
    BackupRestoreException.Code.RESTORE_DB_COMMIT_FAILED.name -> "backup_restore_db_commit_failed"
    BackupRestoreException.Code.RESTORE_ROLLBACK_FAILED.name -> "backup_restore_rollback_failed"
    else -> "backup_restore_failed"
}

class BackupViewModel(
    application: Application,
    private val userId: String,
) : AndroidViewModel(application) {
    private val database = TijarioDatabase.getInstance(application)
    private val coordinator = BackupCoordinator(application, database, Supabase.apiClient)
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    private var preparedExport: File? = null
    private var activeWorkJob: Job? = null

    init {
        refreshLatest()
    }

    /** Legacy entry point: explicit UI actions call a target-specific method below. */
    fun createLocalBackup(exportAfterCreate: Boolean) = saveBackupToPhone(exportAfterCreate)

    fun saveBackupToPhone(exportAfterCreate: Boolean = false) {
        createBackup(BackupTarget.PHONE, exportAfterCreate)
    }

    fun backupNowToGoogleDrive() {
        if (_uiState.value.driveConnectionState !is DriveConnectionState.Connected) {
            _uiState.value = _uiState.value.copy(messageKey = driveMessage(_uiState.value.driveConnectionState))
            return
        }
        createBackup(BackupTarget.GOOGLE_DRIVE, exportAfterCreate = false)
    }

    private fun createBackup(target: BackupTarget, exportAfterCreate: Boolean) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBusy = true,
                messageKey = null,
                operationTarget = target,
                operationStage = "backup_preparing",
                operationPercent = 0,
            )
            runCatching { coordinator.createLocalBackup(userId, allowNetwork = true) }
                .onSuccess { record ->
                    preparedExport = resolveBackupArchiveFile(
                        filesRoot = getApplication<Application>().filesDir,
                        userId = userId,
                        storedRelativePath = record.localRelativePath,
                    )
                    val settings = _uiState.value.settings ?: defaultSettings()
                    val phoneBackup = if (target == BackupTarget.PHONE) {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                PhoneBackupRepository(getApplication()).saveVisibleCopy(userId, record, getApplication<Application>().filesDir)
                            }
                        }.getOrElse { error ->
                            _uiState.value = _uiState.value.copy(
                                isBusy = false,
                                messageKey = backupMessageKeyFor(error),
                                operationStage = "backup_failed",
                                operationPercent = null,
                            )
                            return@onSuccess
                        }
                    } else null
                    val effectiveRecord = when (target) {
                        BackupTarget.PHONE -> record.copy(status = "PHONE_SAVED", lastError = null).also {
                            database.tijarioDao().upsertBackupRecord(it)
                        }
                        BackupTarget.GOOGLE_DRIVE -> record.copy(status = "DRIVE_PENDING", lastError = null).also {
                            database.tijarioDao().upsertBackupRecord(it)
                        }
                        BackupTarget.AUTOMATIC -> record
                    }
                    val uploadWorkId = if (target == BackupTarget.GOOGLE_DRIVE) {
                        BackupScheduler.enqueueDriveUpload(getApplication(), settings, effectiveRecord.id, userInitiated = true)
                    } else null
                    _uiState.value = _uiState.value.copy(
                        isBusy = target == BackupTarget.GOOGLE_DRIVE,
                        latestBackup = effectiveRecord,
                        phoneBackup = phoneBackup,
                        phoneBackupDestination = phoneBackup?.destinationKey
                            ?: PhoneBackupRepository(getApplication()).destinationKey(userId),
                        exportFileName = if (exportAfterCreate) preparedExport?.name else null,
                        messageKey = if (exportAfterCreate) null else when (target) {
                            BackupTarget.PHONE -> "backup_phone_saved"
                            BackupTarget.GOOGLE_DRIVE -> "backup_drive_pending"
                            BackupTarget.AUTOMATIC -> null
                        },
                        operationStage = if (target == BackupTarget.GOOGLE_DRIVE) "backup_drive_waiting_start" else "backup_completed",
                        operationPercent = if (target == BackupTarget.GOOGLE_DRIVE) 0 else 100,
                    )
                    if (target == BackupTarget.GOOGLE_DRIVE) {
                        uploadWorkId?.let { observeWork(it, BackupWorkKind.UPLOAD) }
                    }
                    refreshLatest()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        messageKey = backupMessageKeyFor(error),
                        operationStage = "backup_failed",
                        operationPercent = null,
                    )
                }
        }
    }

    fun consumeExportRequest() {
        _uiState.value = _uiState.value.copy(exportFileName = null)
    }

    fun exportPreparedBackup(destination: Uri?) {
        if (destination == null) {
            preparedExport = null
            return
        }
        val source = preparedExport
        preparedExport = null
        if (source == null || !source.isFile) {
            _uiState.value = _uiState.value.copy(messageKey = "backup_export_failed")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            val succeeded = withContext(Dispatchers.IO) {
                runCatching {
                    getApplication<Application>().contentResolver.openOutputStream(destination, "w")
                        ?.use { output -> source.inputStream().use { it.copyTo(output) } }
                        ?: error("Backup destination is unavailable")
                }.isSuccess
            }
            _uiState.value = _uiState.value.copy(
                isBusy = false,
                messageKey = if (succeeded) "backup_exported_success" else "backup_export_failed",
            )
        }
    }

    fun restoreFrom(destination: Uri?) {
        if (destination == null || userId.isBlank() || _uiState.value.isBusy) return
        try {
            getApplication<Application>().contentResolver.takePersistableUriPermission(
                destination,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            _uiState.value = _uiState.value.copy(messageKey = "backup_restore_file_permission_lost")
            return
        }
        BackupScheduler.enqueueFileRestore(getApplication(), userId, destination.toString())?.let(::observeRestoreWork)
    }

    fun restoreLocalRecord(record: BackupRecordEntity) {
        if (record.userId != userId || _uiState.value.isBusy) return
        BackupScheduler.enqueueLocalRestore(getApplication(), userId, record.id)?.let(::observeRestoreWork)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(messageKey = null)
    }

    fun consumeOpenDriveFolderRequest() {
        _uiState.value = _uiState.value.copy(openDriveFolderUrl = null)
    }

    fun consumeShareRequest() {
        _uiState.value = _uiState.value.copy(shareIntent = null)
    }

    fun rememberPhoneBackupFolder(uri: Uri?) {
        if (uri == null || userId.isBlank()) return
        runCatching { PhoneBackupRepository(getApplication()).rememberTree(userId, uri) }
            .onSuccess { refreshLatest() }
            .onFailure { _uiState.value = _uiState.value.copy(messageKey = "backup_phone_folder_failed") }
    }

    fun useDefaultPhoneBackupFolder() {
        if (userId.isBlank()) return
        runCatching { PhoneBackupRepository(getApplication()).useDefaultDestination(userId) }
            .onSuccess { refreshLatest() }
            .onFailure { _uiState.value = _uiState.value.copy(messageKey = "backup_phone_folder_failed") }
    }

    fun requestShareBackup(record: BackupRecordEntity, preferTelegram: Boolean) {
        if (record.userId != userId) return
        val file = resolveBackupArchiveFile(
            filesRoot = getApplication<Application>().filesDir,
            userId = userId,
            storedRelativePath = record.localRelativePath,
        ) ?: run {
            _uiState.value = _uiState.value.copy(messageKey = "backup_share_failed")
            return
        }
        runCatching { BackupShareIntents.create(getApplication(), file, preferTelegram) }
            .onSuccess { _uiState.value = _uiState.value.copy(shareIntent = it) }
            .onFailure { _uiState.value = _uiState.value.copy(messageKey = "backup_share_failed") }
    }

    fun requestShareLatestBackup(preferTelegram: Boolean) {
        _uiState.value.latestBackup?.let { requestShareBackup(it, preferTelegram) }
    }

    fun connectGoogleDrive(changeAccount: Boolean = false) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        viewModelScope.launch {
            if (changeAccount) BackupScheduler.cancelDriveUploads(getApplication(), userId)
            _uiState.value = _uiState.value.copy(isBusy = true, driveConnectionState = DriveConnectionState.Authorizing, messageKey = null)
            when (val outcome = BackupDriveContainer.beginAuthorization(getApplication(), userId, changeAccount)) {
                is DriveAuthorizationOutcome.ResolutionRequired -> {
                    _uiState.value = _uiState.value.copy(isBusy = false, driveAuthorizationIntentSender = outcome.intentSender)
                }
                else -> {
                    val state = BackupDriveContainer.persistAuthorization(getApplication(), userId, outcome)
                    _uiState.value = _uiState.value.copy(isBusy = false, driveConnectionState = state, messageKey = driveMessage(state))
                    refreshLatest()
                }
            }
        }
    }

    fun consumeDriveAuthorizationRequest() {
        _uiState.value = _uiState.value.copy(driveAuthorizationIntentSender = null)
    }

    fun completeGoogleDriveAuthorization(resultIntent: Intent?) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            val state = BackupDriveContainer.completeAuthorization(getApplication(), userId, resultIntent)
            _uiState.value = _uiState.value.copy(isBusy = false, driveConnectionState = state, messageKey = driveMessage(state))
            refreshLatest()
        }
    }

    fun disconnectGoogleDrive(revoke: Boolean) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            val state = BackupDriveContainer.disconnect(getApplication(), userId, revoke)
            val settings = (_uiState.value.settings ?: defaultSettings()).copy(driveEnabled = false, updatedAt = System.currentTimeMillis())
            withContext(Dispatchers.IO) { database.tijarioDao().upsertBackupSettings(settings) }
            _uiState.value = _uiState.value.copy(isBusy = false, settings = settings, driveConnectionState = state, messageKey = "backup_drive_disconnected")
            refreshLatest()
        }
    }

    fun requestOpenDriveFolder() {
        if (_uiState.value.driveConnectionState !is DriveConnectionState.Connected) return
        viewModelScope.launch {
            runCatching {
                val client = driveClient()
                val folder = DriveFolderRepository(client).resolve()
                client.openFolderUrl(folder.backupsId)
            }.onSuccess { url ->
                _uiState.value = _uiState.value.copy(openDriveFolderUrl = url)
            }.onFailure {
                _uiState.value = _uiState.value.copy(messageKey = "backup_drive_open_failed")
            }
        }
    }

    fun restoreFromDrive(remote: DriveBackupFile) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        BackupScheduler.enqueueDriveRestore(getApplication(), userId, remote)?.let(::observeRestoreWork)
    }

    fun deleteDriveBackup(remote: DriveBackupFile) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        if (_uiState.value.driveBackups.firstOrNull()?.id == remote.id) {
            _uiState.value = _uiState.value.copy(messageKey = "backup_drive_keep_newest")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            runCatching {
                DriveBackupRepository(database, getApplication<Application>().filesDir, driveClient()).delete(userId, remote)
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_drive_deleted")
                    refreshLatest()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_drive_delete_failed")
                }
        }
    }

    fun updateFrequency(frequency: String) {
        updateSettings { it.copy(frequency = frequency, updatedAt = System.currentTimeMillis()) }
    }

    fun updateChargingOnly(enabled: Boolean) {
        updateSettings { it.copy(chargingOnly = enabled, updatedAt = System.currentTimeMillis()) }
    }

    fun updateWifiOnly(enabled: Boolean) {
        updateSettings { it.copy(wifiOnly = enabled, updatedAt = System.currentTimeMillis()) }
    }

    fun updateDriveEnabled(enabled: Boolean) {
        updateSettings { it.copy(driveEnabled = enabled, updatedAt = System.currentTimeMillis()) }
    }

    fun retryDriveUpload(backupId: String) {
        val settings = _uiState.value.settings ?: return
        if (backupId.isBlank()) return
        viewModelScope.launch {
            val record = withContext(Dispatchers.IO) {
                database.tijarioDao().getBackupRecords(userId).firstOrNull { it.id == backupId }
            } ?: return@launch
            withContext(Dispatchers.IO) {
                database.tijarioDao().upsertBackupRecord(record.copy(status = "DRIVE_PENDING", lastError = null))
            }
            BackupScheduler.enqueueDriveUpload(getApplication(), settings, backupId, userInitiated = true)
                ?.let { observeWork(it, BackupWorkKind.UPLOAD) }
            refreshLatest()
        }
    }

    private fun observeRestoreWork(workId: UUID) {
        _uiState.value = _uiState.value.copy(
            isBusy = true,
            messageKey = null,
            operationTarget = BackupTarget.GOOGLE_DRIVE,
            operationStage = "backup_waiting_start",
            operationPercent = 0,
        )
        observeWork(workId, BackupWorkKind.RESTORE)
    }

    /** WorkManager, rather than enqueue time, is the source of truth for long-running backup UI. */
    private fun observeWork(workId: UUID, kind: BackupWorkKind) {
        activeWorkJob?.cancel()
        activeWorkJob = viewModelScope.launch {
            WorkManager.getInstance(getApplication()).getWorkInfoByIdFlow(workId).collect { info ->
                if (info == null) return@collect
                val stage = info.progress.getString(DriveUploadWorker.PROGRESS_STAGE_KEY)
                    ?: info.progress.getString(BackupRestoreWorker.PROGRESS_STAGE_KEY)
                    ?: when (info.state) {
                        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> "backup_waiting_start"
                        WorkInfo.State.RUNNING -> "backup_preparing"
                        WorkInfo.State.SUCCEEDED -> if (kind == BackupWorkKind.UPLOAD) "backup_drive_uploaded" else "backup_restored_success"
                        WorkInfo.State.CANCELLED -> "backup_cancelled"
                        WorkInfo.State.FAILED -> if (kind == BackupWorkKind.UPLOAD) "backup_drive_failed" else restoreErrorMessageKeyFor(info.outputData.getString(BackupRestoreWorker.ERROR_CODE_KEY))
                    }
                val progress = info.progress.getInt(DriveUploadWorker.PROGRESS_PERCENT_KEY, -1)
                    .takeIf { it >= 0 }
                    ?: info.progress.getInt(BackupRestoreWorker.PROGRESS_PERCENT_KEY, -1).takeIf { it >= 0 }
                val message = when (info.state) {
                    WorkInfo.State.SUCCEEDED -> if (kind == BackupWorkKind.UPLOAD) "backup_drive_uploaded" else "backup_restored_success"
                    WorkInfo.State.FAILED -> if (kind == BackupWorkKind.UPLOAD) "backup_drive_failed" else restoreErrorMessageKeyFor(info.outputData.getString(BackupRestoreWorker.ERROR_CODE_KEY))
                    WorkInfo.State.CANCELLED -> "backup_cancelled"
                    else -> null
                }
                _uiState.value = _uiState.value.copy(
                    isBusy = !info.state.isFinished,
                    operationStage = stage,
                    operationPercent = progress,
                    messageKey = message ?: _uiState.value.messageKey,
                )
                if (info.state.isFinished) {
                    refreshLatest()
                    activeWorkJob?.cancel()
                }
            }
        }
    }

    private fun refreshLatest() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val (records, persistedSettings, planPolicy) = withContext(Dispatchers.IO) {
                val dao = database.tijarioDao()
                val policy = BackupPlanPolicy.from(dao.getAccountEntitlement(userId))
                Triple(dao.getBackupRecords(userId), dao.getBackupSettings(userId) ?: defaultSettings(), policy)
            }
            val settings = planPolicy.apply(persistedSettings)
            if (settings != persistedSettings) {
                withContext(Dispatchers.IO) { database.tijarioDao().upsertBackupSettings(settings) }
            }
            val driveState = runCatching { BackupDriveContainer.authorizationState(getApplication(), userId) }
                .getOrDefault(DriveConnectionState.NotConfigured)
            val driveBackups = if (driveState is DriveConnectionState.Connected) {
                runCatching {
                    DriveBackupRepository(database, getApplication<Application>().filesDir, driveClient())
                        .list(userId)
                }.getOrDefault(emptyList())
            } else emptyList()
            val visibleRecords = records.filter { isUserVisibleBackupStatus(it.status) }
            _uiState.value = _uiState.value.copy(
                latestBackup = visibleRecords.firstOrNull(),
                history = visibleRecords,
                settings = settings,
                backupPlanPolicy = planPolicy,
                driveConnectionState = driveState,
                driveBackups = driveBackups,
                phoneBackupDestination = PhoneBackupRepository(getApplication()).destinationKey(userId),
                phoneBackupDestinationDisplayName = PhoneBackupRepository(getApplication()).destinationDisplayName(userId),
            )
            BackupScheduler.apply(getApplication(), settings)
        }
    }

    private fun updateSettings(transform: (BackupSettingsEntity) -> BackupSettingsEntity) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val settings = (_uiState.value.backupPlanPolicy).apply(transform(_uiState.value.settings ?: defaultSettings()))
            withContext(Dispatchers.IO) { database.tijarioDao().upsertBackupSettings(settings) }
            BackupScheduler.apply(getApplication(), settings)
            _uiState.value = _uiState.value.copy(settings = settings, messageKey = "backup_schedule_saved")
        }
    }

    private fun defaultSettings(): BackupSettingsEntity = BackupSettingsEntity(
        userId = userId,
        frequency = "manual",
        wifiOnly = true,
        chargingOnly = false,
        driveEnabled = false,
        retentionDaily = 7,
        retentionWeekly = 4,
        retentionMonthly = 3,
        updatedAt = System.currentTimeMillis(),
    )

    companion object {
        private const val LOG_TAG = "TijarioBackup"

        fun factory(application: Application, userId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BackupViewModel(application, userId) as T
            }
    }

    private fun driveClient() = DriveBackupRuntime.client(getApplication<Application>(), userId)

    private fun progressPercent(transferred: Long, total: Long): Int =
        if (total <= 0L) 0 else ((transferred * 100L) / total).toInt().coerceIn(0, 100)

    private fun driveMessage(state: DriveConnectionState): String? = when (state) {
        is DriveConnectionState.Connected -> "backup_drive_connected"
        DriveConnectionState.AuthorizationRequired -> "backup_drive_permission_denied"
        DriveConnectionState.ReauthorizationRequired -> "backup_drive_reauthorization_required"
        DriveConnectionState.NotConfigured -> "backup_drive_not_configured"
        DriveConnectionState.InvalidConfiguration -> "backup_drive_invalid_configuration"
        DriveConnectionState.TemporarilyUnavailable -> "backup_drive_temporarily_unavailable"
        else -> null
    }

    private enum class BackupWorkKind { UPLOAD, RESTORE }
}
