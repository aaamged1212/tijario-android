package app.tijario.features.backup

import android.app.Application
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    val shareIntent: Intent? = null,
    val driveAuthorizationIntentSender: IntentSender? = null,
    val messageKey: String? = null,
)

class BackupViewModel(
    application: Application,
    private val userId: String,
) : AndroidViewModel(application) {
    private val database = TijarioDatabase.getInstance(application)
    private val coordinator = BackupCoordinator(application, database, Supabase.apiClient)
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()
    private var preparedExport: File? = null

    init {
        refreshLatest()
    }

    fun createLocalBackup(exportAfterCreate: Boolean) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            runCatching { coordinator.createLocalBackup(userId, allowNetwork = true) }
                .onSuccess { record ->
                    val file = File(getApplication<Application>().filesDir, record.localRelativePath)
                    preparedExport = file.takeIf(File::isFile)
                    val phoneBackup = withContext(Dispatchers.IO) {
                        runCatching {
                            PhoneBackupRepository(getApplication()).saveVisibleCopy(userId, record, getApplication<Application>().filesDir)
                        }.getOrNull()
                    }
                    val settings = _uiState.value.settings ?: defaultSettings()
                    val effectiveRecord = if (settings.driveEnabled) {
                        record.copy(status = "DRIVE_PENDING").also {
                            database.tijarioDao().upsertBackupRecord(it)
                            BackupScheduler.enqueueDriveUpload(getApplication(), settings, it.id)
                        }
                    } else record
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        latestBackup = effectiveRecord,
                        phoneBackup = phoneBackup,
                        phoneBackupDestination = PhoneBackupRepository(getApplication()).destinationKey(userId),
                        exportFileName = if (exportAfterCreate) preparedExport?.name else null,
                        messageKey = if (exportAfterCreate) null else if (phoneBackup != null) "backup_phone_saved" else "backup_created_success",
                    )
                    refreshLatest()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_create_failed")
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            runCatching {
                val staged = withContext(Dispatchers.IO) {
                    val input = getApplication<Application>().contentResolver.openInputStream(destination)
                        ?: throw BackupValidationException("Backup file is unavailable")
                    input.use { BackupArchiveInputStager.copyToPrivateFile(it, getApplication<Application>().filesDir, userId) }
                }
                try {
                    coordinator.restoreLocalBackup(userId, staged.file, allowNetwork = true)
                } finally {
                    staged.delete()
                }
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restored_success")
                refreshLatest()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restore_failed")
            }
        }
    }

    fun restoreLocalRecord(record: BackupRecordEntity) {
        if (record.userId != userId || _uiState.value.isBusy) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            runCatching {
                val file = File(getApplication<Application>().filesDir, record.localRelativePath)
                coordinator.restoreLocalBackup(userId, file, allowNetwork = true)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restored_success")
                refreshLatest()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restore_failed")
            }
        }
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

    fun requestShareBackup(record: BackupRecordEntity, preferTelegram: Boolean) {
        if (record.userId != userId) return
        val file = File(getApplication<Application>().filesDir, record.localRelativePath)
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            val temporary = File(getApplication<Application>().cacheDir, "drive-restore-${remote.backupId}.tijario")
            runCatching {
                val repository = DriveBackupRepository(
                    database,
                    getApplication<Application>().filesDir,
                    driveClient(),
                )
                repository.download(userId, remote, temporary)
                coordinator.restoreLocalBackup(userId, temporary, allowNetwork = true)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restored_success")
                refreshLatest()
            }.onFailure {
                _uiState.value = _uiState.value.copy(isBusy = false, messageKey = "backup_restore_failed")
            }
            temporary.delete()
        }
    }

    fun deleteDriveBackup(remote: DriveBackupFile) {
        if (userId.isBlank() || _uiState.value.isBusy) return
        if (_uiState.value.driveBackups.firstOrNull()?.id == remote.id) {
            _uiState.value = _uiState.value.copy(messageKey = "backup_drive_keep_newest")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBusy = true, messageKey = null)
            runCatching { driveClient().deleteFile(remote.id) }
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
        if (!settings.driveEnabled || backupId.isBlank()) return
        viewModelScope.launch {
            val record = withContext(Dispatchers.IO) {
                database.tijarioDao().getBackupRecords(userId).firstOrNull { it.id == backupId }
            } ?: return@launch
            withContext(Dispatchers.IO) {
                database.tijarioDao().upsertBackupRecord(record.copy(status = "DRIVE_PENDING", lastError = null))
            }
            BackupScheduler.enqueueDriveUpload(getApplication(), settings, backupId)
            refreshLatest()
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
            _uiState.value = _uiState.value.copy(
                latestBackup = records.firstOrNull(),
                history = records,
                settings = settings,
                backupPlanPolicy = planPolicy,
                driveConnectionState = driveState,
                driveBackups = driveBackups,
                phoneBackupDestination = PhoneBackupRepository(getApplication()).destinationKey(userId),
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
        fun factory(application: Application, userId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BackupViewModel(application, userId) as T
            }
    }

    private fun driveClient() = DriveBackupRuntime.client(getApplication<Application>(), userId)

    private fun driveMessage(state: DriveConnectionState): String? = when (state) {
        is DriveConnectionState.Connected -> "backup_drive_connected"
        DriveConnectionState.AuthorizationRequired -> "backup_drive_permission_denied"
        DriveConnectionState.ReauthorizationRequired -> "backup_drive_reauthorization_required"
        DriveConnectionState.NotConfigured -> "backup_drive_not_configured"
        DriveConnectionState.TemporarilyUnavailable -> "backup_drive_temporarily_unavailable"
        else -> null
    }
}
