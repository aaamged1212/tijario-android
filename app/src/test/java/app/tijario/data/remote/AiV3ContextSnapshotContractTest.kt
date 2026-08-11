package app.tijario.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test

class AiV3ContextSnapshotContractTest {
    private val json = Json { encodeDefaults = true }

    @Test
    fun replyRequest_serializesLocalContextSnapshotWithoutSensitiveIdentityFields() {
        val request = AiV3ReplyRequest(
            clientRequestId = "request-1",
            customerMessage = "Need a reply",
            customerId = "local-customer",
            productId = "local-product",
            contextSnapshot = AiV3ContextSnapshot(
                business = AiV3BusinessContextSnapshot(name = "Store", country = "YE", currency = "YER"),
                customer = AiV3CustomerContextSnapshot(localId = "local-customer", name = "Customer", city = "Sanaa"),
                product = AiV3ProductContextSnapshot(localId = "local-product", name = "Item", price = 15.0, currency = "YER"),
            ),
        )

        val encoded = json.encodeToString(AiV3ReplyRequest.serializer(), request)

        assertTrue(encoded.contains("\"context_snapshot\""))
        assertTrue(encoded.contains("\"customer_id\":\"local-customer\""))
        assertTrue(encoded.contains("\"product_id\":\"local-product\""))
        assertTrue(encoded.contains("\"currency\":\"YER\""))
        assertTrue(!encoded.contains("whatsapp"))
        assertTrue(!encoded.contains("email"))
    }
}
