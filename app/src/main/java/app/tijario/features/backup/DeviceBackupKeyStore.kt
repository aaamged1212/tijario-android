package app.tijario.features.backup

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import app.tijario.data.remote.BackupKeyEnvelopeRequest
import app.tijario.data.remote.BackendApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PSource

data class ResolvedBackupKey(
    val keyVersion: Int,
    val keyBytes: ByteArray,
)

class BackupKeyException(
    val code: String,
    cause: Throwable? = null,
) : Exception(code, cause)

/**
 * A device-wrapped key cache is disposable: the account key stays protected by the
 * server and can be wrapped again for this installation after an app/device key change.
 */
internal fun shouldRefreshInvalidCachedBackupKey(allowNetwork: Boolean): Boolean = allowNetwork

internal fun backupMessageKeyFor(error: Throwable, fallback: String = "backup_create_failed"): String = when (
    (error as? BackupKeyException)?.code?.uppercase()
) {
    "BACKUP_INSTALLATION_NOT_REGISTERED" -> "backup_installation_not_registered"
    "BACKUP_KEY_UNAVAILABLE" -> "backup_key_unavailable"
    "BACKUP_DEVICE_KEY_INVALID" -> "backup_device_key_invalid"
    "SERVER_CONFIGURATION_ERROR" -> "backup_server_configuration_error"
    "UNAUTHENTICATED" -> "error_session_expired"
    "OFFLINE_KEY_UNAVAILABLE" -> "backup_create_failed"
    else -> when (error) {
        is PhoneBackupDestinationException -> when (error.code) {
            PhoneBackupDestinationException.Code.DEFAULT_FOLDER_UNAVAILABLE -> "backup_phone_folder_required"
            PhoneBackupDestinationException.Code.PERMISSION_LOST -> "backup_phone_folder_permission_lost"
        }
        is BackupRestoreException -> when (error.code) {
            BackupRestoreException.Code.PRE_RESTORE_SAFETY_BACKUP_FAILED -> "backup_restore_safety_backup_failed"
            BackupRestoreException.Code.BACKUP_ACCOUNT_MISMATCH -> "backup_account_mismatch"
            BackupRestoreException.Code.BACKUP_HASH_MISMATCH -> "backup_hash_mismatch"
            BackupRestoreException.Code.BACKUP_DEVICE_KEY_INVALID -> "backup_device_key_invalid"
            BackupRestoreException.Code.BACKUP_KEY_VERSION_UNAVAILABLE -> "backup_key_unavailable"
            BackupRestoreException.Code.DRIVE_AUTH_REQUIRED -> "backup_drive_reauthorization_required"
            else -> "backup_restore_failed"
        }
        else -> fallback
    }
}

@Serializable
private data class CachedWrappedBackupKey(
    val keyVersion: Int,
    val wrappedKey: String,
    val wrappingAlgorithm: String,
)

@Serializable
private data class StoredDeviceKeyPair(
    val version: Int,
    val publicKeySpki: String,
    val encryptedPrivateKeyPkcs8: String,
)

/**
 * Android 13 and older keystore RSA keys only authorize SHA-1 for OAEP MGF1.
 * Keep the device RSA private key encrypted by an Android Keystore AES key instead,
 * so the server's RSA-OAEP-256 envelope remains usable from every supported device.
 */
internal object DeviceKeyMaterialCodec {
    private const val VERSION = "v1"
    private const val GCM_TAG_BITS = 128

    fun seal(privateKeyPkcs8: ByteArray, wrappingKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        // Android Keystore enforces random IV generation for encryption keys.
        cipher.init(Cipher.ENCRYPT_MODE, wrappingKey)
        val nonce = cipher.iv
        val ciphertext = cipher.doFinal(privateKeyPkcs8)
        return listOf(
            VERSION,
            Base64.getEncoder().encodeToString(nonce),
            Base64.getEncoder().encodeToString(ciphertext),
        ).joinToString(".")
    }

    fun open(envelope: String, wrappingKey: SecretKey): ByteArray {
        val pieces = envelope.split(".")
        if (pieces.size != 3 || pieces[0] != VERSION) {
            throw BackupValidationException("Device key envelope is unsupported")
        }
        val nonce = try {
            Base64.getDecoder().decode(pieces[1])
        } catch (error: IllegalArgumentException) {
            throw BackupValidationException("Device key envelope is invalid", error)
        }
        val ciphertext = try {
            Base64.getDecoder().decode(pieces[2])
        } catch (error: IllegalArgumentException) {
            throw BackupValidationException("Device key envelope is invalid", error)
        }
        return try {
            Cipher.getInstance("AES/GCM/NoPadding").run {
                init(Cipher.DECRYPT_MODE, wrappingKey, GCMParameterSpec(GCM_TAG_BITS, nonce))
                doFinal(ciphertext)
            }
        } catch (error: Exception) {
            throw BackupValidationException("Device key envelope could not be opened", error)
        }
    }
}

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
            val keyPair = try {
                getOrCreateKeyPair(userId, installationId)
            } catch (error: Exception) {
                logKeyFailure("device_key_material", error)
                throw BackupKeyException("BACKUP_DEVICE_KEY_INVALID", error)
            }
            val cached = readCached(userId, keyVersion)
            if (cached != null) {
                return@withContext try {
                    unwrap(cached, keyPair.private)
                } catch (error: BackupValidationException) {
                    clearCached(userId, cached.keyVersion)
                    if (shouldRefreshInvalidCachedBackupKey(allowNetwork)) {
                        resolveOnline(userId, installationId, keyVersion, keyPair)
                    } else {
                        throw BackupKeyException("BACKUP_DEVICE_KEY_INVALID", error)
                    }
                }
            }
            if (allowNetwork) {
                return@withContext resolveOnline(userId, installationId, keyVersion, keyPair)
            }

            throw BackupKeyException("OFFLINE_KEY_UNAVAILABLE")
        }

    private suspend fun resolveOnline(
        userId: String,
        installationId: String,
        keyVersion: Int?,
        keyPair: java.security.KeyPair,
    ): ResolvedBackupKey {
        val online = try {
            backendApiClient.resolveBackupKeyEnvelope(
                BackupKeyEnvelopeRequest(
                    installationId = installationId,
                    devicePublicKeySpki = Base64.getEncoder().encodeToString(keyPair.public.encoded),
                    keyVersion = keyVersion,
                ),
            )
        } catch (error: java.io.IOException) {
            logKeyFailure("key_envelope_request", error)
            throw BackupKeyException("OFFLINE_KEY_UNAVAILABLE", error)
        } catch (error: IllegalStateException) {
            logKeyFailure("key_envelope_request", error)
            throw BackupKeyException("UNAUTHENTICATED", error)
        } catch (error: Exception) {
            logKeyFailure("key_envelope_request", error)
            throw BackupKeyException("BACKUP_KEY_UNAVAILABLE", error)
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
        return resolved
    }

    private fun getOrCreateKeyPair(userId: String, installationId: String): KeyPair {
        val keyFile = deviceKeyFile(userId, installationId)
        val wrappingKey = try {
            getOrCreateWrappingKey(userId, installationId)
        } catch (error: Exception) {
            logKeyFailure("device_wrapping_key", error)
            throw error
        }
        if (keyFile.isFile) {
            try {
                val stored = Json.decodeFromString<StoredDeviceKeyPair>(keyFile.readText())
                if (stored.version == 1) {
                    val privateKeyBytes = DeviceKeyMaterialCodec.open(stored.encryptedPrivateKeyPkcs8, wrappingKey)
                    try {
                        val privateKey = KeyFactory.getInstance("RSA")
                            .generatePrivate(PKCS8EncodedKeySpec(privateKeyBytes))
                        val publicKey = KeyFactory.getInstance("RSA")
                            .generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(stored.publicKeySpki)))
                        return KeyPair(publicKey, privateKey)
                    } finally {
                        privateKeyBytes.fill(0)
                    }
                }
            } catch (_: Exception) {
                keyFile.delete()
            }
        }

        val keyPair = try {
            KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        } catch (error: Exception) {
            logKeyFailure("device_rsa_key_pair", error)
            throw error
        }
        val privateKeyPkcs8 = requireNotNull(keyPair.private.encoded) { "Device private key is unavailable" }
        val encryptedPrivateKey = try {
            DeviceKeyMaterialCodec.seal(privateKeyPkcs8, wrappingKey)
        } catch (error: Exception) {
            logKeyFailure("device_private_key_seal", error)
            throw error
        } finally {
            privateKeyPkcs8.fill(0)
        }
        val encoded = Json.encodeToString(
            StoredDeviceKeyPair.serializer(),
            StoredDeviceKeyPair(
                version = 1,
                publicKeySpki = Base64.getEncoder().encodeToString(keyPair.public.encoded),
                encryptedPrivateKeyPkcs8 = encryptedPrivateKey,
            ),
        )
        writeCacheFile(keyFile, encoded)
        return keyPair
    }

    private fun getOrCreateWrappingKey(userId: String, installationId: String): SecretKey {
        val alias = wrappingKeyAlias(userId, installationId)
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existing = keyStore.getKey(alias, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
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

    private fun clearCached(userId: String, keyVersion: Int) {
        File(cacheDirectory(userId), "v$keyVersion.json").delete()
        File(cacheDirectory(userId), "latest.json").delete()
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

    private fun deviceKeyFile(userId: String, installationId: String): File =
        File(cacheDirectory(userId), "device-${keyDigest(userId, installationId).take(32)}.json")

    private fun wrappingKeyAlias(userId: String, installationId: String): String =
        "tijario_backup_wrap_${keyDigest(userId, installationId).take(32)}"

    private fun keyDigest(userId: String, installationId: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$userId:$installationId".encodeToByteArray())
            .joinToString("") { "%02x".format(it) }
        return digest
    }

    /** Emits only the failure stage and exception class; backup keys and account data are never logged. */
    private fun logKeyFailure(stage: String, error: Exception) {
        Log.w(
            "TijarioBackup",
            "operation=backup_key stage=$stage exception=${error.javaClass.simpleName}",
        )
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val WRAPPING_ALGORITHM = "RSA-OAEP-256"
    }
}
