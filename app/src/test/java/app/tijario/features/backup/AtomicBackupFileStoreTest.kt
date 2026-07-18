package app.tijario.features.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class AtomicBackupFileStoreTest {
    @Test
    fun verifiedArchiveIsFinalizedWithoutLeavingTemporaryFile() {
        val directory = Files.createTempDirectory("tijario-backup-test").toFile()
        val archive = "encrypted-archive".encodeToByteArray()

        val stored = AtomicBackupFileStore.writeVerified(
            directory,
            "Tijario-Backup-20260718-1200-abc12345.tijario",
            archive,
        ) { assertArrayEquals(archive, it) }

        assertTrue(stored.file.isFile)
        assertArrayEquals(archive, stored.file.readBytes())
        assertFalse(directory.listFiles().orEmpty().any { it.name.endsWith(".tmp") })
        directory.deleteRecursively()
    }

    @Test
    fun verificationFailureLeavesExistingFinalBackupUntouched() {
        val directory = Files.createTempDirectory("tijario-backup-test").toFile()
        val fileName = "Tijario-Backup-20260718-1200-abc12345.tijario"
        val existing = directory.resolve(fileName).apply { writeText("previous") }

        assertThrows(BackupValidationException::class.java) {
            AtomicBackupFileStore.writeVerified(directory, fileName, "new".encodeToByteArray()) {
                error("verification failed")
            }
        }

        assertArrayEquals("previous".encodeToByteArray(), existing.readBytes())
        assertFalse(directory.listFiles().orEmpty().any { it.name.endsWith(".tmp") })
        directory.deleteRecursively()
    }
}
