package app.tijario.features.backup

import java.io.File
import java.nio.file.StandardCopyOption
import java.util.UUID

class BackupAssetRestorer(
    private val filesRoot: File,
) {
    fun stage(userId: String, entries: Map<String, ByteArray>): StagedBackupAssets {
        require(userId.isNotBlank()) { "Restore account is required" }
        val documentIds = ids(entries, "data/documents.json")
        val productIds = ids(entries, "data/products.json")
        val stagingRoot = resolveInsideFiles("users/$userId/restore-staging/${UUID.randomUUID()}")
        if (!stagingRoot.mkdirs()) throw BackupValidationException("Restore staging directory could not be created")
        val staged = mutableListOf<StagedAsset>()
        try {
            entries.toSortedMap().forEach { (archivePath, bytes) ->
                val finalRelative = when {
                    archivePath.startsWith("assets/business-logo/") -> {
                        val relative = archivePath.removePrefix("assets/business-logo/")
                        validateAssetRelativePath(relative)
                        "users/$userId/business/logo/$relative"
                    }
                    archivePath.startsWith("assets/product-images/") -> {
                        val fileName = archivePath.removePrefix("assets/product-images/")
                        val productId = fileName.removeSuffix(".jpg")
                        if (fileName != "$productId.jpg" || productId !in productIds) {
                            throw BackupValidationException("Backup product image identity is invalid")
                        }
                        "product_images/$fileName"
                    }
                    archivePath.startsWith("assets/document-pdfs/") -> {
                        val fileName = archivePath.removePrefix("assets/document-pdfs/")
                        val documentId = fileName.removeSuffix(".pdf")
                        if (fileName != "$documentId.pdf" || documentId !in documentIds) {
                            throw BackupValidationException("Backup PDF identity is invalid")
                        }
                        "documents/pdfs/$userId/$fileName"
                    }
                    else -> return@forEach
                }
                val stagedFile = File(stagingRoot, "new/$finalRelative")
                requireNotNull(stagedFile.parentFile).mkdirs()
                stagedFile.writeBytes(bytes)
                staged += StagedAsset(stagedFile, resolveInsideFiles(finalRelative), finalRelative)
            }
            return StagedBackupAssets(stagingRoot, staged)
        } catch (error: Exception) {
            stagingRoot.deleteRecursively()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup assets could not be staged", error)
        }
    }

    private fun ids(entries: Map<String, ByteArray>, path: String): Set<String> {
        val snapshot = LogicalBackupSnapshotCodec.decode(
            entries[path] ?: throw BackupValidationException("Backup logical data is missing: $path"),
        )
        val idIndex = snapshot.columns.indexOf("id")
        if (idIndex < 0) throw BackupValidationException("Backup identity column is missing")
        return snapshot.rows.mapNotNull { it[idIndex].value }.toSet()
    }

    private fun validateAssetRelativePath(path: String) {
        if (path.isBlank() || '\\' in path || path.startsWith('/') || path.split('/').any { it.isBlank() || it == "." || it == ".." }) {
            throw BackupValidationException("Backup asset path is unsafe")
        }
    }

    private fun resolveInsideFiles(relativePath: String): File {
        val root = filesRoot.canonicalFile.toPath()
        val candidate = File(filesRoot, relativePath).canonicalFile
        if (!candidate.toPath().startsWith(root)) throw BackupValidationException("Restore asset path is unsafe")
        return candidate
    }
}

internal data class StagedAsset(
    val stagedFile: File,
    val finalFile: File,
    val relativePath: String,
)

class StagedBackupAssets internal constructor(
    private val stagingRoot: File,
    private val assets: List<StagedAsset>,
) {
    fun apply(): AppliedBackupAssets {
        val applied = mutableListOf<AppliedAsset>()
        try {
            assets.forEach { asset ->
                val previous = File(stagingRoot, "previous/${asset.relativePath}")
                requireNotNull(asset.finalFile.parentFile).mkdirs()
                if (asset.finalFile.exists()) {
                    requireNotNull(previous.parentFile).mkdirs()
                    move(asset.finalFile, previous)
                }
                applied += AppliedAsset(asset.finalFile, previous.takeIf(File::exists))
                move(asset.stagedFile, asset.finalFile)
            }
            return AppliedBackupAssets(stagingRoot, applied)
        } catch (error: Exception) {
            AppliedBackupAssets(stagingRoot, applied).rollback()
            if (error is BackupValidationException) throw error
            throw BackupValidationException("Backup assets could not be applied", error)
        }
    }

    fun discard() {
        stagingRoot.deleteRecursively()
    }

    private fun move(from: File, to: File) {
        try {
            java.nio.file.Files.move(
                from.toPath(),
                to.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
            java.nio.file.Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

internal data class AppliedAsset(val finalFile: File, val previousFile: File?)

class AppliedBackupAssets internal constructor(
    private val stagingRoot: File,
    private val assets: List<AppliedAsset>,
) {
    fun complete() {
        stagingRoot.deleteRecursively()
    }

    fun rollback() {
        assets.asReversed().forEach { asset ->
            asset.finalFile.delete()
            asset.previousFile?.takeIf(File::exists)?.let { previous ->
                requireNotNull(asset.finalFile.parentFile).mkdirs()
                previous.copyTo(asset.finalFile, overwrite = true)
            }
        }
        stagingRoot.deleteRecursively()
    }
}
