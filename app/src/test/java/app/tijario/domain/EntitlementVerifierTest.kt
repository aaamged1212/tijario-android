package app.tijario.domain

import app.tijario.data.remote.SignedEntitlementDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Signature
import java.time.Instant
import java.util.Base64

class EntitlementVerifierTest {
    private lateinit var keyPair: KeyPair
    private val now = Instant.parse("2026-07-18T12:00:00Z")

    @Before
    fun setUp() {
        keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    }

    @Test
    fun validCanonicalEntitlement_verifiesAccountDeviceAndLimits() {
        val payload = payload()
        val envelope = sign(payload)

        val result = verifier().verify(envelope, USER_ID, INSTALLATION_ID).getOrThrow()

        assertEquals("local_drive", result.dataMode)
        assertEquals(5, result.documentLimit)
        assertEquals(listOf("tijario-classic"), result.allowedTemplateIds)
    }

    @Test
    fun signedEntitlementOmitsLegacyPrimaryDeviceFields() {
        val encoded = Json.encodeToString(SignedEntitlementPayload.serializer(), payload())

        assertFalse(encoded.contains("max_primary_devices"))
        assertFalse(encoded.contains("primary_device_id"))
    }

    @Test
    fun tamperedPayloadSignatureAndUnknownKeyAreRejected() {
        val envelope = sign(payload())
        val tampered = envelope.copy(
            payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(Base64.getUrlDecoder().decode(envelope.payload).plus(32.toByte())),
        )
        val alteredSignature = envelope.copy(
            signature = if (envelope.signature.first() == 'A') {
                "B${envelope.signature.drop(1)}"
            } else {
                "A${envelope.signature.drop(1)}"
            },
        )

        assertTrue(verifier().verify(tampered, USER_ID, INSTALLATION_ID).isFailure)
        assertTrue(verifier().verify(alteredSignature, USER_ID, INSTALLATION_ID).isFailure)
        assertTrue(verifier().verify(envelope.copy(keyId = "unknown"), USER_ID, INSTALLATION_ID).isFailure)
    }

    @Test
    fun expiredOrWrongAccountEntitlementIsRejected() {
        val expired = sign(payload().copy(expiresAt = "2026-07-18T11:59:59Z"))
        val valid = sign(payload())

        assertTrue(verifier().verify(expired, USER_ID, INSTALLATION_ID).isFailure)
        assertTrue(verifier().verify(valid, "another-user", INSTALLATION_ID).isFailure)
        assertTrue(verifier().verify(valid, USER_ID, "another-installation").isFailure)
    }

    @Test
    fun base64ConfigurationSelectsPublicKeyByKeyId() {
        val configJson = Json.encodeToString(mapOf(KEY_ID to Base64.getEncoder().encodeToString(keyPair.public.encoded)))
        val encodedConfig = Base64.getEncoder().encodeToString(configJson.toByteArray())

        val result = EntitlementVerifier.fromBase64Configuration(encodedConfig)
            .verify(sign(payload()), USER_ID, INSTALLATION_ID)

        assertTrue(result.isSuccess)
    }

    @Test
    fun productionRegistryUsesTheExpectedCommittedPublicKey() {
        val publicKey = appModuleDirectory()
            .resolve("src/main/res/raw/entitlement_signing_public_key_base64.txt")
            .readText()
            .trim()
        val verifier = EntitlementVerifier.fromBase64PublicKeys(
            mapOf(ProductionEntitlementKeyRegistry.KEY_ID to publicKey),
        )

        assertEquals("tijario-entitlement-prod-2026-v1", ProductionEntitlementKeyRegistry.KEY_ID)
        assertEquals(
            ProductionEntitlementKeyRegistry.PUBLIC_KEY_SHA256,
            EntitlementVerifier.publicKeyFingerprint(publicKey),
        )
        assertTrue(EntitlementVerifier.hasTrustedKey(verifier, ProductionEntitlementKeyRegistry.KEY_ID))
    }

    @Test
    fun malformedInvalidAndWeakTrustedKeysAreRejected() {
        assertConfigurationRejected("not-base64")
        assertConfigurationRejected(Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3)))

        val ecPublicKey = KeyPairGenerator.getInstance("EC").apply { initialize(256) }
            .generateKeyPair()
            .public
        assertConfigurationRejected(Base64.getEncoder().encodeToString(ecPublicKey.encoded))

        val weakRsaPublicKey = KeyPairGenerator.getInstance("RSA").apply { initialize(1024) }
            .generateKeyPair()
            .public
        assertConfigurationRejected(Base64.getEncoder().encodeToString(weakRsaPublicKey.encoded))
    }

    @Test
    fun androidMainSourcesContainNoPrivateEntitlementOrBackupKey() {
        val forbiddenMarkers = listOf(
            "BEGIN PRIVATE KEY",
            "ENTITLEMENT_SIGNING_PRIVATE_KEY_BASE64",
            "BACKUP_KEY_ENCRYPTION_KEY",
        )
        val sourceFiles = appModuleDirectory().resolve("src/main").walkTopDown()
            .filter { it.isFile && it.extension in setOf("kt", "xml", "txt") }
            .toList()

        assertTrue(sourceFiles.isNotEmpty())
        assertTrue(sourceFiles.none { file -> forbiddenMarkers.any { marker -> file.readTextContains(marker) } })
    }

    private fun verifier() = EntitlementVerifier(mapOf(KEY_ID to keyPair.public)) { now.toEpochMilli() }

    private fun assertConfigurationRejected(keyBase64: String) {
        assertTrue(
            runCatching {
                EntitlementVerifier.fromBase64PublicKeys(mapOf(KEY_ID to keyBase64))
            }.isFailure,
        )
    }

    private fun File.readTextContains(marker: String): Boolean = readText().contains(marker)

    private fun appModuleDirectory(): File {
        val workingDirectory = File(System.getProperty("user.dir") ?: error("Working directory is unavailable"))
        return listOf(workingDirectory, workingDirectory.resolve("app"))
            .firstOrNull { it.resolve("src/main").isDirectory }
            ?: error("Android app module directory is unavailable")
    }

    private fun sign(payload: SignedEntitlementPayload): SignedEntitlementDto {
        val element = Json.encodeToJsonElement(SignedEntitlementPayload.serializer(), payload)
        val canonical = EntitlementVerifier.canonicalJson(element)
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(keyPair.private)
        signer.update(canonical.toByteArray())
        return SignedEntitlementDto(
            algorithm = "RS256",
            keyId = KEY_ID,
            payload = Base64.getUrlEncoder().withoutPadding().encodeToString(canonical.toByteArray()),
            signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign()),
        )
    }

    private fun payload() = SignedEntitlementPayload(
        allowedTemplateIds = listOf("tijario-classic"),
        backupFrequency = "manual",
        backupRetentionDaily = 1,
        backupRetentionMonthly = 1,
        backupRetentionWeekly = 1,
        customerLimit = 5,
        dataMode = "local_drive",
        documentLimit = 5,
        documentLimitScope = "lifetime",
        documentsUsed = 1,
        entitlementVersion = 3,
        expiresAt = "2099-07-19T12:00:00Z",
        installationId = INSTALLATION_ID,
        issuedAt = "2020-01-01T00:00:00Z",
        keyId = KEY_ID,
        offlineCreditBatchSize = 2,
        offlineEntitlementDays = 7,
        planCode = "free",
        productLimit = 5,
        removeTijarioBranding = false,
        userId = USER_ID,
    )

    private companion object {
        const val KEY_ID = "test-key"
        const val USER_ID = "11111111-1111-1111-1111-111111111111"
        const val INSTALLATION_ID = "test-installation"
    }
}
