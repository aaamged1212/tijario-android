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

    @Query("DELETE FROM customers_cache WHERE user_id = :userId")
    suspend fun deleteCustomers(userId: String)

    @Query("SELECT * FROM products_cache WHERE user_id = :userId ORDER BY name COLLATE NOCASE ASC")
    fun observeProducts(userId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Query("DELETE FROM products_cache WHERE user_id = :userId")
    suspend fun deleteProducts(userId: String)

    @Query("SELECT * FROM documents_cache WHERE user_id = :userId ORDER BY issue_date DESC, document_number DESC")
    fun observeDocuments(userId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocuments(documents: List<DocumentEntity>)

    @Query("DELETE FROM documents_cache WHERE user_id = :userId")
    suspend fun deleteDocuments(userId: String)

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
}
