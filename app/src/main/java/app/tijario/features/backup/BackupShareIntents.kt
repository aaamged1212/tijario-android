package app.tijario.features.backup

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object BackupShareIntents {
    private const val TELEGRAM_PACKAGE = "org.telegram.messenger"

    fun create(context: Context, archive: File, preferTelegram: Boolean): Intent {
        require(archive.isFile && archive.name.endsWith(".tijario")) { "Only finalized encrypted backups are shareable" }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archive)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("Tijario backup", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        if (preferTelegram && context.packageManager.resolveActivity(send.setPackage(TELEGRAM_PACKAGE), 0) != null) {
            return send
        }
        send.setPackage(null)
        return Intent.createChooser(send, null)
    }
}
