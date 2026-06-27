package app.tijario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.math.BigDecimal

@Database(
    entities = [
        BusinessSettingsEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        DocumentEntity::class,
        LocalTaxEntity::class,
        LocalPaymentMethodEntity::class,
        LocalSignatureEntity::class,
        LocalTermsEntity::class,
        LocalDocumentMetadataEntity::class,
        DocumentItemEntity::class,
        SyncStateEntity::class,
        SyncOutboxEntity::class,
        OfflineQuotaLeaseEntity::class,
        LocalUsageLedgerEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@TypeConverters(BigDecimalConverter::class)
abstract class TijarioDatabase : RoomDatabase() {
    abstract fun tijarioDao(): TijarioDao

    companion object {
        @Volatile
        private var instance: TijarioDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add sync metadata columns to business_settings_cache
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN local_revision INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN server_revision TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN server_updated_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN last_synced_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN sync_error_code TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")

                // 2. Add sync metadata columns to customers_cache
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'SYNCED'")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN local_revision INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN server_revision TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN server_updated_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN last_synced_at INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN sync_error_code TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE customers_cache ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")

                // 3. Migrate products_cache to support BigDecimal (TEXT) & sync metadata using shadow table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS products_cache_new (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        price TEXT NOT NULL,
                        currency TEXT NOT NULL,
                        stock_quantity INTEGER,
                        synced_at INTEGER NOT NULL,
                        sync_status TEXT NOT NULL DEFAULT 'LOCAL_ONLY',
                        local_revision INTEGER NOT NULL DEFAULT 1,
                        server_revision TEXT DEFAULT NULL,
                        server_updated_at INTEGER DEFAULT NULL,
                        last_synced_at INTEGER DEFAULT NULL,
                        sync_error_code TEXT DEFAULT NULL,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())

                val productsCursor = db.query("SELECT id, user_id, kind, name, description, price, currency, stock_quantity, synced_at FROM products_cache")
                try {
                    while (productsCursor.moveToNext()) {
                        val id = productsCursor.getString(0)
                        val userId = productsCursor.getString(1)
                        val kind = productsCursor.getString(2)
                        val name = productsCursor.getString(3)
                        val description = productsCursor.getString(4)
                        val priceDouble = productsCursor.getDouble(5)
                        val currency = productsCursor.getString(6)
                        val stockQuantity = if (productsCursor.isNull(7)) null else productsCursor.getInt(7)
                        val syncedAt = productsCursor.getLong(8)

                        val priceStr = BigDecimal.valueOf(priceDouble).toPlainString()

                        db.execSQL(
                            "INSERT INTO products_cache_new (id, user_id, kind, name, description, price, currency, stock_quantity, synced_at, sync_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED')",
                            arrayOf(id, userId, kind, name, description, priceStr, currency, stockQuantity, syncedAt)
                        )
                    }
                } finally {
                    productsCursor.close()
                }
                db.execSQL("DROP TABLE products_cache")
                db.execSQL("ALTER TABLE products_cache_new RENAME TO products_cache")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_cache_user_id ON products_cache (user_id)")

                // 4. Migrate documents_cache using shadow table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS documents_cache_new (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        customer_id TEXT NOT NULL,
                        type TEXT NOT NULL,
                        document_number TEXT NOT NULL,
                        status TEXT NOT NULL,
                        payment_status TEXT,
                        amount_paid TEXT,
                        issue_date TEXT NOT NULL,
                        total TEXT NOT NULL,
                        currency TEXT NOT NULL,
                        synced_at INTEGER NOT NULL,
                        subtotal TEXT NOT NULL DEFAULT '0.0',
                        discount TEXT NOT NULL DEFAULT '0.0',
                        extra_fees TEXT NOT NULL DEFAULT '0.0',
                        notes TEXT DEFAULT NULL,
                        terms_text TEXT DEFAULT NULL,
                        sync_status TEXT NOT NULL DEFAULT 'LOCAL_ONLY',
                        local_revision INTEGER NOT NULL DEFAULT 1,
                        server_revision TEXT DEFAULT NULL,
                        server_updated_at INTEGER DEFAULT NULL,
                        last_synced_at INTEGER DEFAULT NULL,
                        sync_error_code TEXT DEFAULT NULL,
                        is_deleted INTEGER NOT NULL DEFAULT 0,
                        local_pdf_relative_path TEXT DEFAULT NULL,
                        pdf_generated_at INTEGER DEFAULT NULL,
                        pdf_document_revision INTEGER DEFAULT NULL,
                        pdf_content_hash TEXT DEFAULT NULL,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())

                val docsCursor = db.query("SELECT id, user_id, customer_id, type, document_number, status, payment_status, amount_paid, issue_date, total, currency, synced_at FROM documents_cache")
                try {
                    while (docsCursor.moveToNext()) {
                        val id = docsCursor.getString(0)
                        val userId = docsCursor.getString(1)
                        val customerId = docsCursor.getString(2)
                        val type = docsCursor.getString(3)
                        val documentNumber = docsCursor.getString(4)
                        val status = docsCursor.getString(5)
                        val paymentStatus = docsCursor.getString(6)
                        val amountPaidDouble = if (docsCursor.isNull(7)) null else docsCursor.getDouble(7)
                        val issueDate = docsCursor.getString(8)
                        val totalDouble = docsCursor.getDouble(9)
                        val currency = docsCursor.getString(10)
                        val syncedAt = docsCursor.getLong(11)

                        val totalStr = BigDecimal.valueOf(totalDouble).toPlainString()
                        val amountPaidStr = amountPaidDouble?.let { BigDecimal.valueOf(it).toPlainString() }

                        db.execSQL(
                            "INSERT INTO documents_cache_new (id, user_id, customer_id, type, document_number, status, payment_status, amount_paid, issue_date, total, currency, synced_at, sync_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYNCED')",
                            arrayOf(id, userId, customerId, type, documentNumber, status, paymentStatus, amountPaidStr, issueDate, totalStr, currency, syncedAt)
                        )
                    }
                } finally {
                    docsCursor.close()
                }
                db.execSQL("DROP TABLE documents_cache")
                db.execSQL("ALTER TABLE documents_cache_new RENAME TO documents_cache")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_documents_cache_user_id ON documents_cache (user_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_documents_cache_user_id_document_number ON documents_cache (user_id, document_number)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_documents_cache_user_id_id ON documents_cache (user_id, id)")

                // 5. Create new V7 tables
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS document_items_cache (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        document_id TEXT NOT NULL,
                        product_id TEXT,
                        name TEXT NOT NULL,
                        description TEXT,
                        quantity INTEGER NOT NULL,
                        unit_price TEXT NOT NULL,
                        line_total TEXT NOT NULL,
                        sort_order INTEGER NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(user_id, document_id) REFERENCES documents_cache(user_id, id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_document_items_cache_user_id ON document_items_cache (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_document_items_cache_document_id ON document_items_cache (document_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_document_items_cache_product_id ON document_items_cache (product_id)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_state (
                        user_id TEXT NOT NULL,
                        opaque_cursor TEXT,
                        bootstrap_state TEXT NOT NULL,
                        last_successful_sync INTEGER,
                        sync_schema_version INTEGER NOT NULL,
                        PRIMARY KEY(user_id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_state_user_id ON sync_state (user_id)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS sync_outbox (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        operation TEXT NOT NULL,
                        idempotency_key TEXT NOT NULL,
                        base_server_revision TEXT,
                        status TEXT NOT NULL,
                        attempts INTEGER NOT NULL,
                        processing_started_at INTEGER,
                        lock_expires_at INTEGER,
                        last_error TEXT,
                        created_at INTEGER NOT NULL,
                        deleted_minimal_payload TEXT,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_user_id ON sync_outbox (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_status ON sync_outbox (status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sync_outbox_next_retry_at ON sync_outbox (next_retry_at)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_sync_outbox_idempotency_key ON sync_outbox (idempotency_key)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS offline_quota_lease (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        device_id TEXT NOT NULL,
                        plan_code TEXT NOT NULL,
                        period_month TEXT NOT NULL,
                        allowed_limit INTEGER NOT NULL,
                        consumed_count INTEGER NOT NULL,
                        expires_at INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_offline_quota_lease_user_id ON offline_quota_lease (user_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_offline_quota_lease_user_id_device_id_period_month ON offline_quota_lease (user_id, device_id, period_month)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS local_usage_ledger (
                        usage_event_id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        document_id TEXT NOT NULL,
                        operation_id TEXT NOT NULL,
                        lease_id TEXT NOT NULL,
                        period_month TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        synced_at INTEGER,
                        PRIMARY KEY(usage_event_id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_usage_ledger_user_id ON local_usage_ledger (user_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_local_usage_ledger_user_id_document_id ON local_usage_ledger (user_id, document_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_local_usage_ledger_user_id_operation_id ON local_usage_ledger (user_id, operation_id)")
            }
        }

        fun getInstance(context: Context): TijarioDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TijarioDatabase::class.java,
                    "tijario-local-cache.db",
                )
                    .addMigrations(MIGRATION_6_7)
                    .build()
                    .also { instance = it }
            }
    }
}
