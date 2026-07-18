package app.tijario.features.backup

import android.content.Context
import app.tijario.config.AppLanguage
import app.tijario.data.local.DocumentEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.toModel
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentItem
import app.tijario.data.model.DocumentType
import app.tijario.features.documents.mapper.SavedDocumentRenderMapper
import app.tijario.features.documents.pdf.LocalPdfGenerator
import app.tijario.features.documents.template.DocumentTemplateRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

data class PdfPreparationSummary(
    val generated: Int,
    val failed: Int,
)

class LocalBackupPdfPreparer(
    context: Context,
    private val database: TijarioDatabase,
    private val filesRoot: File,
    private val pdfGenerator: LocalPdfGenerator = LocalPdfGenerator(context, allowNetworkLogoFetch = false),
) {
    suspend fun prepare(userId: String): PdfPreparationSummary {
        val dao = database.tijarioDao()
        val documents = withContext(Dispatchers.IO) { dao.observeDocuments(userId).first() }
        val business = withContext(Dispatchers.IO) { dao.getBusinessSettings(userId)?.toModel() }
        val showBranding = withContext(Dispatchers.IO) {
            dao.getAccountEntitlement(userId)?.removeTijarioBranding != true
        }
        var generated = 0
        var failed = 0

        documents.forEach { document ->
            if (hasCurrentPdf(document)) return@forEach
            val succeeded = runCatching {
                val complete = completeDocument(userId, document)
                require(complete.items.isNotEmpty()) { "Document has no items" }
                val metadata = withContext(Dispatchers.IO) { dao.getDocumentMetadata(userId, document.id) }
                val language = if (document.documentLanguage.equals("en", ignoreCase = true)) AppLanguage.EN else AppLanguage.AR
                val model = SavedDocumentRenderMapper.map(
                    document = complete,
                    businessSettings = business,
                    language = language,
                    templateId = DocumentTemplateRegistry.normalizeId(document.templateId),
                    metadata = metadata,
                    showTijarioBranding = showBranding,
                )
                val rendered = pdfGenerator.ensurePdf(model).file
                val destination = destination(userId, document.id)
                withContext(Dispatchers.IO) {
                    destination.parentFile?.mkdirs()
                    rendered.copyTo(destination, overwrite = true)
                    require(isPdf(destination)) { "Generated PDF is invalid" }
                    val relativePath = destination.relativeTo(filesRoot).invariantSeparatorsPath
                    dao.updateDocumentPdfState(
                        userId = userId,
                        documentId = document.id,
                        relativePath = relativePath,
                        generatedAt = System.currentTimeMillis(),
                        documentRevision = document.localRevision,
                        contentHash = sha256(destination),
                        status = "ready",
                    )
                }
            }.isSuccess

            if (succeeded) {
                generated += 1
            } else {
                failed += 1
                withContext(Dispatchers.IO) {
                    dao.updateDocumentPdfState(userId, document.id, null, null, null, null, "failed")
                }
            }
        }

        return PdfPreparationSummary(generated, failed)
    }

    private suspend fun completeDocument(userId: String, document: DocumentEntity): CompleteDocument {
        val dao = database.tijarioDao()
        return withContext(Dispatchers.IO) {
            val currentCustomer = dao.getCustomer(userId, document.customerId)?.toModel()
            val customer = if (document.customerSnapshotName != null || currentCustomer != null) {
                Customer(
                    id = document.customerId,
                    userId = document.userId,
                    name = document.customerSnapshotName ?: currentCustomer?.name.orEmpty(),
                    whatsappNumber = document.customerSnapshotWhatsapp ?: currentCustomer?.whatsappNumber.orEmpty(),
                    city = document.customerSnapshotCity ?: currentCustomer?.city,
                    notes = currentCustomer?.notes,
                    updatedAt = currentCustomer?.updatedAt,
                )
            } else {
                null
            }
            val items = dao.getDocumentItems(userId, document.id).map { item ->
                DocumentItem(
                    id = item.id,
                    documentId = item.documentId,
                    productId = item.productId,
                    name = item.name,
                    description = item.description,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice.toDouble(),
                )
            }
            CompleteDocument(
                id = document.id,
                userId = document.userId,
                customerId = document.customerId,
                type = if (document.type == "quote") DocumentType.Quote else DocumentType.Invoice,
                documentNumber = document.documentNumber,
                documentTitle = document.documentTitle,
                status = document.status,
                paymentStatus = document.paymentStatus,
                amountPaid = document.amountPaid?.toDouble(),
                issueDate = document.issueDate,
                createdAt = document.createdAt,
                subtotal = document.subtotal.toDouble(),
                discount = document.discount.toDouble(),
                discountLabel = document.discountLabel,
                extraFees = document.extraFees.toDouble(),
                extraFeesLabel = document.extraFeesLabel,
                taxName = document.taxName,
                taxRate = document.taxRate.toDouble(),
                taxAmount = document.taxAmount.toDouble(),
                total = document.total.toDouble(),
                currency = document.currency,
                templateId = document.templateId,
                documentLanguage = document.documentLanguage,
                notes = document.notes,
                termsText = document.termsText,
                customer = customer,
                items = items,
                updatedAt = document.serverRevision,
            )
        }
    }

    private fun hasCurrentPdf(document: DocumentEntity): Boolean {
        if (document.pdfGenerationStatus != "ready" || document.pdfDocumentRevision != document.localRevision) return false
        val path = document.localPdfRelativePath ?: return false
        return runCatching {
            val file = File(filesRoot, path).canonicalFile
            file.toPath().startsWith(filesRoot.canonicalFile.toPath()) && isPdf(file)
        }.getOrDefault(false)
    }

    private fun destination(userId: String, documentId: String): File {
        require(userId.none { it == '/' || it == '\\' } && documentId.none { it == '/' || it == '\\' })
        return File(filesRoot, "users/$userId/document-pdfs/$documentId.pdf")
    }

    private fun isPdf(file: File): Boolean =
        file.isFile && file.length() > 4 && file.inputStream().use { input ->
            val signature = ByteArray(4)
            input.read(signature) == 4 && signature.decodeToString() == "%PDF"
        }

    private fun sha256(file: File): String =
        file.inputStream().use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        }
}
