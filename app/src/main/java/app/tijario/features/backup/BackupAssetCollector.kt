package app.tijario.features.backup

import java.io.File

data class BackupAssetCollection(
    val entries: Map<String, ByteArray>,
    val pdfIncludedCount: Int,
    val pdfMissingCount: Int,
    val pdfFailedGenerationCount: Int,
)

class BackupAssetCollector(
    private val filesRoot: File,
) {
    fun collect(userId: String, logicalEntries: Map<String, ByteArray>): BackupAssetCollection {
        val assets = linkedMapOf<String, ByteArray>()
        val documents = decode(logicalEntries, "data/documents.json")
        val products = decode(logicalEntries, "data/products.json")
        var pdfIncluded = 0
        var pdfMissing = 0
        var pdfFailed = 0

        documents.rows.forEach { row ->
            val documentId = documents.text(row, "id") ?: return@forEach
            val pdfPath = documents.text(row, "local_pdf_relative_path")
            val pdfStatus = documents.text(row, "pdf_generation_status")
            val pdfFile = pdfPath?.let(::resolveInsideFilesRoot)
            if (pdfFile != null && pdfFile.isFile) {
                assets["assets/document-pdfs/$documentId.pdf"] = pdfFile.readBytes()
                pdfIncluded += 1
            } else if (pdfStatus == "failed") {
                pdfFailed += 1
            } else {
                pdfMissing += 1
            }
        }

        products.rows.forEach { row ->
            val productId = products.text(row, "id") ?: return@forEach
            val productImage = resolveInsideFilesRoot("product_images/$productId.jpg")
            if (productImage.isFile) {
                assets["assets/product-images/$productId.jpg"] = productImage.readBytes()
            }
        }

        val logoRoot = resolveInsideFilesRoot("users/$userId/business/logo")
        if (logoRoot.isDirectory) {
            logoRoot.walkTopDown().filter(File::isFile).forEach { file ->
                val relative = file.relativeTo(logoRoot).invariantSeparatorsPath
                if (relative.split('/').none { it == ".." }) {
                    assets["assets/business-logo/$relative"] = file.readBytes()
                }
            }
        }

        return BackupAssetCollection(assets, pdfIncluded, pdfMissing, pdfFailed)
    }

    private fun decode(entries: Map<String, ByteArray>, path: String): LogicalTableSnapshot =
        LogicalBackupSnapshotCodec.decode(
            entries[path] ?: throw BackupValidationException("Backup logical data is missing: $path"),
        )

    private fun LogicalTableSnapshot.text(row: List<LogicalBackupValue>, column: String): String? {
        val index = columns.indexOf(column)
        return if (index >= 0) row[index].value else null
    }

    private fun resolveInsideFilesRoot(relativePath: String): File {
        val candidate = File(filesRoot, relativePath)
        val rootPath = filesRoot.canonicalFile.toPath()
        val candidatePath = candidate.canonicalFile.toPath()
        if (!candidatePath.startsWith(rootPath)) throw BackupValidationException("Local asset path is unsafe")
        return candidate
    }
}
