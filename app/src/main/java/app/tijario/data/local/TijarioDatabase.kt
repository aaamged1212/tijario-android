package app.tijario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.math.BigDecimal

const val TIJARIO_DATABASE_VERSION = 20

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
        DocumentCreationEventEntity::class,
        AccountEntitlementEntity::class,
        BackupSettingsEntity::class,
        BackupRecordEntity::class,
        BackupFileEntryEntity::class,
        DeviceBindingEntity::class,
        DeletedRecordEntity::class,
        AnnouncementEntity::class,
        AnnouncementReceiptOutboxEntity::class,
        AiGenerationHistoryEntity::class,
    ],
    version = TIJARIO_DATABASE_VERSION,
    exportSchema = true,
)
@TypeConverters(BigDecimalConverter::class)
abstract class TijarioDatabase : RoomDatabase() {
    abstract fun tijarioDao(): TijarioDao
    abstract fun notificationsDao(): NotificationsDao

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
                            arrayOf<Any?>(id, userId, kind, name, description, priceStr, currency, stockQuantity, syncedAt)
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
                            arrayOf<Any?>(id, userId, customerId, type, documentNumber, status, paymentStatus, amountPaidStr, issueDate, totalStr, currency, syncedAt)
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
                        next_retry_at INTEGER NOT NULL DEFAULT 0,
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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products_cache ADD COLUMN category TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS announcements_cache (
                        user_id TEXT NOT NULL,
                        id TEXT NOT NULL,
                        title_ar TEXT NOT NULL,
                        body_ar TEXT NOT NULL,
                        title_en TEXT NOT NULL,
                        body_en TEXT NOT NULL,
                        action_label_ar TEXT,
                        action_label_en TEXT,
                        deep_link TEXT,
                        priority INTEGER NOT NULL,
                        published_at TEXT,
                        expires_at TEXT,
                        is_read INTEGER NOT NULL,
                        is_seen INTEGER NOT NULL,
                        is_dismissed INTEGER NOT NULL,
                        last_synced_at INTEGER NOT NULL,
                        PRIMARY KEY(user_id, id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcements_cache_user_id ON announcements_cache (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcements_cache_user_id_published_at ON announcements_cache (user_id, published_at)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcements_cache_user_id_is_read ON announcements_cache (user_id, is_read)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcements_cache_user_id_is_dismissed ON announcements_cache (user_id, is_dismissed)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS announcement_receipt_outbox (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        announcement_id TEXT NOT NULL,
                        event TEXT NOT NULL,
                        opened_from TEXT,
                        status TEXT NOT NULL,
                        attempts INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        last_error TEXT,
                        PRIMARY KEY(id)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcement_receipt_outbox_user_id ON announcement_receipt_outbox (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcement_receipt_outbox_user_id_announcement_id ON announcement_receipt_outbox (user_id, announcement_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_announcement_receipt_outbox_status ON announcement_receipt_outbox (status)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN template_id TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN document_title TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN discount_label TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN extra_fees_label TEXT")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN tax_name TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN tax_rate TEXT NOT NULL DEFAULT '0.0'")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN tax_amount TEXT NOT NULL DEFAULT '0.0'")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN address TEXT")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN email TEXT")
                db.execSQL("ALTER TABLE business_settings_cache ADD COLUMN website_url TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN document_language TEXT NOT NULL DEFAULT 'ar'")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN created_at TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN discount_type TEXT NOT NULL DEFAULT 'fixed'")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN discount_value TEXT")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN shipping_amount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN shipping_label TEXT")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Legacy local-only rows have no owner. Removing them prevents cross-account disclosure.
                db.execSQL("DROP TABLE IF EXISTS local_taxes")
                db.execSQL("DROP TABLE IF EXISTS local_payment_methods")
                db.execSQL("DROP TABLE IF EXISTS local_signatures")
                db.execSQL("DROP TABLE IF EXISTS local_terms")
                db.execSQL("DROP TABLE IF EXISTS local_document_metadata")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_taxes (user_id TEXT NOT NULL, id TEXT NOT NULL, name TEXT NOT NULL, rate REAL NOT NULL, PRIMARY KEY(user_id, id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_taxes_user_id ON local_taxes (user_id)")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_payment_methods (user_id TEXT NOT NULL, id TEXT NOT NULL, name TEXT NOT NULL, details TEXT, PRIMARY KEY(user_id, id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_payment_methods_user_id ON local_payment_methods (user_id)")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_signatures (user_id TEXT NOT NULL, id TEXT NOT NULL, name TEXT NOT NULL, signature_data TEXT NOT NULL, PRIMARY KEY(user_id, id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_signatures_user_id ON local_signatures (user_id)")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_terms (user_id TEXT NOT NULL, id TEXT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, PRIMARY KEY(user_id, id))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_terms_user_id ON local_terms (user_id)")
                db.execSQL("CREATE TABLE IF NOT EXISTS local_document_metadata (user_id TEXT NOT NULL, documentId TEXT NOT NULL, currency TEXT NOT NULL, signature_data TEXT, payment_method TEXT, tax_rate REAL NOT NULL DEFAULT 0.0, tax_name TEXT NOT NULL DEFAULT 'Tax', discount_type TEXT NOT NULL DEFAULT 'fixed', discount_value TEXT, shipping_amount REAL NOT NULL DEFAULT 0.0, shipping_label TEXT, PRIMARY KEY(user_id, documentId))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_document_metadata_user_id ON local_document_metadata (user_id)")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS document_creation_events (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        document_id TEXT NOT NULL,
                        operation_id TEXT NOT NULL,
                        installation_id TEXT NOT NULL,
                        lease_id TEXT,
                        plan_code TEXT NOT NULL,
                        quota_scope TEXT NOT NULL,
                        period_key TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_at_client INTEGER NOT NULL,
                        acknowledged_at_server INTEGER,
                        entitlement_version INTEGER,
                        source TEXT NOT NULL,
                        migrated_baseline INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_document_creation_events_user_id ON document_creation_events (user_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_document_creation_events_user_id_document_id ON document_creation_events (user_id, document_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_document_creation_events_user_id_operation_id ON document_creation_events (user_id, operation_id)")
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO document_creation_events (
                        id, user_id, document_id, operation_id, installation_id, lease_id,
                        plan_code, quota_scope, period_key, status, created_at_client,
                        acknowledged_at_server, entitlement_version, source, migrated_baseline
                    )
                    SELECT
                        usage_event_id, user_id, document_id, operation_id, 'legacy-installation',
                        NULLIF(lease_id, ''), 'legacy', 'billing_cycle', period_month,
                        CASE WHEN status = 'SYNCED' THEN 'ACKNOWLEDGED' ELSE status END,
                        created_at, synced_at, NULL, 'legacy_ledger', 0
                    FROM local_usage_ledger
                    """.trimIndent(),
                )
                db.execSQL("DROP TABLE local_usage_ledger")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS account_entitlements (
                        user_id TEXT NOT NULL,
                        plan_code TEXT NOT NULL,
                        data_mode TEXT NOT NULL,
                        document_limit_scope TEXT NOT NULL,
                        document_limit INTEGER,
                        documents_used INTEGER NOT NULL,
                        customer_limit INTEGER,
                        product_limit INTEGER,
                        allowed_template_ids_json TEXT NOT NULL,
                        remove_tijario_branding INTEGER NOT NULL,
                        entitlement_version INTEGER NOT NULL,
                        verified_at INTEGER NOT NULL,
                        expires_at INTEGER,
                        signed_payload TEXT,
                        signature TEXT,
                        PRIMARY KEY(user_id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS backup_settings (
                        user_id TEXT NOT NULL,
                        frequency TEXT NOT NULL,
                        wifi_only INTEGER NOT NULL,
                        charging_only INTEGER NOT NULL,
                        drive_enabled INTEGER NOT NULL,
                        retention_daily INTEGER NOT NULL,
                        retention_weekly INTEGER NOT NULL,
                        retention_monthly INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        PRIMARY KEY(user_id)
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS backup_records (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        local_relative_path TEXT NOT NULL,
                        format_version INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        size_bytes INTEGER NOT NULL,
                        checksum TEXT,
                        created_at INTEGER NOT NULL,
                        uploaded_at INTEGER,
                        drive_file_id TEXT,
                        last_error TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_backup_records_user_id ON backup_records (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_backup_records_created_at ON backup_records (created_at)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS backup_file_entries (
                        id TEXT NOT NULL,
                        backup_id TEXT NOT NULL,
                        relative_path TEXT NOT NULL,
                        size_bytes INTEGER NOT NULL,
                        checksum TEXT NOT NULL,
                        status TEXT NOT NULL,
                        PRIMARY KEY(id),
                        FOREIGN KEY(backup_id) REFERENCES backup_records(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_backup_file_entries_backup_id ON backup_file_entries (backup_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_backup_file_entries_backup_id_relative_path ON backup_file_entries (backup_id, relative_path)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS device_bindings (
                        user_id TEXT NOT NULL,
                        installation_id TEXT NOT NULL,
                        device_name TEXT,
                        is_primary INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        registered_at INTEGER NOT NULL,
                        last_seen_at INTEGER,
                        revoked_at INTEGER,
                        PRIMARY KEY(user_id, installation_id)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_device_bindings_user_id ON device_bindings (user_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_device_bindings_installation_id ON device_bindings (installation_id)")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS deleted_record_history (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_id TEXT NOT NULL,
                        deleted_at INTEGER NOT NULL,
                        local_revision INTEGER NOT NULL,
                        payload_json TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_deleted_record_history_user_id ON deleted_record_history (user_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_deleted_record_history_user_id_entity_type_entity_id ON deleted_record_history (user_id, entity_type, entity_id)")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN customer_snapshot_name TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN customer_snapshot_whatsapp TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN customer_snapshot_city TEXT")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN deleted_at INTEGER")
                db.execSQL("ALTER TABLE documents_cache ADD COLUMN pdf_generation_status TEXT NOT NULL DEFAULT 'missing'")
                db.execSQL("UPDATE documents_cache SET pdf_generation_status = 'available' WHERE local_pdf_relative_path IS NOT NULL")
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE offline_quota_lease ADD COLUMN entitlement_version INTEGER")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE backup_records ADD COLUMN restored_at INTEGER")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ai_generation_history (
                        id TEXT NOT NULL,
                        user_id TEXT NOT NULL,
                        generation_type TEXT NOT NULL,
                        generation_id TEXT NOT NULL,
                        variant_id TEXT NOT NULL,
                        variant_label TEXT NOT NULL,
                        variant_order INTEGER NOT NULL,
                        result_text TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent(),
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_ai_generation_history_user_id ON ai_generation_history (user_id)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_ai_generation_history_user_id_generation_type_created_at " +
                        "ON ai_generation_history (user_id, generation_type, created_at)",
                )
            }
        }

        fun getInstance(context: Context): TijarioDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TijarioDatabase::class.java,
                    "tijario-local-cache.db",
                )
                    .addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                    .build()
                    .also { instance = it }
            }
    }
}
