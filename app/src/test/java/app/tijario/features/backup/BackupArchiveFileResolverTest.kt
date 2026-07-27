package app.tijario.features.backup

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupArchiveFileResolverTest {
    @Test
    fun resolvesCurrentRelativeArchivePath() {
        val root = Files.createTempDirectory("tijario-backup").toFile()
        val archive = File(root, "users/user-1/backups/current.tijario").apply {
            parentFile!!.mkdirs()
            writeText("archive")
        }

        assertEquals(
            archive.canonicalFile,
            resolveBackupArchiveFile(root, "user-1", "users/user-1/backups/current.tijario"),
        )
    }

    @Test
    fun recoversLegacyStoredPathUsingOnlyTheCurrentAccountsBackupDirectory() {
        val root = Files.createTempDirectory("tijario-backup").toFile()
        val archive = File(root, "users/user-1/backups/legacy.tijario").apply {
            parentFile!!.mkdirs()
            writeText("archive")
        }

        assertEquals(
            archive.canonicalFile,
            resolveBackupArchiveFile(
                root,
                "user-1",
                "../../../../data/app.tijario/files/users/user-1/backups/legacy.tijario",
            ),
        )
    }

    @Test
    fun rejectsNonArchiveAndPathTraversalWithoutARecoverableArchive() {
        val root = Files.createTempDirectory("tijario-backup").toFile()

        assertNull(resolveBackupArchiveFile(root, "user-1", "../../outside.txt"))
        assertNull(resolveBackupArchiveFile(root, "user-1", "../../outside.tijario"))
    }
}
