package app.tijario.features.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.config.Supabase
import app.tijario.data.local.BackupRecordEntity
import app.tijario.data.local.BackupSettingsEntity
import app.tijario.data.local.TijarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

data class BackupUiState(
    val isBusy: Boolean = false,
    val latestBackup: BackupRecordEntity? = null,
    val settings: BackupSettingsEntity? = null,
    val exportFileName: String? = null,
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
                    _uiState.value = _uiState.value.copy(
                        isBusy = false,
                        latestBackup = record,
                        exportFileName = if (exportAfterCreate) preparedExport?.name else null,
                        messageKey = if (exportAfterCreate) null else "backup_created_success",
                    )
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
                val archive = withContext(Dispatchers.IO) { readArchive(destination) }
                coordinator.restoreLocalBackup(userId, archive, allowNetwork = true)
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

    fun updateFrequency(frequency: String) {
        updateSettings { it.copy(frequency = frequency, updatedAt = System.currentTimeMillis()) }
    }

    fun updateChargingOnly(enabled: Boolean) {
        updateSettings { it.copy(chargingOnly = enabled, updatedAt = System.currentTimeMillis()) }
    }

    private fun refreshLatest() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val (latest, settings) = withContext(Dispatchers.IO) {
                val dao = database.tijarioDao()
                dao.getBackupRecords(userId).firstOrNull() to (dao.getBackupSettings(userId) ?: defaultSettings())
            }
            _uiState.value = _uiState.value.copy(latestBackup = latest, settings = settings)
            BackupScheduler.apply(getApplication(), settings)
        }
    }

    private fun updateSettings(transform: (BackupSettingsEntity) -> BackupSettingsEntity) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val settings = transform(_uiState.value.settings ?: defaultSettings())
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

    private fun readArchive(uri: Uri): ByteArray {
        val input = getApplication<Application>().contentResolver.openInputStream(uri)
            ?: throw BackupValidationException("Backup file is unavailable")
        return input.use { stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                total += count
                if (total > MAX_ARCHIVE_BYTES) throw BackupValidationException("Backup archive is too large")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

    companion object {
        private const val MAX_ARCHIVE_BYTES = 256L * 1024L * 1024L

        fun factory(application: Application, userId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    BackupViewModel(application, userId) as T
            }
    }
}
