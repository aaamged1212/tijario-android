package app.tijario.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TijarioDao {
    @Query("SELECT * FROM business_settings_cache WHERE user_id = :userId LIMIT 1")
    fun observeBusinessSettings(userId: String): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings_cache WHERE user_id = :userId LIMIT 1")
    suspend fun getBusinessSettings(userId: String): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBusinessSettings(settings: BusinessSettingsEntity)

    @Query("DELETE FROM business_settings_cache WHERE user_id = :userId")
    suspend fun deleteBusinessSettings(userId: String)

    @Query("SELECT * FROM customers_cache WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeCustomers(userId: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomers(customers: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers_cache WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getCustomer(userId: String, id: String): CustomerEntity?

    @Query("DELETE FROM customers_cache WHERE id = :id AND user_id = :userId")
    suspend fun deleteCustomer(userId: String, id: String)

    @Query("DELETE FROM customers_cache WHERE user_id = :userId")
    suspend fun deleteCustomers(userId: String)

    @Query("SELECT * FROM products_cache WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeProducts(userId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(product: ProductEntity)

    @Query("SELECT * FROM products_cache WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getProduct(userId: String, id: String): ProductEntity?

    @Query("DELETE FROM products_cache WHERE id = :id AND user_id = :userId")
    suspend fun deleteProduct(userId: String, id: String)

    @Query("DELETE FROM products_cache WHERE user_id = :userId")
    suspend fun deleteProducts(userId: String)

    @Query("SELECT * FROM documents_cache WHERE user_id = :userId ORDER BY issue_date DESC, document_number DESC")
    fun observeDocuments(userId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocuments(documents: List<DocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: DocumentEntity)

    @Query("SELECT * FROM documents_cache WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getDocument(userId: String, id: String): DocumentEntity?

    @Query("DELETE FROM documents_cache WHERE id = :id AND user_id = :userId")
    suspend fun deleteDocument(userId: String, id: String)

    @Query("DELETE FROM documents_cache WHERE user_id = :userId")
    suspend fun deleteDocuments(userId: String)

    @Query("SELECT COUNT(*) FROM document_items_cache WHERE product_id = :productId")
    suspend fun countDocumentItemsForProduct(productId: String): Int

    @Query("DELETE FROM business_settings_cache")
    suspend fun clearBusinessSettings()

    @Query("DELETE FROM customers_cache")
    suspend fun clearCustomers()

    @Query("DELETE FROM products_cache")
    suspend fun clearProducts()

    @Query("DELETE FROM documents_cache")
    suspend fun clearDocuments()

    @Query("SELECT COUNT(*) FROM documents_cache WHERE customer_id = :customerId")
    suspend fun countDocumentsForCustomer(customerId: String): Int

    @Query("SELECT COUNT(*) FROM documents_cache WHERE user_id = :userId AND issue_date LIKE :monthPrefix || '%'")
    suspend fun countDocumentsForMonth(userId: String, monthPrefix: String): Int

    @Query("SELECT COUNT(*) FROM documents_cache WHERE user_id = :userId AND sync_status = 'LOCAL_ONLY' AND is_deleted = 0")
    suspend fun countLocalOnlyDocuments(userId: String): Int

    @Query("SELECT * FROM local_taxes ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalTaxes(): Flow<List<LocalTaxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTax(tax: LocalTaxEntity)

    @Query("DELETE FROM local_taxes WHERE id = :id")
    suspend fun deleteLocalTax(id: String)

    @Query("SELECT * FROM local_payment_methods ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalPaymentMethods(): Flow<List<LocalPaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalPaymentMethod(method: LocalPaymentMethodEntity)

    @Query("DELETE FROM local_payment_methods WHERE id = :id")
    suspend fun deleteLocalPaymentMethod(id: String)

    @Query("SELECT * FROM local_signatures ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalSignatures(): Flow<List<LocalSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalSignature(signature: LocalSignatureEntity)

    @Query("DELETE FROM local_signatures WHERE id = :id")
    suspend fun deleteLocalSignature(id: String)

    @Query("SELECT * FROM local_terms ORDER BY title COLLATE NOCASE ASC")
    fun observeLocalTerms(): Flow<List<LocalTermsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTerms(terms: LocalTermsEntity)

    @Query("DELETE FROM local_terms WHERE id = :id")
    suspend fun deleteLocalTerms(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocumentMetadata(metadata: LocalDocumentMetadataEntity)

    @Query("SELECT * FROM local_document_metadata WHERE documentId = :documentId LIMIT 1")
    suspend fun getDocumentMetadata(documentId: String): LocalDocumentMetadataEntity?

    @Query("SELECT * FROM local_document_metadata")
    fun observeAllDocumentMetadata(): Flow<List<LocalDocumentMetadataEntity>>

    // V7 Sync state queries
    @Query("SELECT * FROM sync_state WHERE user_id = :userId LIMIT 1")
    suspend fun getSyncState(userId: String): SyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncState(state: SyncStateEntity)

    // V7 Sync outbox queries
    @Query("SELECT * FROM sync_outbox WHERE user_id = :userId ORDER BY created_at ASC")
    suspend fun getPendingOutbox(userId: String): List<SyncOutboxEntity>

    @Query("SELECT * FROM sync_outbox WHERE id = :id LIMIT 1")
    suspend fun getOutboxById(id: String): SyncOutboxEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOutbox(outbox: SyncOutboxEntity)

    @Query("DELETE FROM sync_outbox WHERE id = :id")
    suspend fun deleteOutbox(id: String)

    @Query("DELETE FROM sync_outbox WHERE user_id = :userId AND status = 'COMPLETED'")
    suspend fun clearCompletedOutbox(userId: String)

    // V7 Document item queries
    @Query("SELECT * FROM document_items_cache WHERE user_id = :userId AND document_id = :documentId ORDER BY sort_order ASC")
    fun observeDocumentItems(userId: String, documentId: String): Flow<List<DocumentItemEntity>>

    @Query("SELECT * FROM document_items_cache WHERE user_id = :userId AND document_id = :documentId ORDER BY sort_order ASC")
    suspend fun getDocumentItems(userId: String, documentId: String): List<DocumentItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocumentItems(items: List<DocumentItemEntity>)

    @Query("DELETE FROM document_items_cache WHERE user_id = :userId AND document_id = :documentId")
    suspend fun deleteDocumentItems(userId: String, documentId: String)

    // V7 Offline lease queries
    @Query("SELECT * FROM offline_quota_lease WHERE user_id = :userId AND device_id = :deviceId AND period_month = :periodMonth LIMIT 1")
    suspend fun getLease(userId: String, deviceId: String, periodMonth: String): OfflineQuotaLeaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLease(lease: OfflineQuotaLeaseEntity)

    // V7 Local usage ledger queries
    @Query("SELECT * FROM local_usage_ledger WHERE user_id = :userId AND status = 'PENDING'")
    suspend fun getPendingLedger(userId: String): List<LocalUsageLedgerEntity>

    @Query("SELECT * FROM local_usage_ledger WHERE user_id = :userId AND document_id = :documentId LIMIT 1")
    suspend fun getLedgerByDocId(userId: String, documentId: String): LocalUsageLedgerEntity?

    @Query("DELETE FROM local_usage_ledger WHERE user_id = :userId AND document_id = :documentId")
    suspend fun deleteLedgerByDocId(userId: String, documentId: String)

    @Query("SELECT * FROM local_usage_ledger WHERE user_id = :userId AND operation_id = :operationId LIMIT 1")
    suspend fun getLedgerByOpId(userId: String, operationId: String): LocalUsageLedgerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLedger(ledger: LocalUsageLedgerEntity)

    @Query("SELECT * FROM customers_cache WHERE user_id = :userId AND whatsapp_number = :whatsappNumber LIMIT 1")
    suspend fun getCustomerByWhatsapp(userId: String, whatsappNumber: String): CustomerEntity?

    @Query("SELECT * FROM documents_cache WHERE user_id = :userId AND id = :documentId LIMIT 1")
    fun observeDocument(userId: String, documentId: String): Flow<DocumentEntity?>

    @Query("DELETE FROM sync_outbox WHERE user_id = :userId")
    suspend fun deleteOutboxForUser(userId: String)

    @Query("DELETE FROM offline_quota_lease WHERE user_id = :userId")
    suspend fun deleteLeasesForUser(userId: String)

    @Query("DELETE FROM local_usage_ledger WHERE user_id = :userId")
    suspend fun deleteLedgerForUser(userId: String)
}
