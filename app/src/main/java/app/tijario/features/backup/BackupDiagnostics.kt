package app.tijario.features.backup

import android.util.Log

/** Logs only restore/upload stages and safe error metadata. */
internal object BackupDiagnostics {
    private const val TAG = "TijarioBackup"

    fun stage(operation: String, stage: String, table: String? = null, error: Throwable? = null) {
        val tablePart = table?.let { " table=$it" }.orEmpty()
        val errorPart = error?.let { " error=${it.javaClass.simpleName}" }.orEmpty()
        Log.i(TAG, "backup operation=$operation stage=$stage$tablePart$errorPart")
    }
}
