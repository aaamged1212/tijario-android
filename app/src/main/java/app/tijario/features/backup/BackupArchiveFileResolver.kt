package app.tijario.features.backup

import java.io.File

/** Resolves current and legacy backup-record paths without trusting stored path traversal. */
internal fun resolveBackupArchiveFile(
    filesRoot: File,
    userId: String,
    storedRelativePath: String,
): File? {
    val canonicalRoot = filesRoot.canonicalFile
    val direct = File(canonicalRoot, storedRelativePath).canonicalFile
    if (direct.isFile && direct.isWithin(canonicalRoot)) return direct

    val archiveName = File(storedRelativePath).name
    if (!archiveName.endsWith(".tijario")) return null
    val recovered = File(canonicalRoot, "users/$userId/backups/$archiveName").canonicalFile
    return recovered.takeIf { it.isFile && it.isWithin(canonicalRoot) }
}

private fun File.isWithin(root: File): Boolean =
    path == root.path || path.startsWith("${root.path}${File.separator}")
