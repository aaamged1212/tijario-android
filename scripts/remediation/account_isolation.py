from __future__ import annotations

from pathlib import Path
import re
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if new in text:
        return text
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one old block, found {count}")
    return text.replace(old, new, 1)


entities_path = "app/src/main/java/app/tijario/data/local/TijarioEntities.kt"
entities = read(entities_path)
old_entities = '''@Entity(tableName = "local_taxes")
data class LocalTaxEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val rate: Double
)

@Entity(tableName = "local_payment_methods")
data class LocalPaymentMethodEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val details: String? = null
)

@Entity(tableName = "local_signatures")
data class LocalSignatureEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String
)

@Entity(tableName = "local_terms")
data class LocalTermsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String
)

@Entity(tableName = "local_document_metadata")
data class LocalDocumentMetadataEntity(
    @PrimaryKey
    val documentId: String,
    val currency: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String?,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    @ColumnInfo(name = "tax_rate", defaultValue = "0.0")
    val taxRate: Double = 0.0,
    @ColumnInfo(name = "tax_name", defaultValue = "Tax")
    val taxName: String = "Tax",
    @ColumnInfo(name = "discount_type", defaultValue = "fixed")
    val discountType: String = "fixed",
    @ColumnInfo(name = "discount_value")
    val discountValue: String? = null,
    @ColumnInfo(name = "shipping_amount", defaultValue = "0.0")
    val shippingAmount: Double = 0.0,
    @ColumnInfo(name = "shipping_label")
    val shippingLabel: String? = null
)'''
new_entities = '''@Entity(
    tableName = "local_taxes",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalTaxEntity(
    val id: String,
    val name: String,
    val rate: Double,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_payment_methods",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalPaymentMethodEntity(
    val id: String,
    val name: String,
    val details: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_signatures",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalSignatureEntity(
    val id: String,
    val name: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_terms",
    primaryKeys = ["user_id", "id"],
    indices = [Index(value = ["user_id"])],
)
data class LocalTermsEntity(
    val id: String,
    val title: String,
    val content: String,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)

@Entity(
    tableName = "local_document_metadata",
    primaryKeys = ["user_id", "documentId"],
    indices = [Index(value = ["user_id"])],
)
data class LocalDocumentMetadataEntity(
    val documentId: String,
    val currency: String,
    @ColumnInfo(name = "signature_data")
    val signatureData: String?,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    @ColumnInfo(name = "tax_rate", defaultValue = "0.0")
    val taxRate: Double = 0.0,
    @ColumnInfo(name = "tax_name", defaultValue = "Tax")
    val taxName: String = "Tax",
    @ColumnInfo(name = "discount_type", defaultValue = "fixed")
    val discountType: String = "fixed",
    @ColumnInfo(name = "discount_value")
    val discountValue: String? = null,
    @ColumnInfo(name = "shipping_amount", defaultValue = "0.0")
    val shippingAmount: Double = 0.0,
    @ColumnInfo(name = "shipping_label")
    val shippingLabel: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
)'''
entities = replace_once(entities, old_entities, new_entities, "local account entities")
write(entities_path, entities)


dao_path = "app/src/main/java/app/tijario/data/local/TijarioDao.kt"
dao = read(dao_path)
start_marker = '    @Query("SELECT * FROM local_taxes ORDER BY name COLLATE NOCASE ASC")'
end_marker = '    fun observeAllDocumentMetadata(): Flow<List<LocalDocumentMetadataEntity>>'
new_dao_block = '''    @Query("SELECT * FROM local_taxes WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalTaxes(userId: String): Flow<List<LocalTaxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTax(tax: LocalTaxEntity)

    @Query("DELETE FROM local_taxes WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalTax(userId: String, id: String)

    @Query("DELETE FROM local_taxes")
    suspend fun clearLocalTaxes()

    @Query("SELECT * FROM local_payment_methods WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalPaymentMethods(userId: String): Flow<List<LocalPaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalPaymentMethod(method: LocalPaymentMethodEntity)

    @Query("DELETE FROM local_payment_methods WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalPaymentMethod(userId: String, id: String)

    @Query("DELETE FROM local_payment_methods")
    suspend fun clearLocalPaymentMethods()

    @Query("SELECT * FROM local_signatures WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalSignatures(userId: String): Flow<List<LocalSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalSignature(signature: LocalSignatureEntity)

    @Query("DELETE FROM local_signatures WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalSignature(userId: String, id: String)

    @Query("DELETE FROM local_signatures")
    suspend fun clearLocalSignatures()

    @Query("SELECT * FROM local_terms WHERE user_id = :userId ORDER BY title COLLATE NOCASE ASC")
    fun observeLocalTerms(userId: String): Flow<List<LocalTermsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTerms(terms: LocalTermsEntity)

    @Query("DELETE FROM local_terms WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalTerms(userId: String, id: String)

    @Query("DELETE FROM local_terms")
    suspend fun clearLocalTerms()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocumentMetadata(metadata: LocalDocumentMetadataEntity)

    @Query("SELECT * FROM local_document_metadata WHERE user_id = :userId AND documentId = :documentId LIMIT 1")
    suspend fun getDocumentMetadata(userId: String, documentId: String): LocalDocumentMetadataEntity?

    @Query("SELECT * FROM local_document_metadata WHERE user_id = :userId")
    fun observeAllDocumentMetadata(userId: String): Flow<List<LocalDocumentMetadataEntity>>

    @Query("DELETE FROM local_document_metadata")
    suspend fun clearLocalDocumentMetadata()'''
if new_dao_block not in dao:
    start = dao.index(start_marker)
    end = dao.index(end_marker, start) + len(end_marker)
    dao = dao[:start] + new_dao_block + dao[end:]
old_dao_tail = '''    @Query("DELETE FROM local_usage_ledger WHERE user_id = :userId")
    suspend fun deleteLedgerForUser(userId: String)
}'''
new_dao_tail = '''    @Query("DELETE FROM local_usage_ledger WHERE user_id = :userId")
    suspend fun deleteLedgerForUser(userId: String)

    @Query("DELETE FROM sync_state")
    suspend fun clearSyncState()

    @Query("DELETE FROM sync_outbox")
    suspend fun clearOutbox()

    @Query("DELETE FROM offline_quota_lease")
    suspend fun clearLeases()

    @Query("DELETE FROM local_usage_ledger")
    suspend fun clearLedger()
}'''
dao = replace_once(dao, old_dao_tail, new_dao_tail, "DAO global clear methods")
write(dao_path, dao)


database_path = "app/src/main/java/app/tijario/data/local/TijarioDatabase.kt"
database = read(database_path)
if "next_retry_at INTEGER NOT NULL DEFAULT 0" not in database:
    database, count = re.subn(
        r"(\s+lock_expires_at INTEGER,\n)(\s+last_error TEXT,)",
        r"\1                        next_retry_at INTEGER NOT NULL DEFAULT 0,\n\2",
        database,
        count=1,
    )
    if count != 1:
        raise RuntimeError("Room 6-7 next_retry_at insertion failed")
database = replace_once(database, "    version = 14,", "    version = 15,", "Room version")
old_migration_tail = '''        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN discount_type TEXT NOT NULL DEFAULT 'fixed'")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN discount_value TEXT")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN shipping_amount REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE local_document_metadata ADD COLUMN shipping_label TEXT")
            }
        }

        fun getInstance(context: Context): TijarioDatabase ='''
new_migration_tail = '''        val MIGRATION_13_14 = object : Migration(13, 14) {
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

        fun getInstance(context: Context): TijarioDatabase ='''
database = replace_once(database, old_migration_tail, new_migration_tail, "Room 14-15 migration")
database = replace_once(
    database,
    ".addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)",
    ".addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15)",
    "Room migration registration",
)
write(database_path, database)


repository_path = "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt"
repository = read(repository_path)
repository = repository.replace(
    "            dao.observeAllDocumentMetadata()",
    "            dao.observeAllDocumentMetadata(userId)",
    1,
)
if "            return\n        }\n\n        val first = pending.first()" in repository:
    repository = repository.replace(
        '''            dao.upsertOutbox(entry)
            return
        }

        val first = pending.first()
        when {''',
        '''            dao.upsertOutbox(entry)
        } else {
            val first = pending.first()
            when {''',
        1,
    )
    repository = repository.replace(
        '''            }
        }
        SyncScheduler(context).triggerSync(userId)
    }

    // Remote Cache Ingestion Policy''',
        '''            }
            }
        }
        SyncScheduler(context).triggerSync(userId)
    }

    // Remote Cache Ingestion Policy''',
        1,
    )
old_repository_block = '''    fun observeLocalTaxes(): Flow<List<app.tijario.data.local.LocalTaxEntity>> = dao.observeLocalTaxes()
    suspend fun upsertLocalTax(tax: app.tijario.data.local.LocalTaxEntity) = withContext(Dispatchers.IO) { dao.upsertLocalTax(tax) }
    suspend fun deleteLocalTax(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTax(id) }

    fun observeLocalPaymentMethods(): Flow<List<app.tijario.data.local.LocalPaymentMethodEntity>> = dao.observeLocalPaymentMethods()
    suspend fun upsertLocalPaymentMethod(method: app.tijario.data.local.LocalPaymentMethodEntity) = withContext(Dispatchers.IO) { dao.upsertLocalPaymentMethod(method) }
    suspend fun deleteLocalPaymentMethod(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalPaymentMethod(id) }

    fun observeLocalSignatures(): Flow<List<app.tijario.data.local.LocalSignatureEntity>> = dao.observeLocalSignatures()
    suspend fun upsertLocalSignature(sig: app.tijario.data.local.LocalSignatureEntity) = withContext(Dispatchers.IO) { dao.upsertLocalSignature(sig) }
    suspend fun deleteLocalSignature(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalSignature(id) }

    fun observeLocalTerms(): Flow<List<app.tijario.data.local.LocalTermsEntity>> = dao.observeLocalTerms()
    suspend fun upsertLocalTerms(terms: app.tijario.data.local.LocalTermsEntity) = withContext(Dispatchers.IO) { dao.upsertLocalTerms(terms) }
    suspend fun deleteLocalTerms(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTerms(id) }

    suspend fun getDocumentMetadata(documentId: String): app.tijario.data.local.LocalDocumentMetadataEntity? = withContext(Dispatchers.IO) {
        dao.getDocumentMetadata(documentId)
    }

    suspend fun upsertDocumentMetadata(metadata: app.tijario.data.local.LocalDocumentMetadataEntity) = withContext(Dispatchers.IO) {
        dao.upsertDocumentMetadata(metadata)
    }'''
new_repository_block = '''    private fun currentUserIdOrThrow(): String =
        supabaseClient.auth.currentUserOrNull()?.id ?: error("No authenticated user.")

    fun observeLocalTaxes(): Flow<List<app.tijario.data.local.LocalTaxEntity>> =
        dao.observeLocalTaxes(currentUserIdOrThrow())
    suspend fun upsertLocalTax(tax: app.tijario.data.local.LocalTaxEntity) = withContext(Dispatchers.IO) {
        dao.upsertLocalTax(tax.copy(userId = requireUserId()))
    }
    suspend fun deleteLocalTax(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTax(requireUserId(), id) }

    fun observeLocalPaymentMethods(): Flow<List<app.tijario.data.local.LocalPaymentMethodEntity>> =
        dao.observeLocalPaymentMethods(currentUserIdOrThrow())
    suspend fun upsertLocalPaymentMethod(method: app.tijario.data.local.LocalPaymentMethodEntity) = withContext(Dispatchers.IO) {
        dao.upsertLocalPaymentMethod(method.copy(userId = requireUserId()))
    }
    suspend fun deleteLocalPaymentMethod(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalPaymentMethod(requireUserId(), id) }

    fun observeLocalSignatures(): Flow<List<app.tijario.data.local.LocalSignatureEntity>> =
        dao.observeLocalSignatures(currentUserIdOrThrow())
    suspend fun upsertLocalSignature(sig: app.tijario.data.local.LocalSignatureEntity) = withContext(Dispatchers.IO) {
        dao.upsertLocalSignature(sig.copy(userId = requireUserId()))
    }
    suspend fun deleteLocalSignature(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalSignature(requireUserId(), id) }

    fun observeLocalTerms(): Flow<List<app.tijario.data.local.LocalTermsEntity>> =
        dao.observeLocalTerms(currentUserIdOrThrow())
    suspend fun upsertLocalTerms(terms: app.tijario.data.local.LocalTermsEntity) = withContext(Dispatchers.IO) {
        dao.upsertLocalTerms(terms.copy(userId = requireUserId()))
    }
    suspend fun deleteLocalTerms(id: String) = withContext(Dispatchers.IO) { dao.deleteLocalTerms(requireUserId(), id) }

    suspend fun getDocumentMetadata(documentId: String): app.tijario.data.local.LocalDocumentMetadataEntity? = withContext(Dispatchers.IO) {
        dao.getDocumentMetadata(requireUserId(), documentId)
    }

    suspend fun upsertDocumentMetadata(metadata: app.tijario.data.local.LocalDocumentMetadataEntity) = withContext(Dispatchers.IO) {
        dao.upsertDocumentMetadata(metadata.copy(userId = requireUserId()))
    }'''
repository = replace_once(
    repository,
    old_repository_block,
    new_repository_block,
    "Repository user-scoped local methods",
)
repository = replace_once(
    repository,
    '''                dao.clearBusinessSettings()
                dao.clearCustomers()
                dao.clearProducts()
                dao.clearDocuments()''',
    '''                dao.clearBusinessSettings()
                dao.clearCustomers()
                dao.clearProducts()
                dao.clearDocuments()
                dao.clearLocalTaxes()
                dao.clearLocalPaymentMethods()
                dao.clearLocalSignatures()
                dao.clearLocalTerms()
                dao.clearLocalDocumentMetadata()
                dao.clearSyncState()
                dao.clearOutbox()
                dao.clearLeases()
                dao.clearLedger()''',
    "Complete local cache clearing",
)
write(repository_path, repository)

print("account isolation remediation applied")
