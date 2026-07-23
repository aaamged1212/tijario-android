package app.tijario.domain

import android.content.Context
import app.tijario.R

/** The committed public key allows entitlement verification without local Gradle configuration. */
object ProductionEntitlementKeyRegistry {
    const val KEY_ID = "tijario-entitlement-prod-2026-v1"
    const val PUBLIC_KEY_SHA256 = "761de204abebb9717778481e90f42326fdf78f2f911083b58f7c8afda54650fd"

    fun verifier(context: Context): EntitlementVerifier {
        val keyBase64 = context.resources
            .openRawResource(R.raw.entitlement_signing_public_key_base64)
            .bufferedReader(Charsets.US_ASCII)
            .use { it.readText().trim() }
        require(EntitlementVerifier.publicKeyFingerprint(keyBase64) == PUBLIC_KEY_SHA256) {
            "ENTITLEMENT_PUBLIC_KEY_FINGERPRINT_MISMATCH"
        }
        return EntitlementVerifier.fromBase64PublicKeys(mapOf(KEY_ID to keyBase64))
    }
}
