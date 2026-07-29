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

    @Query("SELECT COUNT(*) FROM customers_cache WHERE user_id = :userId AND is_deleted = 0")
    suspend fun countActiveCustomers(userId: String): Int

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

    @Query("SELECT COUNT(*) FROM products_cache WHERE user_id = :userId AND is_deleted = 0")
    suspend fun countActiveProducts(userId: String): Int

    @Query("SELECT * FROM documents_cache WHERE user_id = :userId ORDER BY COALESCE(created_at, issue_date) DESC, synced_at DESC, document_number DESC")
    fun observeDocuments(userId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocuments(documents: List<DocumentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(document: DocumentEntity)

    @Query("SELECT * FROM documents_cache WHERE id = :id AND user_id = :userId LIMIT 1")
    suspend fun getDocument(userId: String, id: String): DocumentEntity?

    @Query(
        """
        UPDATE documents_cache
        SET local_pdf_relative_path = :relativePath,
            pdf_generated_at = :generatedAt,
            pdf_document_revision = :documentRevision,
            pdf_content_hash = :contentHash,
            pdf_generation_status = :status
        WHERE user_id = :userId AND id = :documentId
        """
    )
    suspend fun updateDocumentPdfState(
        userId: String,
        documentId: String,
        relativePath: String?,
        generatedAt: Long?,
        documentRevision: Long?,
        contentHash: String?,
        status: String,
    )

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

    @Query("SELECT * FROM local_taxes WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalTaxes(userId: String): Flow<List<LocalTaxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTax(tax: LocalTaxEntity)

    @Query("DELETE FROM local_taxes WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalTax(userId: String, id: String)

    @Query("DELETE FROM local_taxes")
    suspend fun clearLocalTaxes()

    @Query("DELETE FROM local_taxes WHERE user_id = :userId")
    suspend fun deleteLocalTaxesForUser(userId: String)

    @Query("SELECT * FROM local_payment_methods WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalPaymentMethods(userId: String): Flow<List<LocalPaymentMethodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalPaymentMethod(method: LocalPaymentMethodEntity)

    @Query("DELETE FROM local_payment_methods WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalPaymentMethod(userId: String, id: String)

    @Query("DELETE FROM local_payment_methods")
    suspend fun clearLocalPaymentMethods()

    @Query("DELETE FROM local_payment_methods WHERE user_id = :userId")
    suspend fun deleteLocalPaymentMethodsForUser(userId: String)

    @Query("SELECT * FROM local_signatures WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeLocalSignatures(userId: String): Flow<List<LocalSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalSignature(signature: LocalSignatureEntity)

    @Query("DELETE FROM local_signatures WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalSignature(userId: String, id: String)

    @Query("DELETE FROM local_signatures")
    suspend fun clearLocalSignatures()

    @Query("DELETE FROM local_signatures WHERE user_id = :userId")
    suspend fun deleteLocalSignaturesForUser(userId: String)

    @Query("SELECT * FROM local_terms WHERE user_id = :userId ORDER BY title COLLATE NOCASE ASC")
    fun observeLocalTerms(userId: String): Flow<List<LocalTermsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalTerms(terms: LocalTermsEntity)

    @Query("DELETE FROM local_terms WHERE user_id = :userId AND id = :id")
    suspend fun deleteLocalTerms(userId: String, id: String)

    @Query("DELETE FROM local_terms")
    suspend fun clearLocalTerms()

    @Query("DELETE FROM local_terms WHERE user_id = :userId")
    suspend fun deleteLocalTermsForUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocumentMetadata(metadata: LocalDocumentMetadataEntity)

    @Query("SELECT * FROM local_document_metadata WHERE user_id = :userId AND documentId = :documentId LIMIT 1")
    suspend fun getDocumentMetadata(userId: String, documentId: String): LocalDocumentMetadataEntity?

    @Query("SELECT * FROM local_document_metadata WHERE user_id = :userId")
    fun observeAllDocumentMetadata(userId: String): Flow<List<LocalDocumentMetadataEntity>>

    @Query("DELETE FROM local_document_metadata")
    suspend fun clearLocalDocumentMetadata()

    @Query("DELETE FROM local_document_metadata WHERE user_id = :userId")
    suspend fun deleteLocalDocumentMetadataForUser(userId: String)

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

    @Query("SELECT * FROM offline_quota_lease WHERE user_id = :userId AND device_id = :deviceId AND status = 'ACTIVE' AND expires_at > :now ORDER BY expires_at DESC LIMIT 1")
    suspend fun getActiveLease(userId: String, deviceId: String, now: Long): OfflineQuotaLeaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLease(lease: OfflineQuotaLeaseEntity)

    // Immutable document creation event queries
    @Query("SELECT * FROM document_creation_events WHERE user_id = :userId AND status = 'PENDING' ORDER BY created_at_client ASC")
    suspend fun getPendingCreationEvents(userId: String): List<DocumentCreationEventEntity>

    @Query("SELECT * FROM document_creation_events WHERE user_id = :userId AND document_id = :documentId LIMIT 1")
    suspend fun getCreationEventByDocument(userId: String, documentId: String): DocumentCreationEventEntity?

    @Query("SELECT * FROM document_creation_events WHERE user_id = :userId AND operation_id = :operationId LIMIT 1")
    suspend fun getCreationEventByOperation(userId: String, operationId: String): DocumentCreationEventEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCreationEvent(event: DocumentCreationEventEntity): Long

    @Query("UPDATE document_creation_events SET status = 'ACKNOWLEDGED', acknowledged_at_server = :acknowledgedAt WHERE user_id = :userId AND document_id = :documentId AND status = 'PENDING'")
    suspend fun acknowledgeCreationEvent(userId: String, documentId: String, acknowledgedAt: Long): Int

    @Query(
        """
        UPDATE offline_quota_lease
        SET consumed_count = consumed_count + 1,
            status = CASE WHEN consumed_count + 1 >= allowed_limit THEN 'EXHAUSTED' ELSE 'ACTIVE' END
        WHERE id = :leaseId
          AND user_id = :userId
          AND status = 'ACTIVE'
          AND consumed_count < allowed_limit
        """
    )
    suspend fun consumeLeaseCredit(userId: String, leaseId: String): Int

    @Query("UPDATE document_creation_events SET status = 'REJECTED', acknowledged_at_server = :resolvedAt WHERE user_id = :userId AND operation_id = :operationId AND status = 'PENDING'")
    suspend fun rejectCreationEvent(userId: String, operationId: String, resolvedAt: Long): Int

    @Query("UPDATE document_creation_events SET lease_id = :leaseId WHERE user_id = :userId AND operation_id = :operationId AND status = 'PENDING' AND lease_id IS NULL")
    suspend fun assignLeaseToLegacyCreationEvent(userId: String, operationId: String, leaseId: String): Int

    @Query("UPDATE document_creation_events SET lease_id = NULL WHERE user_id = :userId AND operation_id = :operationId AND status = 'PENDING'")
    suspend fun clearLeaseFromPendingCreationEvent(userId: String, operationId: String): Int

    @Query("UPDATE document_creation_events SET status = 'BLOCKED', acknowledged_at_server = :resolvedAt WHERE user_id = :userId AND operation_id = :operationId AND status = 'PENDING'")
    suspend fun blockCreationEvent(userId: String, operationId: String, resolvedAt: Long): Int

    @Query("SELECT COUNT(*) FROM document_creation_events WHERE user_id = :userId AND migrated_baseline = 0")
    suspend fun countDocumentCreationEvents(userId: String): Int

    @Query("SELECT COUNT(*) FROM document_creation_events WHERE user_id = :userId AND period_key = :periodKey AND migrated_baseline = 0")
    suspend fun countDocumentCreationEventsForPeriod(userId: String, periodKey: String): Int

    @Query("SELECT * FROM account_entitlements WHERE user_id = :userId LIMIT 1")
    suspend fun getAccountEntitlement(userId: String): AccountEntitlementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccountEntitlement(entitlement: AccountEntitlementEntity)

    @Query("SELECT * FROM backup_settings WHERE user_id = :userId LIMIT 1")
    suspend fun getBackupSettings(userId: String): BackupSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBackupSettings(settings: BackupSettingsEntity)

    @Query("SELECT * FROM backup_records WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getBackupRecords(userId: String): List<BackupRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBackupRecord(record: BackupRecordEntity)

    @Query("DELETE FROM backup_records WHERE user_id = :userId AND id = :backupId")
    suspend fun deleteBackupRecord(userId: String, backupId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBackupFileEntries(entries: List<BackupFileEntryEntity>)

    @Query("SELECT * FROM backup_file_entries WHERE backup_id = :backupId ORDER BY relative_path ASC")
    suspend fun getBackupFileEntries(backupId: String): List<BackupFileEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeviceBinding(binding: DeviceBindingEntity)

    @Query("SELECT * FROM device_bindings WHERE user_id = :userId")
    suspend fun getDeviceBindings(userId: String): List<DeviceBindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeletedRecord(record: DeletedRecordEntity)

    @Query("SELECT * FROM deleted_record_history WHERE user_id = :userId ORDER BY deleted_at ASC")
    suspend fun getDeletedRecords(userId: String): List<DeletedRecordEntity>

    @Query("SELECT * FROM deleted_record_history WHERE user_id = :userId AND entity_type = :entityType AND entity_id = :entityId LIMIT 1")
    suspend fun getDeletedRecord(userId: String, entityType: String, entityId: String): DeletedRecordEntity?

    @Query("DELETE FROM deleted_record_history WHERE user_id = :userId AND entity_type = :entityType AND entity_id = :entityId")
    suspend fun deleteDeletedRecord(userId: String, entityType: String, entityId: String)

    @Query("SELECT * FROM documents_cache WHERE user_id = :userId AND id = :documentId LIMIT 1")
    fun observeDocument(userId: String, documentId: String): Flow<DocumentEntity?>

    @Query("DELETE FROM sync_outbox WHERE user_id = :userId")
    suspend fun deleteOutboxForUser(userId: String)

    @Query("DELETE FROM offline_quota_lease WHERE user_id = :userId")
    suspend fun deleteLeasesForUser(userId: String)

    @Query("DELETE FROM document_creation_events WHERE user_id = :userId")
    suspend fun deleteCreationEventsForUser(userId: String)

    @Query("DELETE FROM account_entitlements WHERE user_id = :userId")
    suspend fun deleteAccountEntitlementForUser(userId: String)

    @Query("DELETE FROM backup_settings WHERE user_id = :userId")
    suspend fun deleteBackupSettingsForUser(userId: String)

    @Query("DELETE FROM backup_records WHERE user_id = :userId")
    suspend fun deleteBackupRecordsForUser(userId: String)

    @Query("DELETE FROM device_bindings WHERE user_id = :userId")
    suspend fun deleteDeviceBindingsForUser(userId: String)

    @Query("DELETE FROM deleted_record_history WHERE user_id = :userId")
    suspend fun deleteDeletedRecordsForUser(userId: String)

    @Query("DELETE FROM sync_state WHERE user_id = :userId")
    suspend fun deleteSyncStateForUser(userId: String)

    @Query("DELETE FROM sync_state")
    suspend fun clearSyncState()

    @Query("DELETE FROM sync_outbox")
    suspend fun clearOutbox()

    @Query("DELETE FROM offline_quota_lease")
    suspend fun clearLeases()

    @Query("DELETE FROM document_creation_events")
    suspend fun clearCreationEvents()
}
