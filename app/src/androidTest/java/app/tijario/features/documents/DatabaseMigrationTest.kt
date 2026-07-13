package app.tijario.features.documents

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.tijario.data.local.BigDecimalConverter
import app.tijario.data.local.TijarioDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TijarioDatabase::class.java
    )

    @Test
    fun migrateVersion12To13AddsCreatedAtWithoutLosingDocuments() {
        val db12 = helper.createDatabase(TEST_DB, 12)
        db12.execSQL(
            """
            INSERT INTO documents_cache (
                id, user_id, customer_id, type, document_number, status, issue_date,
                tax_rate, tax_amount, document_language, total, currency, synced_at,
                subtotal, discount, extra_fees, sync_status, local_revision, is_deleted
            ) VALUES (
                'doc-created-at', 'user-1', 'customer-1', 'invoice', 'INV-13', 'draft', '2026-07-12',
                '0.0', '0.0', 'ar', '10.0', 'SAR', 1,
                '10.0', '0.0', '0.0', 'SYNCED', 1, 0
            )
            """.trimIndent()
        )
        db12.close()

        val db13 = helper.runMigrationsAndValidate(
            TEST_DB,
            13,
            true,
            TijarioDatabase.MIGRATION_12_13,
        )
        val cursor = db13.query("SELECT id, created_at FROM documents_cache WHERE id = 'doc-created-at'")

        assertTrue(cursor.moveToFirst())
        assertEquals("doc-created-at", cursor.getString(cursor.getColumnIndexOrThrow("id")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("created_at")))
        cursor.close()
        db13.close()
    }

    @Test
    fun migrateVersion13To14AddsLocalDocumentOptionFields() {
        val db13 = helper.createDatabase(TEST_DB, 13)
        db13.execSQL(
            """
            INSERT INTO local_document_metadata (
                documentId, currency, signature_data, payment_method, tax_rate, tax_name
            ) VALUES (
                'doc-options', 'SAR', NULL, 'cash', 15.0, 'VAT'
            )
            """.trimIndent()
        )
        db13.close()

        val db14 = helper.runMigrationsAndValidate(
            TEST_DB,
            14,
            true,
            TijarioDatabase.MIGRATION_13_14,
        )
        val cursor = db14.query(
            """
            SELECT discount_type, discount_value, shipping_amount, shipping_label
            FROM local_document_metadata
            WHERE documentId = 'doc-options'
            """.trimIndent()
        )

        assertTrue(cursor.moveToFirst())
        assertEquals("fixed", cursor.getString(cursor.getColumnIndexOrThrow("discount_type")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("discount_value")))
        assertEquals(0.0, cursor.getDouble(cursor.getColumnIndexOrThrow("shipping_amount")), 0.0)
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("shipping_label")))
        cursor.close()
        db14.close()
    }

    @Test
    fun migrateVersion6To7PreservesAllDataAndConvertsPricesToCanonicalStrings() {
        // 1. Create version 6 database
        val db6 = helper.createDatabase(TEST_DB, 6)

        // 2. Insert rows for user 1 (Arabic data, floats, etc.)
        val user1 = "user_1_uuid"
        val user2 = "user_2_uuid"

        // Insert business settings V6
        val settingsValues = ContentValues().apply {
            put("user_id", user1)
            put("remote_id", "bs_remote_1")
            put("business_name", "محل تجاريو التجريبي")
            put("whatsapp_number", "9665000000")
            put("country", "Saudi Arabia")
            put("currency", "SAR")
            put("synced_at", 123456789L)
        }
        db6.insert("business_settings_cache", SQLiteDatabase.CONFLICT_REPLACE, settingsValues)

        // Insert customers V6
        val customerValues = ContentValues().apply {
            put("id", "cust_1")
            put("user_id", user1)
            put("name", "أحمد سالم")
            put("whatsapp_number", "9665111111")
            put("synced_at", 123456789L)
        }
        db6.insert("customers_cache", SQLiteDatabase.CONFLICT_REPLACE, customerValues)

        // Insert products V6 (Double values representing prices with float representation drift)
        val productValues = ContentValues().apply {
            put("id", "prod_1")
            put("user_id", user1)
            put("kind", "product")
            put("name", "خدمة تصميم سحابية")
            put("price", 199.99)
            put("currency", "SAR")
            put("synced_at", 123456789L)
        }
        db6.insert("products_cache", SQLiteDatabase.CONFLICT_REPLACE, productValues)

        // Insert documents V6 (Double total, nullable payment_status, float drift)
        val docValues = ContentValues().apply {
            put("id", "doc_1")
            put("user_id", user1)
            put("customer_id", "cust_1")
            put("type", "invoice")
            put("document_number", "INV-0001")
            put("status", "draft")
            put("payment_status", null as String?)
            put("amount_paid", 0.0)
            put("issue_date", "2026-06-20")
            put("total", 1000.5)
            put("currency", "SAR")
            put("synced_at", 123456789L)
        }
        db6.insert("documents_cache", SQLiteDatabase.CONFLICT_REPLACE, docValues)

        db6.close()

        // 3. Migrate database 6 -> 7
        val db7 = helper.runMigrationsAndValidate(TEST_DB, 7, true, TijarioDatabase.MIGRATION_6_7)

        // 4. Verify migrated records in Room V7
        verifyBusinessSettings(db7, user1)
        verifyCustomer(db7, user1)
        verifyProduct(db7, user1)
        verifyDocument(db7, user1)

        db7.close()
    }

    private fun verifyBusinessSettings(db: SupportSQLiteDatabase, userId: String) {
        val cursor = db.query("SELECT * FROM business_settings_cache WHERE user_id = ?", arrayOf(userId))
        assertTrue("Business settings row should exist", cursor.moveToFirst())
        assertEquals("محل تجاريو التجريبي", cursor.getString(cursor.getColumnIndexOrThrow("business_name")))
        assertEquals("SYNCED", cursor.getString(cursor.getColumnIndexOrThrow("sync_status")))
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("local_revision")))
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("server_revision")))
        cursor.close()
    }

    private fun verifyCustomer(db: SupportSQLiteDatabase, userId: String) {
        val cursor = db.query("SELECT * FROM customers_cache WHERE user_id = ?", arrayOf(userId))
        assertTrue("Customer row should exist", cursor.moveToFirst())
        assertEquals("أحمد سالم", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        assertEquals("SYNCED", cursor.getString(cursor.getColumnIndexOrThrow("sync_status")))
        cursor.close()
    }

    private fun verifyProduct(db: SupportSQLiteDatabase, userId: String) {
        val cursor = db.query("SELECT * FROM products_cache WHERE user_id = ?", arrayOf(userId))
        assertTrue("Product row should exist", cursor.moveToFirst())
        assertEquals("خدمة تصميم سحابية", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        val priceStr = cursor.getString(cursor.getColumnIndexOrThrow("price"))
        assertEquals("199.99", priceStr) // Canonical Plain Decimal String
        assertEquals(BigDecimal("199.99"), BigDecimal(priceStr))
        assertEquals("SYNCED", cursor.getString(cursor.getColumnIndexOrThrow("sync_status")))
        cursor.close()
    }

    private fun verifyDocument(db: SupportSQLiteDatabase, userId: String) {
        val cursor = db.query("SELECT * FROM documents_cache WHERE user_id = ?", arrayOf(userId))
        assertTrue("Document row should exist", cursor.moveToFirst())
        assertEquals("INV-0001", cursor.getString(cursor.getColumnIndexOrThrow("document_number")))

        val totalStr = cursor.getString(cursor.getColumnIndexOrThrow("total"))
        assertEquals("1000.5", totalStr) // Canonical Plain Decimal String representation
        assertEquals(BigDecimal("1000.5"), BigDecimal(totalStr))

        val amountPaidStr = cursor.getString(cursor.getColumnIndexOrThrow("amount_paid"))
        assertEquals("0.0", amountPaidStr)

        // Nullable test
        assertTrue(cursor.isNull(cursor.getColumnIndexOrThrow("payment_status")))

        // V7 columns check
        assertEquals("0.0", cursor.getString(cursor.getColumnIndexOrThrow("subtotal")))
        assertEquals("0.0", cursor.getString(cursor.getColumnIndexOrThrow("discount")))
        assertEquals("0.0", cursor.getString(cursor.getColumnIndexOrThrow("extra_fees")))

        assertEquals("SYNCED", cursor.getString(cursor.getColumnIndexOrThrow("sync_status")))
        cursor.close()
    }
}
