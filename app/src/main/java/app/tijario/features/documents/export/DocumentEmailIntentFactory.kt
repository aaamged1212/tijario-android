package app.tijario.features.documents.export

import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import app.tijario.config.AppLanguage
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.model.DocumentRenderModel
import java.io.File

class DocumentEmailIntentFactory(
    private val context: Context,
    private val shareFactory: DocumentShareIntentFactory,
) {
    fun email(model: DocumentRenderModel, file: File): Intent? {
        val subjectType = when (model.documentType) {
            DocumentType.Invoice -> if (model.language == AppLanguage.AR) "\u0641\u0627\u062a\u0648\u0631\u0629" else "Invoice"
            DocumentType.Quote -> if (model.language == AppLanguage.AR) "\u0639\u0631\u0636 \u0633\u0639\u0631" else "Quotation"
        }
        val body = if (model.language == AppLanguage.AR) {
            "\u0645\u0631\u0641\u0642 \u0645\u0633\u062a\u0646\u062f \u062a\u062c\u0627\u0631\u064a\u0648 \u0628\u0635\u064a\u063a\u0629 PDF."
        } else {
            "Attached is the Tijario PDF document."
        }
        val contentUri = shareFactory.contentUri(file)
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "$subjectType ${model.documentNumber}")
            putExtra(Intent.EXTRA_TEXT, body)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            clipData = ClipData.newUri(context.contentResolver, "PDF", contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val emailTargets = queryEmailTargets()
            .mapNotNull { resolveInfo ->
                val activity = resolveInfo.activityInfo ?: return@mapNotNull null
                Intent(baseIntent).apply {
                    component = ComponentName(activity.packageName, activity.name)
                    setPackage(activity.packageName)
                }
            }
            .distinctBy { it.component?.flattenToString() }
        if (emailTargets.isEmpty()) return null
        return Intent.createChooser(emailTargets.first(), subjectType).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, emailTargets.drop(1).toTypedArray())
        }
    }

    private fun queryEmailTargets() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")),
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")),
                PackageManager.MATCH_DEFAULT_ONLY,
            )
        }
}
