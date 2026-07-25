package app.tijario.features.backup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import app.tijario.data.remote.BackupKeyEnvelopeRequest
import app.tijario.data.remote.BackendApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

data class ResolvedBackupKey(
    val keyVersion: Int,
    val keyBytes: ByteArray,
)

class BackupKeyException(
    val code: String,
    cause: Throwable? = null,
) : Exception(code, cause)

internal fun backupMessageKeyFor(error: Throwable, fallback: String = "backup_create_failed"): String = when (
    (error as? BackupKeyException)?.code?.uppercase()
) {
    "BACKUP_DEVICE_NOT_PRIMARY" -> "backup_device_not_primary"
    "BACKUP_KEY_UNAVAILABLE" -> "backup_key_unavailable"
    "BACKUP_DEVICE_KEY_INVALID" -> "backup_device_key_invalid"
    "SERVER_CONFIGURATION_ERROR" -> "backup_server_configuration_error"
    "UNAUTHENTICATED" -> "error_session_expired"
    "OFFLINE_KEY_UNAVAILABLE" -> "backup_create_failed"
    else -> fallback
}

@Serializable
private data class CachedWrappedBackupKey(
    val keyVersion: Int,
    val wrappedKey: String,
    val wrappingAlgorithm: String,
)

class DeviceBackupKeyStore(
    private val context: Context,
    private val backendApiClient: BackendApiClient,
) {
    suspend fun resolve(
        userId: String,
        installationId: String,
        allowNetwork: Boolean,
        keyVersion: Int? = null,
    ): ResolvedBackupKey =
        withContext(Dispatchers.IO) {
            require(userId.isNotBlank() && installationId.isNotBlank()) { "Backup identity is required" }
            val keyPair = getOrCreateKeyPair(userId, installationId)
            val cached = readCached(userId, keyVersion)
            if (cached != null) {
                return@withContext try {
                    unwrap(cached, keyPair.private)
                } catch (error: BackupValidationException) {
                    throw BackupKeyException("BACKUP_DEVICE_KEY_INVALID", error)
                }
            }
            if (allowNetwork) {
                val online = try {
                    backendApiClient.resolveBackupKeyEnvelope(
                        BackupKeyEnvelopeRequest(
                            installationId = installationId,
                            devicePublicKeySpki = Base64.getEncoder().encodeToString(keyPair.public.encoded),
                            keyVersion = keyVersion,
                        ),
                    )
                } catch (error: java.io.IOException) {
                    throw BackupKeyException("OFFLINE_KEY_UNAVAILABLE", error)
                }
                if (!online.ok) throw BackupKeyException(online.code ?: "BACKUP_KEY_UNAVAILABLE")
                val envelope = online.data ?: throw BackupKeyException("BACKUP_KEY_UNAVAILABLE")
                val cachedEnvelope = CachedWrappedBackupKey(
                    keyVersion = envelope.keyVersion,
                    wrappedKey = envelope.wrappedKey,
                    wrappingAlgorithm = envelope.wrappingAlgorithm,
                )
                val resolved = try {
                    unwrap(cachedEnvelope, keyPair.private)
                } catch (error: BackupValidationException) {
                    throw BackupKeyException("BACKUP_DEVICE_KEY_INVALID", error)
                }
                writeCached(userId, cachedEnvelope)
                return@withContext resolved
            }

            throw BackupKeyException("OFFLINE_KEY_UNAVAILABLE")
        }

    private fun getOrCreateKeyPair(userId: String, installationId: String): java.security.KeyPair {
        val alias = alias(userId, installationId)
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existingPrivate = keyStore.getKey(alias, null) as? java.security.PrivateKey
        val existingPublic = keyStore.getCertificate(alias)?.publicKey
        if (existingPrivate != null && existingPublic != null) return java.security.KeyPair(existingPublic, existingPrivate)

        val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE)
        generator.initialize(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(2048)
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build(),
        )
        return generator.generateKeyPair()
    }

    private fun unwrap(cached: CachedWrappedBackupKey, privateKey: java.security.PrivateKey): ResolvedBackupKey {
        if (cached.wrappingAlgorithm != WRAPPING_ALGORITHM || cached.keyVersion <= 0) {
            throw BackupValidationException("Backup key envelope is unsupported")
        }
        val wrapped = try {
            Base64.getDecoder().decode(cached.wrappedKey)
        } catch (error: IllegalArgumentException) {
            throw BackupValidationException("Backup key envelope is invalid", error)
        }
        val key = try {
            Cipher.getInstance("RSA/ECB/OAEPPadding").run {
                init(
                    Cipher.DECRYPT_MODE,
                    privateKey,
                    OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT),
                )
                doFinal(wrapped)
            }
        } catch (error: Exception) {
            throw BackupValidationException("Backup key envelope could not be opened", error)
        }
        if (key.size != 32) {
            key.fill(0)
            throw BackupValidationException("Backup key length is invalid")
        }
        return ResolvedBackupKey(cached.keyVersion, key)
    }

    private fun cacheDirectory(userId: String): File =
        File(context.filesDir, "users/$userId/backup/keys")

    private fun readCached(userId: String, keyVersion: Int?): CachedWrappedBackupKey? {
        val file = if (keyVersion == null) {
            File(cacheDirectory(userId), "latest.json")
        } else {
            File(cacheDirectory(userId), "v$keyVersion.json")
        }
        if (!file.isFile) return null
        return try {
            Json.decodeFromString<CachedWrappedBackupKey>(file.readText())
        } catch (_: Exception) {
            file.delete()
            null
        }
    }

    private fun writeCached(userId: String, envelope: CachedWrappedBackupKey) {
        val encoded = Json.encodeToString(CachedWrappedBackupKey.serializer(), envelope)
        writeCacheFile(File(cacheDirectory(userId), "v${envelope.keyVersion}.json"), encoded)
        writeCacheFile(File(cacheDirectory(userId), "latest.json"), encoded)
    }

    private fun writeCacheFile(file: File, encoded: String) {
        requireNotNull(file.parentFile).mkdirs()
        val temporary = File(file.parentFile, ".${file.name}.tmp")
        temporary.writeText(encoded)
        if (!temporary.renameTo(file)) {
            temporary.copyTo(file, overwrite = true)
            temporary.delete()
        }
    }

    private fun alias(userId: String, installationId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$userId:$installationId".encodeToByteArray())
            .joinToString("") { "%02x".format(it) }
        return "tijario_backup_${digest.take(32)}"
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val WRAPPING_ALGORITHM = "RSA-OAEP-256"
    }
}
