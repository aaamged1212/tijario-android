package app.tijario.features.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

/** Opens the platform file picker at the configured backup directory when possible. */
class RestoreBackupDocumentContract(
    context: Context,
    userId: String,
) : ActivityResultContract<Array<String>, Uri?>() {
    private val initialUri = PhoneBackupRepository(context.applicationContext).restoreInitialUri(userId)

    override fun createIntent(context: Context, input: Array<String>): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("application/octet-stream")
        .putExtra(Intent.EXTRA_MIME_TYPES, input)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        .apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        if (resultCode == android.app.Activity.RESULT_OK) intent?.data else null
}
