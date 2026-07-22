package app.tijario.domain

import app.tijario.data.remote.SignedEntitlementDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64

@Serializable
data class SignedEntitlementPayload(
    @SerialName("allowed_template_ids") val allowedTemplateIds: List<String>,
    @SerialName("backup_frequency") val backupFrequency: String,
    @SerialName("backup_retention_daily") val backupRetentionDaily: Int,
    @SerialName("backup_retention_monthly") val backupRetentionMonthly: Int,
    @SerialName("backup_retention_weekly") val backupRetentionWeekly: Int,
    @SerialName("customer_limit") val customerLimit: Int? = null,
    @SerialName("data_mode") val dataMode: String,
    @SerialName("document_limit") val documentLimit: Int,
    @SerialName("document_limit_scope") val documentLimitScope: String,
    @SerialName("documents_used") val documentsUsed: Int,
    @SerialName("entitlement_version") val entitlementVersion: Long,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("installation_id") val installationId: String,
    @SerialName("issued_at") val issuedAt: String,
    @SerialName("key_id") val keyId: String,
    @SerialName("max_primary_devices") val maxPrimaryDevices: Int,
    @SerialName("offline_credit_batch_size") val offlineCreditBatchSize: Int,
    @SerialName("offline_entitlement_days") val offlineEntitlementDays: Int,
    @SerialName("plan_code") val planCode: String,
    @SerialName("product_limit") val productLimit: Int? = null,
    @SerialName("remove_tijario_branding") val removeTijarioBranding: Boolean,
    @SerialName("user_id") val userId: String,
)

class EntitlementVerifier(
    private val publicKeys: Map<String, PublicKey>,
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    fun verify(
        envelope: SignedEntitlementDto,
        expectedUserId: String,
        expectedInstallationId: String,
    ): Result<SignedEntitlementPayload> = runCatching {
        require(envelope.algorithm == "RS256") { "ENTITLEMENT_ALGORITHM_INVALID" }
        val key = publicKeys[envelope.keyId] ?: error("ENTITLEMENT_KEY_UNKNOWN")
        val payloadBytes = Base64.getUrlDecoder().decode(envelope.payload)
        val signatureBytes = Base64.getUrlDecoder().decode(envelope.signature)
        val payloadText = payloadBytes.toString(Charsets.UTF_8)
        val jsonElement = json.parseToJsonElement(payloadText)
        require(canonicalJson(jsonElement) == payloadText) { "ENTITLEMENT_PAYLOAD_NOT_CANONICAL" }

        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(key)
        verifier.update(payloadBytes)
        require(verifier.verify(signatureBytes)) { "ENTITLEMENT_SIGNATURE_INVALID" }

        val payload = json.decodeFromString<SignedEntitlementPayload>(payloadText)
        require(payload.keyId == envelope.keyId) { "ENTITLEMENT_KEY_MISMATCH" }
        require(payload.userId == expectedUserId) { "ENTITLEMENT_ACCOUNT_MISMATCH" }
        require(payload.installationId == expectedInstallationId) { "ENTITLEMENT_DEVICE_MISMATCH" }
        require(Instant.parse(payload.expiresAt).toEpochMilli() > nowMillis()) { "ENTITLEMENT_EXPIRED" }
        require(Instant.parse(payload.issuedAt).toEpochMilli() <= nowMillis() + MAX_CLOCK_SKEW_MS) {
            "ENTITLEMENT_ISSUED_IN_FUTURE"
        }
        payload
    }

    companion object {
        private const val MAX_CLOCK_SKEW_MS = 5 * 60 * 1000L
        private val json = Json { ignoreUnknownKeys = false }

        fun fromBase64Configuration(encoded: String): EntitlementVerifier {
            if (encoded.isBlank()) return EntitlementVerifier(emptyMap())
            val decoded = Base64.getDecoder().decode(encoded).toString(Charsets.UTF_8)
            val keyMap = json.decodeFromString<Map<String, String>>(decoded)
            val factory = KeyFactory.getInstance("RSA")
            return EntitlementVerifier(
                keyMap.mapValues { (_, keyBase64) ->
                    factory.generatePublic(X509EncodedKeySpec(Base64.getDecoder().decode(keyBase64)))
                },
            )
        }

        fun payloadHash(value: String): String =
            MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }

        internal fun canonicalJson(element: JsonElement): String = when (element) {
            JsonNull -> "null"
            is JsonPrimitive -> element.toString()
            is JsonArray -> element.joinToString(prefix = "[", postfix = "]") { canonicalJson(it) }
            is JsonObject -> element.entries
                .sortedBy { it.key }
                .joinToString(prefix = "{", postfix = "}") { (key, value) ->
                    "${JsonPrimitive(key)}:${canonicalJson(value)}"
                }
        }
    }
}
