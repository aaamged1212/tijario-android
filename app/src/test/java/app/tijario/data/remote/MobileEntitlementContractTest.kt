package app.tijario.data.remote

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileEntitlementContractTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val apiJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun productionSerializerAcceptsEveryEntitlementResponseFixture() {
        val raw = checkNotNull(
            javaClass.classLoader?.getResourceAsStream("fixtures/mobile-entitlement-responses.json"),
        ) { "Mobile entitlement fixture is missing" }.bufferedReader().use { it.readText() }
        val cases = apiJson.parseToJsonElement(raw).jsonObject.getValue("cases").jsonArray
        val expectedNames = listOf(
            "new_free_local_drive",
            "existing_free_local_drive",
            "paid_local_drive",
            "existing_legacy_cloud",
            "missing_usage_row",
            "missing_account_usage_totals",
            "missing_device_registration",
            "device_seen_under_24h",
            "device_seen_over_24h",
            "expired_or_invalid_plan",
            "grace_period",
            "cancelled_until_period_end",
        )

        assertEquals(expectedNames, cases.map { it.jsonObject.getValue("name").jsonPrimitive.content })
        cases.forEach { fixtureCase ->
            val name = fixtureCase.jsonObject.getValue("name").jsonPrimitive.content
            val response = apiJson.decodeFromJsonElement(
                AccountUsageResponse.serializer(),
                fixtureCase.jsonObject.getValue("response"),
            )

            assertTrue(name, response.ok)
            assertNotNull(name, response.data)
            assertNotNull(name, response.data?.signedEntitlement)
            assertTrue(name, response.data?.allowedTemplateIds?.isNotEmpty() == true)
        }
    }
}
