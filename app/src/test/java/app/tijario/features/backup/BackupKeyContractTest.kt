package app.tijario.features.backup

import app.tijario.data.remote.BackupKeyEnvelopeRequest
import app.tijario.data.remote.BackupKeyEnvelopeResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupKeyContractTest {
    @Test
    fun requestUsesInstallationAndDevicePublicKeyOnly() {
        val encoded = Json.encodeToString(
            BackupKeyEnvelopeRequest(
                installationId = "installation-1",
                devicePublicKeySpki = "public-key",
                keyVersion = 2,
            ),
        )

        assertTrue(encoded.contains("\"installation_id\""))
        assertTrue(encoded.contains("\"device_public_key_spki\""))
        assertTrue(encoded.contains("\"key_version\":2"))
        assertFalse(encoded.contains("user_id"))
        assertFalse(encoded.contains("private"))
    }

    @Test
    fun responseParsesWrappedKeyWithoutPlaintextKeyField() {
        val response = Json.decodeFromString<BackupKeyEnvelopeResponse>(
            """{"key_version":2,"wrapped_key":"ciphertext","wrapping_algorithm":"RSA-OAEP-256"}""",
        )

        assertEquals(2, response.keyVersion)
        assertEquals("ciphertext", response.wrappedKey)
        assertEquals("RSA-OAEP-256", response.wrappingAlgorithm)
    }

    @Test
    fun deviceKeyImplementationUsesAndroidKeystoreAndNeverLogsKeys() {
        val source = File("src/main/java/app/tijario/features/backup/DeviceBackupKeyStore.kt").readText()

        assertTrue(source.contains("AndroidKeyStore"))
        assertTrue(source.contains("KeyProperties.PURPOSE_DECRYPT"))
        assertTrue(source.contains("RSA/ECB/OAEPPadding"))
        assertTrue(source.contains("BACKUP_INSTALLATION_NOT_REGISTERED"))
        assertFalse(source.contains("BACKUP_DEVICE_NOT_PRIMARY"))
        assertTrue(source.contains("key.fill(0)"))
        assertFalse(source.contains("Log."))
        assertFalse(source.contains("println("))
    }
}
