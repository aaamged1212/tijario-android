package app.tijario.features.backup

import app.tijario.data.local.BackupSettingsEntity
import app.tijario.data.local.TijarioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class BackupRetentionPruner(
    private val database: TijarioDatabase,
    private val filesRoot: File,
) {
    suspend fun prune(userId: String, settings: BackupSettingsEntity) = withContext(Dispatchers.IO) {
        val keep = backupRetentionCount(settings) ?: return@withContext
        val dao = database.tijarioDao()
        dao.getBackupRecords(userId).drop(keep).forEach { record ->
            val file = File(filesRoot, record.localRelativePath).canonicalFile
            if (file.toPath().startsWith(filesRoot.canonicalFile.toPath())) {
                if (!file.exists() || file.delete()) {
                    dao.deleteBackupRecord(userId, record.id)
                }
            }
        }
    }

}

internal fun backupRetentionCount(settings: BackupSettingsEntity): Int? = when (settings.frequency.lowercase()) {
    "daily" -> settings.retentionDaily.coerceAtLeast(1)
    "weekly" -> settings.retentionWeekly.coerceAtLeast(1)
    else -> null
}
