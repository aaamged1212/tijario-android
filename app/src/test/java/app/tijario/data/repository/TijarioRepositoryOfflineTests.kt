package app.tijario.data.repository

import android.content.Context
import app.tijario.data.local.TijarioDao
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.BusinessSettingsEntity
import app.tijario.data.local.CustomerEntity
import app.tijario.data.local.DocumentEntity
import app.tijario.data.local.DocumentCreationEventEntity
import app.tijario.data.local.AccountEntitlementEntity
import app.tijario.data.local.OfflineQuotaLeaseEntity
import app.tijario.data.local.ProductEntity
import app.tijario.data.local.SyncOutboxEntity
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import app.tijario.data.remote.BackendApiClient
import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.DocumentCustomerInput
import app.tijario.data.remote.DocumentItemInput
import io.github.jan.supabase.SupabaseClient
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class TijarioRepositoryOfflineTests {

    private val context = mockk<Context>(relaxed = true)
    private val database = mockk<TijarioDatabase>(relaxed = true)
    private val dao = mockk<TijarioDao>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private val backendApiClient = mockk<BackendApiClient>(relaxed = true)

    private lateinit var repository: TijarioRepository
    private val userId = "test_user_123"

    private fun validOfflineQuotaLease() = OfflineQuotaLeaseEntity(
        id = "lease_for_document_creation",
        userId = userId,
        deviceId = "test_installation",
        planCode = "free",
        periodMonth = java.util.Date().toInstant().toString().substring(0, 7) + "-01",
        allowedLimit = 5,
        consumedCount = 0,
        expiresAt = System.currentTimeMillis() + 100_000,
        status = "ACTIVE",
    )

    private fun localDriveEntitlement(documentsUsed: Int = 0) = AccountEntitlementEntity(
        userId = userId,
        planCode = "free",
        dataMode = "local_drive",
        documentLimitScope = "lifetime",
        documentLimit = 5,
        documentsUsed = documentsUsed,
        customerLimit = 5,
        productLimit = 5,
        allowedTemplateIdsJson = "[\"tijario-classic\"]",
        removeTijarioBranding = false,
        entitlementVersion = 1L,
        verifiedAt = 1L,
        expiresAt = System.currentTimeMillis() + 100_000,
        signedPayload = "signed-payload",
        signature = "signature",
    )

    // Subclass of repository under test to stub out active Supabase Auth & Remote Fetch connections
    private open class TestableTijarioRepository(
        context: Context,
        database: TijarioDatabase,
        supabaseClient: SupabaseClient,
        backendApiClient: BackendApiClient,
        private val stubUserId: String,
        private val fakeBusinessSettings: BusinessSettings? = null,
        private val fakeCustomersList: List<Customer> = emptyList(),
        private val fakeDocumentsList: List<DocumentSummary> = emptyList(),
        private val fakeProductsList: List<Product> = emptyList()
    ) : TijarioRepository(context, database, supabaseClient, backendApiClient) {
        
        override suspend fun currentUserId(): String? {
            return stubUserId
        }

        override suspend fun fetchBusinessSettings(userId: String): BusinessSettings? {
            return fakeBusinessSettings
        }

        override suspend fun fetchCustomers(userId: String): List<Customer> {
            return fakeCustomersList
        }

        override suspend fun fetchDocuments(userId: String): List<DocumentSummary> {
            return fakeDocumentsList
        }

        override suspend fun fetchProducts(userId: String): List<Product> {
            return fakeProductsList
        }
    }

    @Before
    fun setUp() {
        io.mockk.mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<TijarioDatabase>().withTransaction<Any?>(any()) } answers {
            val block = secondArg<suspend () -> Any?>()
            runBlocking { block() }
        }
        every { database.tijarioDao() } returns dao
        // Operational writes now require an initialized entitlement; legacy-sync fixtures use this explicit mode.
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement().copy(dataMode = "legacy_cloud")
        repository = TestableTijarioRepository(context, database, supabaseClient, backendApiClient, userId)
    }

    @Test
    fun createCustomerLocal_writesToRoomAndEnqueuesCreateInOutbox() = runBlocking {
        val customer = Customer(name = "عميل جديد", whatsappNumber = "1234567")

        val slotEntity = slot<CustomerEntity>()
        coEvery { dao.upsertCustomer(capture(slotEntity)) } returns Unit

        val slotOutbox = slot<SyncOutboxEntity>()
        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        coEvery { dao.upsertOutbox(capture(slotOutbox)) } returns Unit

        val result = repository.createCustomerLocal(customer).getOrThrow()

        // Verify entity details
        assertEquals("LOCAL_ONLY", slotEntity.captured.syncStatus)
        assertEquals(1, slotEntity.captured.localRevision)
        assertEquals("عميل جديد", slotEntity.captured.name)
        assertEquals(result.id, slotEntity.captured.id)

        // Verify outbox entry details
        assertEquals("customer", slotOutbox.captured.entityType)
        assertEquals("CREATE", slotOutbox.captured.operation)
        assertEquals(result.id, slotOutbox.captured.entityId)
    }

    @Test
    fun createDocumentLocal_doesNotMergeCustomerByWhatsapp() = runBlocking {
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        coEvery { dao.upsertOutbox(any()) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getLease(userId, any(), any()) } returns validOfflineQuotaLease()
        coEvery { dao.insertCreationEvent(any()) } returns 1L

        val customerSlot = slot<CustomerEntity>()
        val documentSlot = slot<app.tijario.data.local.DocumentEntity>()
        coEvery { dao.upsertCustomer(capture(customerSlot)) } returns Unit
        coEvery { dao.upsertDocument(capture(documentSlot)) } answers {
            coEvery { dao.getDocument(userId, documentSlot.captured.id) } returns documentSlot.captured
            Unit
        }

        val result = repository.createDocumentLocal(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("New recipient", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertEquals("1234567", customerSlot.captured.whatsappNumber)
        assertEquals(customerSlot.captured.id, documentSlot.captured.customerId)
    }

    @Test
    fun createDocumentLocal_usesSelectedCustomerIdWithoutCreatingDuplicateCustomer() = runBlocking {
        val existingCustomerId = "customer_existing_1"
        val existingCustomer = CustomerEntity(
            id = existingCustomerId,
            userId = userId,
            name = "Existing customer",
            whatsappNumber = "1234567",
            city = "Riyadh",
            notes = null,
            syncedAt = 1000L,
            syncStatus = "SYNCED",
            localRevision = 3,
            serverRevision = "server-rev-1",
            serverUpdatedAt = null,
            lastSyncedAt = 1000L,
            syncErrorCode = null,
            isDeleted = false,
        )
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getCustomer(userId, existingCustomerId) } returns existingCustomer
        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getLease(userId, any(), any()) } returns validOfflineQuotaLease()
        coEvery { dao.insertCreationEvent(any()) } returns 1L

        val customerSlot = slot<CustomerEntity>()
        val documentSlot = slot<app.tijario.data.local.DocumentEntity>()
        val outboxEntries = mutableListOf<SyncOutboxEntity>()
        coEvery { dao.upsertCustomer(capture(customerSlot)) } returns Unit
        coEvery { dao.upsertOutbox(capture(outboxEntries)) } returns Unit
        coEvery { dao.upsertDocument(capture(documentSlot)) } answers {
            coEvery { dao.getDocument(userId, documentSlot.captured.id) } returns documentSlot.captured
            Unit
        }

        val result = repository.createDocumentLocal(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput(
                    name = "Existing customer",
                    whatsappNumber = "1234567",
                    city = "Riyadh",
                    id = existingCustomerId,
                ),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertEquals(existingCustomerId, customerSlot.captured.id)
        assertEquals(existingCustomerId, documentSlot.captured.customerId)
        assertTrue(outboxEntries.none { it.entityType == "customer" && it.operation == "CREATE" })
    }

    @Test
    fun updateCustomerLocal_incrementsRevisionAndEnqueuesUpdate() = runBlocking {
        val existingId = "customer_99"
        val existing = CustomerEntity(
            id = existingId,
            userId = userId,
            name = "عميل قديم",
            whatsappNumber = "12345",
            city = null,
            notes = null,
            syncedAt = 5000L,
            syncStatus = "SYNCED",
            localRevision = 2,
            serverRevision = "rev-99",
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )

        coEvery { dao.getCustomer(userId, existingId) } returns existing
        val slotEntity = slot<CustomerEntity>()
        coEvery { dao.upsertCustomer(capture(slotEntity)) } returns Unit

        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        val slotOutbox = slot<SyncOutboxEntity>()
        coEvery { dao.upsertOutbox(capture(slotOutbox)) } returns Unit

        val updateData = Customer(id = existingId, name = "عميل محدث", whatsappNumber = "12345")
        repository.updateCustomerLocal(updateData).getOrThrow()

        // Verify entity fields
        assertEquals(3, slotEntity.captured.localRevision)
        assertEquals("PENDING_SYNC", slotEntity.captured.syncStatus)
        assertEquals("عميل محدث", slotEntity.captured.name)

        // Verify outbox entry
        assertEquals("customer", slotOutbox.captured.entityType)
        assertEquals("UPDATE", slotOutbox.captured.operation)
        assertEquals("rev-99", slotOutbox.captured.baseServerRevision)
    }

    @Test
    fun outboxCompaction_createThenUpdate_keepsCreate() = runBlocking {
        val customerId = "customer_comp_1"
        val existingCreate = SyncOutboxEntity(
            id = "outbox_1",
            userId = userId,
            entityType = "customer",
            entityId = customerId,
            operation = "CREATE",
            idempotencyKey = "key_1",
            baseServerRevision = null,
            status = "PENDING",
            attempts = 0,
            nextRetryAt = 0L,
            processingStartedAt = null,
            lockExpiresAt = null,
            lastError = null,
            createdAt = 1000L,
            deletedMinimalPayload = null
        )

        coEvery { dao.getCustomer(userId, customerId) } returns CustomerEntity(
            id = customerId,
            userId = userId,
            name = "name",
            whatsappNumber = "123",
            city = null,
            notes = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            serverRevision = null,
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )
        coEvery { dao.getPendingOutbox(userId) } returns listOf(existingCreate)

        val customerUpdate = Customer(id = customerId, name = "name modified", whatsappNumber = "123")
        repository.updateCustomerLocal(customerUpdate).getOrThrow()

        // Compaction rule: CREATE + UPDATE -> Keep CREATE, no new outbox upsert called
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun outboxCompaction_updateThenDelete_convertsToDelete() = runBlocking {
        val customerId = "customer_comp_2"
        val existingUpdate = SyncOutboxEntity(
            id = "outbox_2",
            userId = userId,
            entityType = "customer",
            entityId = customerId,
            operation = "UPDATE",
            idempotencyKey = "key_2",
            baseServerRevision = "rev-original",
            status = "PENDING",
            attempts = 0,
            nextRetryAt = 0L,
            processingStartedAt = null,
            lockExpiresAt = null,
            lastError = null,
            createdAt = 1000L,
            deletedMinimalPayload = null
        )

        coEvery { dao.getCustomer(userId, customerId) } returns CustomerEntity(
            id = customerId,
            userId = userId,
            name = "name",
            whatsappNumber = "123",
            city = null,
            notes = null,
            syncedAt = 2000L,
            syncStatus = "SYNCED",
            localRevision = 1,
            serverRevision = "rev-original",
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )
        coEvery { dao.getPendingOutbox(userId) } returns listOf(existingUpdate)
        coEvery { dao.countDocumentsForCustomer(customerId) } returns 0

        val slotOutbox = slot<SyncOutboxEntity>()
        coEvery { dao.upsertOutbox(capture(slotOutbox)) } returns Unit
        coEvery { dao.deleteOutbox("outbox_2") } returns Unit

        repository.deleteCustomerLocal(customerId).getOrThrow()

        // Compaction rule: UPDATE + DELETE -> delete first outbox, upsert DELETE
        coVerify(exactly = 1) { dao.deleteOutbox("outbox_2") }
        assertEquals("DELETE", slotOutbox.captured.operation)
        assertEquals("rev-original", slotOutbox.captured.baseServerRevision)
    }

    @Test
    fun outboxCompaction_createThenDelete_cancelsBoth() = runBlocking {
        val customerId = "customer_comp_3"
        val existingCreate = SyncOutboxEntity(
            id = "outbox_3",
            userId = userId,
            entityType = "customer",
            entityId = customerId,
            operation = "CREATE",
            idempotencyKey = "key_3",
            baseServerRevision = null,
            status = "PENDING",
            attempts = 0,
            nextRetryAt = 0L,
            processingStartedAt = null,
            lockExpiresAt = null,
            lastError = null,
            createdAt = 1000L,
            deletedMinimalPayload = null
        )

        coEvery { dao.getCustomer(userId, customerId) } returns CustomerEntity(
            id = customerId,
            userId = userId,
            name = "name",
            whatsappNumber = "123",
            city = null,
            notes = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            serverRevision = null,
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )
        coEvery { dao.getPendingOutbox(userId) } returns listOf(existingCreate)
        coEvery { dao.countDocumentsForCustomer(customerId) } returns 0
        coEvery { dao.deleteOutbox("outbox_3") } returns Unit

        repository.deleteCustomerLocal(customerId).getOrThrow()

        // Compaction rule: CREATE + DELETE -> cancel both (delete first create, do not insert new delete)
        coVerify(exactly = 1) { dao.deleteOutbox("outbox_3") }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun remoteIngestion_doesNotOverwriteLocalPendingChanges() = runBlocking {
        val existingProduct = ProductEntity(
            id = "prod_pending",
            userId = userId,
            kind = "product",
            name = "اسم محلي",
            description = null,
            price = BigDecimal("150.00"),
            currency = "SAR",
            stockQuantity = null,
            syncedAt = 0L,
            syncStatus = "PENDING_SYNC",
            localRevision = 2,
            serverRevision = null,
            serverUpdatedAt = null,
            lastSyncedAt = null,
            syncErrorCode = null,
            isDeleted = false
        )

        coEvery { dao.getProduct(userId, "prod_pending") } returns existingProduct

        val remoteProducts = listOf(
            Product(id = "prod_pending", userId = userId, kind = ProductKind.Product, name = "Remote Name", price = 100.0, currency = "SAR", stockQuantity = null)
        )

        val ingestionRepo = TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeProductsList = remoteProducts,
        )

        ingestionRepo.refreshProducts()

        // Verify that products cache update was never called because it is marked as PENDING_SYNC
        coVerify(exactly = 0) { dao.upsertProducts(any()) }
    }

    @Test
    fun createCustomer_routesLocalDriveToRoomWithoutOperationalOutbox() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.upsertCustomer(any()) } returns Unit

        val result = repository.createCustomer(Customer(name = "Offline customer", whatsappNumber = "1234567"))

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dao.upsertCustomer(match { it.syncStatus == "LOCAL_ONLY" }) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun createDocument_routesLocalDriveToRoomWithoutAnActiveLease() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.getActiveLease(userId, any(), any()) } returns null
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.upsertCustomer(any()) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.insertCreationEvent(any()) } returns 1L
        val documentSlot = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(documentSlot)) } answers {
            coEvery { dao.getDocument(userId, documentSlot.captured.id) } returns documentSlot.captured
            Unit
        }

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        coVerify(exactly = 0) { backendApiClient.createDocument(any()) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
        coVerify(exactly = 1) {
            dao.insertCreationEvent(match { it.quotaScope == "lifetime" && it.leaseId == null })
        }
    }

    @Test
    fun createDocument_rejectsExpiredLocalDriveEntitlementWithoutDeletingCachedData() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement().copy(expiresAt = 1L)
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.upsertCustomer(any()) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        val documentSlot = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(documentSlot)) } answers {
            coEvery { dao.getDocument(userId, documentSlot.captured.id) } returns documentSlot.captured
            Unit
        }

        val failure = runCatching {
            repository.createDocument(
                CreateDocumentRequest(
                    type = DocumentType.Invoice,
                    customer = DocumentCustomerInput("Offline customer", "1234567"),
                    items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                ),
            )
        }.exceptionOrNull()

        assertEquals("ENTITLEMENT_INITIALIZATION_REQUIRED", failure?.message)
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
        coVerify(exactly = 0) { dao.deleteDocuments(any()) }
    }

    @Test
    fun clearTransientSessionState_doesNotDeleteRoomData() {
        repository.clearTransientSessionState()

        coVerify(exactly = 0) { dao.clearBusinessSettings() }
        coVerify(exactly = 0) { dao.clearCustomers() }
        coVerify(exactly = 0) { dao.clearProducts() }
        coVerify(exactly = 0) { dao.clearDocuments() }
        coVerify(exactly = 0) { dao.clearCreationEvents() }
    }

    @Test
    fun createCustomerLocal_rejectsLocalDriveWhenActiveLimitReached() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.countActiveCustomers(userId) } returns 5

        val result = repository.createCustomerLocal(Customer(name = "Sixth", whatsappNumber = "555"))

        assertTrue(result.isFailure)
        assertEquals("CUSTOMER_LIMIT_REACHED", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.upsertCustomer(any()) }
    }

    @Test
    fun createProductLocal_rejectsLocalDriveWhenActiveLimitReached() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.countActiveProducts(userId) } returns 5

        val result = repository.createProductLocal(
            Product(kind = ProductKind.Product, name = "Sixth", price = 10.0, currency = "SAR"),
        )

        assertTrue(result.isFailure)
        assertEquals("PRODUCT_LIMIT_REACHED", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.upsertProduct(any()) }
    }

    @Test
    fun localDriveCustomerDelete_isSoftAndRestorableEvenWhenReferenced() = runBlocking {
        val customer = CustomerEntity(
            id = "customer-local-drive",
            userId = userId,
            name = "Customer",
            whatsappNumber = "555",
            city = null,
            notes = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            isDeleted = false,
        )
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.getCustomer(userId, customer.id) } returns customer andThen customer.copy(isDeleted = true, localRevision = 2)
        coEvery { dao.countActiveCustomers(userId) } returns 4

        repository.deleteCustomerLocal(customer.id).getOrThrow()
        repository.restoreCustomerLocal(customer.id).getOrThrow()

        coVerify(exactly = 0) { dao.countDocumentsForCustomer(customer.id) }
        coVerify(exactly = 0) { dao.deleteCustomer(userId, customer.id) }
        coVerify(exactly = 1) { dao.upsertDeletedRecord(match { it.entityType == "customer" && it.entityId == customer.id }) }
        coVerify(exactly = 1) { dao.deleteDeletedRecord(userId, "customer", customer.id) }
        coVerify(exactly = 2) { dao.upsertCustomer(any()) }
    }

    @Test
    fun localDriveDocumentRestore_doesNotConsumeAnotherCredit() = runBlocking {
        val documentId = "restorable-document"
        val document = DocumentEntity(
            id = documentId,
            userId = userId,
            customerId = "customer",
            type = "invoice",
            documentNumber = "INV-00001",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-07-18",
            total = BigDecimal("10.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            isDeleted = true,
        )
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement()
        coEvery { dao.getDocument(userId, documentId) } returns document

        val result = repository.restoreDocumentLocal(documentId)

        assertTrue(result.ok)
        coVerify(exactly = 1) { dao.upsertDocument(match { !it.isDeleted && it.id == documentId }) }
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
        coVerify(exactly = 0) { dao.deleteCreationEventsForUser(any()) }
    }

    @Test
    fun remoteIngestion_replacesMissingCustomerAndSyncedProduct() = runBlocking {
        coEvery { dao.getCustomer(userId, "cust_remote") } returns null
        val customerSlot = slot<List<CustomerEntity>>()
        coEvery { dao.upsertCustomers(capture(customerSlot)) } returns Unit

        TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeCustomersList = listOf(Customer(id = "cust_remote", name = "Remote customer", whatsappNumber = "555")),
        ).refreshCustomers()

        assertEquals("Remote customer", customerSlot.captured.single().name)

        coEvery { dao.getProduct(userId, "prod_synced") } returns ProductEntity(
            id = "prod_synced",
            userId = userId,
            kind = "product",
            name = "Old product",
            description = null,
            price = BigDecimal("1.00"),
            currency = "SAR",
            stockQuantity = null,
            syncedAt = 0L,
            syncStatus = "SYNCED",
        )
        val productSlot = slot<List<ProductEntity>>()
        coEvery { dao.upsertProducts(capture(productSlot)) } returns Unit

        TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeProductsList = listOf(
                Product(
                    id = "prod_synced",
                    userId = userId,
                    kind = ProductKind.Product,
                    name = "Remote product",
                    price = 12.0,
                    currency = "SAR",
                    stockQuantity = 4,
                ),
            ),
        ).refreshProducts()

        assertEquals("Remote product", productSlot.captured.single().name)
    }

    @Test
    fun remoteIngestion_preservesBusinessSettingsAndDocumentTerminalStates() = runBlocking {
        coEvery { dao.getBusinessSettings(userId) } returns BusinessSettingsEntity(
            userId = userId,
            remoteId = "settings_remote",
            businessName = "Local business",
            whatsappNumber = "555",
            country = "SA",
            city = null,
            currency = "SAR",
            logoUrl = null,
            instagramUrl = null,
            invoiceNote = null,
            termsText = null,
            syncedAt = 0L,
            syncStatus = "failed_non_retryable",
        )
        coEvery { dao.upsertBusinessSettings(any()) } returns Unit

        TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeBusinessSettings = BusinessSettings(
                id = "settings_remote",
                businessName = "Remote business",
                whatsappNumber = "777",
                country = "SA",
                city = null,
                currency = "SAR",
            ),
        ).refreshBusinessSettings()

        coVerify(exactly = 0) { dao.upsertBusinessSettings(any()) }

        coEvery { dao.getDocument(userId, "doc_blocked") } returns DocumentEntity(
            id = "doc_blocked",
            userId = userId,
            customerId = "cust",
            type = "invoice",
            documentNumber = "INV-1",
            status = "draft",
            paymentStatus = null,
            amountPaid = null,
            issueDate = "2026-07-01",
            total = BigDecimal("10.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "BLOCKED_BY_PLAN",
        )
        coEvery { dao.upsertDocuments(any()) } returns Unit

        TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeDocumentsList = listOf(
                DocumentSummary(
                    id = "doc_blocked",
                    customerId = "cust",
                    type = DocumentType.Invoice,
                    documentNumber = "INV-2",
                    status = "draft",
                    issueDate = "2026-07-01",
                    total = 12.0,
                    currency = "SAR",
                ),
            ),
        ).refreshDocuments()

        coVerify(exactly = 0) { dao.upsertDocuments(any()) }
    }

    @Test
    fun finalizeOrVerifyQuota_succeedsIfQuotaAvailable() = runBlocking {
        val documentId = "doc_test_123"
        val existingDoc = app.tijario.data.local.DocumentEntity(
            id = documentId,
            userId = userId,
            customerId = "cust_123",
            type = "invoice",
            documentNumber = "INV-001",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-06-27",
            total = BigDecimal("100.00"),
            currency = "SAR",
            syncedAt = 0L
        )

        coEvery { dao.getDocument(userId, documentId) } returns existingDoc
        coEvery { dao.getCreationEventByDocument(userId, documentId) } returns null
        
        val lease = app.tijario.data.local.OfflineQuotaLeaseEntity(
            id = "lease_1",
            userId = userId,
            deviceId = android.os.Build.MODEL + "_" + android.os.Build.ID,
            planCode = "free",
            periodMonth = java.util.Date().toInstant().toString().substring(0, 7) + "-01",
            allowedLimit = 5,
            consumedCount = 1,
            expiresAt = System.currentTimeMillis() + 100_000,
            status = "ACTIVE"
        )
        
        coEvery { dao.getLease(userId, any(), any()) } returns lease
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.insertCreationEvent(any()) } returns 1L
        coEvery { dao.upsertDocument(any()) } returns Unit
        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        coEvery { dao.upsertOutbox(any()) } returns Unit

        val result = repository.finalizeOrVerifyQuota(documentId)
        assertTrue(result.isSuccess)
    }

    @Test
    fun finalizeOrVerifyQuota_throwsExceptionIfQuotaExceeded() = runBlocking {
        val documentId = "doc_test_456"
        val existingDoc = app.tijario.data.local.DocumentEntity(
            id = documentId,
            userId = userId,
            customerId = "cust_123",
            type = "invoice",
            documentNumber = "INV-002",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-06-27",
            total = BigDecimal("100.00"),
            currency = "SAR",
            syncedAt = 0L
        )

        coEvery { dao.getDocument(userId, documentId) } returns existingDoc
        coEvery { dao.getCreationEventByDocument(userId, documentId) } returns null
        
        val lease = app.tijario.data.local.OfflineQuotaLeaseEntity(
            id = "lease_2",
            userId = userId,
            deviceId = android.os.Build.MODEL + "_" + android.os.Build.ID,
            planCode = "free",
            periodMonth = java.util.Date().toInstant().toString().substring(0, 7) + "-01",
            allowedLimit = 5,
            consumedCount = 5,
            expiresAt = System.currentTimeMillis() + 100_000,
            status = "ACTIVE"
        )
        
        coEvery { dao.getLease(userId, any(), any()) } returns lease
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()

        val result = repository.finalizeOrVerifyQuota(documentId)
        assertTrue(result.isFailure)
        assertEquals("QUOTA_LIMIT_EXCEEDED", result.exceptionOrNull()?.message)
    }

    @Test
    fun finalizeOrVerifyQuota_isIdempotentForExistingCreationEvent() = runBlocking {
        val documentId = "doc_already_counted"
        coEvery { dao.getDocument(userId, documentId) } returns DocumentEntity(
            id = documentId,
            userId = userId,
            customerId = "cust_123",
            type = "invoice",
            documentNumber = "INV-003",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-07-18",
            total = BigDecimal("100.00"),
            currency = "SAR",
            syncedAt = 0L,
        )
        coEvery { dao.getCreationEventByDocument(userId, documentId) } returns DocumentCreationEventEntity(
            id = "event-1",
            userId = userId,
            documentId = documentId,
            operationId = "operation-1",
            installationId = "installation-1",
            leaseId = "lease-1",
            planCode = "free",
            quotaScope = "lifetime",
            periodKey = "lifetime",
            status = "ACKNOWLEDGED",
            createdAtClient = 1L,
            acknowledgedAtServer = 2L,
            entitlementVersion = 1L,
            source = "local_create",
        )
        coEvery { dao.getPendingOutbox(userId) } returns emptyList()
        coEvery { dao.upsertOutbox(any()) } returns Unit
        coEvery { dao.upsertDocument(any()) } returns Unit

        val result = repository.finalizeOrVerifyQuota(documentId)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
    }

    @Test
    fun deleteAccountLocal_purgesUserData() = runBlocking {
        repository.deleteAccountLocal(userId)
        coVerify(exactly = 1) {
            dao.deleteBusinessSettings(userId)
            dao.deleteCustomers(userId)
            dao.deleteProducts(userId)
            dao.deleteDocuments(userId)
            dao.deleteOutboxForUser(userId)
            dao.deleteLeasesForUser(userId)
            dao.deleteCreationEventsForUser(userId)
        }
    }
}
