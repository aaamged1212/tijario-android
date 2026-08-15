package app.tijario.data.repository

import android.content.Context
import app.tijario.config.AppPreferences
import app.tijario.data.local.TijarioDao
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.BusinessSettingsEntity
import app.tijario.data.local.CustomerEntity
import app.tijario.data.local.DocumentEntity
import app.tijario.data.local.DocumentCreationEventEntity
import app.tijario.data.local.AccountEntitlementEntity
import app.tijario.data.local.OfflineQuotaLeaseEntity
import app.tijario.data.local.ProductEntity
import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.Customer
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.DocumentItem
import app.tijario.data.model.DocumentSummary
import app.tijario.data.model.DocumentType
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import app.tijario.data.remote.BackendApiClient
import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.DocumentCustomerInput
import app.tijario.data.remote.DocumentItemInput
import app.tijario.data.remote.OfflineLeaseRequest
import io.github.jan.supabase.SupabaseClient
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
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
        // Legacy server data mode must still use the same Room-only operational policy.
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement().copy(dataMode = "legacy_cloud")
        repository = TestableTijarioRepository(context, database, supabaseClient, backendApiClient, userId)
    }

    @Test
    fun createCustomer_legacyCloudWritesToRoomWithoutOperationalOutbox() = runBlocking {
        val customer = Customer(name = "عميل جديد", whatsappNumber = "1234567")

        val slotEntity = slot<CustomerEntity>()
        coEvery { dao.upsertCustomer(capture(slotEntity)) } returns Unit

        val result = repository.createCustomer(customer).getOrThrow()

        // Verify entity details
        assertEquals("LOCAL_ONLY", slotEntity.captured.syncStatus)
        assertEquals(1, slotEntity.captured.localRevision)
        assertEquals("عميل جديد", slotEntity.captured.name)
        assertEquals(userId, slotEntity.captured.userId)
        assertTrue(slotEntity.captured.id.isNotBlank())

        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun createDocumentLocal_doesNotMergeCustomerByWhatsapp() = runBlocking {
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getBusinessSettings(userId) } returns BusinessSettingsEntity(
            userId = userId,
            remoteId = null,
            businessName = "Store",
            whatsappNumber = "555",
            country = "SA",
            city = null,
            currency = "YER",
            logoUrl = null,
            instagramUrl = null,
            invoiceNote = null,
            termsText = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
        )
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
                documentNumber = "INV-00101",
                customer = DocumentCustomerInput("New recipient", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertEquals("INV-00101", result.data?.documentNumber)
        assertEquals("INV-00101", documentSlot.captured.documentNumber)
        assertEquals("YER", documentSlot.captured.currency)
        assertEquals("1234567", customerSlot.captured.whatsappNumber)
        assertEquals(customerSlot.captured.id, documentSlot.captured.customerId)
        assertTrue(documentSlot.captured.createdAt?.contains("T") == true)
    }

    @Test
    fun updateDocumentLocal_succeedsWithoutCachedEntitlementAndKeepsRoomOnlyState() = runBlocking {
        val existing = DocumentEntity(
            id = "offline-edit-document",
            userId = userId,
            customerId = "customer-1",
            type = "invoice",
            documentNumber = "INV-00007",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-13",
            createdAt = "2026-08-13T08:00:00Z",
            total = BigDecimal("10.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 3,
        )
        coEvery { dao.getAccountEntitlement(userId) } returns null
        coEvery { dao.getDocument(userId, existing.id) } returns existing
        coEvery { dao.deleteDocumentItems(userId, existing.id) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        val savedDocument = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(savedDocument)) } returns Unit

        val result = repository.updateDocumentLocal(
            existing.id,
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Customer", "555"),
                items = listOf(DocumentItemInput(name = "Updated item", quantity = 2, unitPrice = 15.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertEquals(existing.documentNumber, result.data?.documentNumber)
        assertEquals("LOCAL_ONLY", savedDocument.captured.syncStatus)
        assertEquals(existing.localRevision + 1, savedDocument.captured.localRevision)
        assertEquals(existing.createdAt, savedDocument.captured.createdAt)
        coVerifyOrder {
            dao.upsertDocument(any())
            dao.deleteDocumentItems(userId, existing.id)
            dao.insertDocumentItems(any())
        }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun fetchCompleteDocument_usesCompleteRoomSnapshotWithoutNetwork() = runBlocking {
        val localDocument = DocumentEntity(
            id = "local-document",
            userId = userId,
            customerId = "customer-1",
            type = "invoice",
            documentNumber = "INV-00008",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-15",
            total = BigDecimal("20.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
        )
        coEvery { dao.getDocument(userId, localDocument.id) } returns localDocument
        coEvery { dao.getDocumentItems(userId, localDocument.id) } returns listOf(
            app.tijario.data.local.DocumentItemEntity(
                id = "item-1",
                userId = userId,
                documentId = localDocument.id,
                productId = null,
                name = "Local item",
                description = null,
                quantity = 1,
                unitPrice = BigDecimal("20.00"),
                lineTotal = BigDecimal("20.00"),
                sortOrder = 0,
            ),
        )

        val result = repository.fetchCompleteDocument(localDocument.id)

        assertTrue(result.isSuccess)
        assertEquals("Local item", result.getOrThrow().items.single().name)
        coVerify(exactly = 0) { backendApiClient.fetchCompleteDocument(any()) }
    }

    @Test
    fun fetchCompleteDocument_hydratesLegacySyncedSummaryWithMissingItems() = runBlocking {
        val localDocument = DocumentEntity(
            id = "legacy-document",
            userId = userId,
            customerId = "customer-1",
            type = "invoice",
            documentNumber = "INV-00009",
            status = "issued",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-15",
            total = BigDecimal("20.00"),
            currency = "SAR",
            syncedAt = 1L,
            syncStatus = "SYNCED",
        )
        val remoteDocument = CompleteDocument(
            id = localDocument.id,
            userId = userId,
            customerId = localDocument.customerId,
            type = DocumentType.Invoice,
            documentNumber = localDocument.documentNumber,
            status = "issued",
            paymentStatus = "unpaid",
            issueDate = localDocument.issueDate,
            subtotal = 20.0,
            discount = 0.0,
            extraFees = 0.0,
            total = 20.0,
            currency = "SAR",
            items = listOf(
                DocumentItem(
                    id = "remote-item",
                    documentId = localDocument.id,
                    name = "Hydrated item",
                    quantity = 1,
                    unitPrice = 20.0,
                ),
            ),
        )
        coEvery { dao.getDocument(userId, localDocument.id) } returns localDocument
        coEvery { dao.getDocumentItems(userId, localDocument.id) } returns emptyList()
        coEvery { backendApiClient.fetchCompleteDocument(localDocument.id) } returns ApiResult(
            ok = true,
            data = remoteDocument,
        )
        coEvery { dao.upsertDocument(any()) } returns Unit
        coEvery { dao.deleteDocumentItems(userId, localDocument.id) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit

        val result = repository.fetchCompleteDocument(localDocument.id)

        assertEquals("Hydrated item", result.getOrThrow().items.single().name)
        coVerify(exactly = 1) { backendApiClient.fetchCompleteDocument(localDocument.id) }
        coVerify(exactly = 1) { dao.insertDocumentItems(match { it.single().name == "Hydrated item" }) }
    }

    @Test
    fun fetchCompleteDocument_doesNotReplaceIncompleteUnsyncedLocalDocument() = runBlocking {
        val localDocument = DocumentEntity(
            id = "protected-document",
            userId = userId,
            customerId = "customer-1",
            type = "invoice",
            documentNumber = "INV-00010",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-15",
            total = BigDecimal("20.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
        )
        coEvery { dao.getDocument(userId, localDocument.id) } returns localDocument
        coEvery { dao.getDocumentItems(userId, localDocument.id) } returns emptyList()

        val result = repository.fetchCompleteDocument(localDocument.id)

        assertTrue(result.isFailure)
        assertEquals("MISSING_DOCUMENT_ITEMS", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { backendApiClient.fetchCompleteDocument(any()) }
    }

    @Test
    fun fetchCompleteDocument_recoversServerBackedDocumentWhoseItemsWereLostAfterLocalEdit() = runBlocking {
        val localDocument = DocumentEntity(
            id = "recoverable-document",
            userId = userId,
            customerId = "customer-1",
            type = "invoice",
            documentNumber = "INV-00011",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-15",
            total = BigDecimal("20.00"),
            currency = "SAR",
            syncedAt = 1L,
            syncStatus = "LOCAL_ONLY",
            serverRevision = "2026-08-15T10:00:00Z",
        )
        val remoteDocument = CompleteDocument(
            id = localDocument.id,
            userId = userId,
            customerId = localDocument.customerId,
            type = DocumentType.Invoice,
            documentNumber = localDocument.documentNumber,
            status = "issued",
            paymentStatus = "unpaid",
            issueDate = localDocument.issueDate,
            subtotal = 20.0,
            discount = 0.0,
            extraFees = 0.0,
            total = 20.0,
            currency = "SAR",
            items = listOf(
                DocumentItem(
                    id = "recovered-item",
                    documentId = localDocument.id,
                    name = "Recovered item",
                    quantity = 1,
                    unitPrice = 20.0,
                ),
            ),
        )
        coEvery { dao.getDocument(userId, localDocument.id) } returns localDocument
        coEvery { dao.getDocumentItems(userId, localDocument.id) } returns emptyList()
        coEvery { backendApiClient.fetchCompleteDocument(localDocument.id) } returns ApiResult(
            ok = true,
            data = remoteDocument,
        )
        coEvery { dao.upsertDocument(any()) } returns Unit
        coEvery { dao.deleteDocumentItems(userId, localDocument.id) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit

        val result = repository.fetchCompleteDocument(localDocument.id)

        assertEquals("Recovered item", result.getOrThrow().items.single().name)
        coVerify(exactly = 1) { backendApiClient.fetchCompleteDocument(localDocument.id) }
        coVerify(exactly = 1) { dao.insertDocumentItems(match { it.single().id == "recovered-item" }) }
    }

    @Test
    fun createDocumentLocal_rejectsDuplicateCustomNumberBeforeWritingRoomRows() = runBlocking {
        every { dao.observeDocuments(userId) } returns flowOf(
            listOf(
                DocumentEntity(
                    id = "existing-invoice",
                    userId = userId,
                    customerId = "existing-customer",
                    type = "invoice",
                    documentNumber = "INV-00101",
                    status = "draft",
                    paymentStatus = "unpaid",
                    amountPaid = null,
                    issueDate = "2026-08-01",
                    total = BigDecimal("10.00"),
                    currency = "SAR",
                    syncedAt = 0L,
                ),
            ),
        )

        val result = repository.createDocumentLocal(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                documentNumber = "INV-00101",
                customer = DocumentCustomerInput("Recipient", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(!result.ok)
        assertEquals("document_number_collision", result.code)
        coVerify(exactly = 0) { dao.upsertDocument(any()) }
        coVerify(exactly = 0) { dao.insertDocumentItems(any()) }
    }

    @Test
    fun saveBusinessSettings_persistsLocallyWhenEntitlementHasNotBootstrapped() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns null
        coEvery { dao.getBusinessSettings(userId) } returns null
        val settingsSlot = slot<BusinessSettingsEntity>()
        coEvery { dao.upsertBusinessSettings(capture(settingsSlot)) } returns Unit

        val result = repository.saveBusinessSettings(
            BusinessSettings(
                businessName = "Offline store",
                whatsappNumber = "555",
                country = "SA",
                currency = "YER",
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals("YER", settingsSlot.captured.currency)
        assertEquals("LOCAL_ONLY", settingsSlot.captured.syncStatus)
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun nextDocumentNumber_usesLegacyCloudRoomHistoryWithoutRemoteRequest() = runBlocking {
        every { dao.observeDocuments(userId) } returns flowOf(
            listOf(
                DocumentEntity(
                    id = "invoice-previous",
                    userId = userId,
                    customerId = "customer-1",
                    type = "invoice",
                    documentNumber = "INV-00041",
                    status = "draft",
                    paymentStatus = null,
                    amountPaid = null,
                    issueDate = "2026-08-13",
                    total = BigDecimal("10.00"),
                    currency = "SAR",
                    syncedAt = 0L,
                ),
            ),
        )

        val result = repository.getNextDocumentNumber("invoice")

        assertTrue(result.ok)
        assertEquals("INV-00042", result.data?.documentNumber)
        coVerify(exactly = 0) { backendApiClient.getNextDocumentNumber(any()) }
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
        coEvery { dao.upsertCustomer(capture(customerSlot)) } returns Unit
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
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun updateCustomer_legacyCloudRemainsLocalOnlyWithoutOutbox() = runBlocking {
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

        val updateData = Customer(id = existingId, name = "عميل محدث", whatsappNumber = "12345")
        repository.updateCustomer(updateData).getOrThrow()

        // Verify entity fields
        assertEquals(3, slotEntity.captured.localRevision)
        assertEquals("LOCAL_ONLY", slotEntity.captured.syncStatus)
        assertEquals("عميل محدث", slotEntity.captured.name)
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
    fun createCustomer_routesLegacyCloudToRoomWithoutOperationalOutbox() = runBlocking {
        coEvery { dao.upsertCustomer(any()) } returns Unit

        val result = repository.createCustomer(Customer(name = "Offline customer", whatsappNumber = "1234567"))

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { dao.upsertCustomer(match { it.syncStatus == "LOCAL_ONLY" }) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun updateCustomer_routesLegacyCloudToRoomWithoutOperationalOutbox() = runBlocking {
        val existing = CustomerEntity(
            id = "offline-customer",
            userId = userId,
            name = "Before",
            whatsappNumber = "111",
            city = null,
            notes = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 2,
            isDeleted = false,
        )
        coEvery { dao.getCustomer(userId, existing.id) } returns existing
        val saved = slot<CustomerEntity>()
        coEvery { dao.upsertCustomer(capture(saved)) } returns Unit

        val result = repository.updateCustomer(
            Customer(id = existing.id, name = "After", whatsappNumber = "222"),
        )

        assertTrue(result.isSuccess)
        assertEquals("LOCAL_ONLY", saved.captured.syncStatus)
        assertEquals(3, saved.captured.localRevision)
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun createProductAndService_routeLegacyCloudToRoomWithoutOperationalOutbox() = runBlocking {
        coEvery { dao.getBusinessSettings(userId) } returns BusinessSettingsEntity(
            userId = userId,
            remoteId = null,
            businessName = "Store",
            whatsappNumber = "111",
            country = "SA",
            city = null,
            currency = "SAR",
            logoUrl = null,
            instagramUrl = null,
            invoiceNote = null,
            termsText = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
        )
        coEvery { dao.upsertProduct(any()) } returns Unit

        val product = repository.createProduct(
            Product(id = "offline-product", kind = ProductKind.Product, name = "Product", price = 10.0, currency = "USD"),
        )
        val service = repository.createProduct(
            Product(id = "offline-service", kind = ProductKind.Service, name = "Service", price = 20.0, currency = "USD"),
        )

        assertTrue(product.isSuccess)
        assertTrue(service.isSuccess)
        coVerify(exactly = 2) { dao.upsertProduct(match { it.syncStatus == "LOCAL_ONLY" && it.currency == "USD" }) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun increaseProductStock_updatesOnlyTheLocalProduct() = runBlocking {
        val existing = ProductEntity(
            id = "stock-product",
            userId = userId,
            kind = "product",
            name = "Tracked product",
            description = null,
            price = BigDecimal("10.00"),
            currency = "SAR",
            stockQuantity = 3,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 4,
            isDeleted = false,
        )
        coEvery { dao.getProduct(userId, existing.id) } returns existing
        val saved = slot<ProductEntity>()
        coEvery { dao.upsertProduct(capture(saved)) } returns Unit

        val result = repository.increaseProductStock(existing.id, 2)

        assertTrue(result.isSuccess)
        assertEquals(5, saved.captured.stockQuantity)
        assertEquals(5, saved.captured.localRevision)
        assertEquals("LOCAL_ONLY", saved.captured.syncStatus)
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun updateProduct_routesLegacyCloudToRoomWithoutOperationalOutbox() = runBlocking {
        val existing = ProductEntity(
            id = "offline-product",
            userId = userId,
            kind = "product",
            name = "Before",
            description = null,
            price = BigDecimal("10.00"),
            currency = "SAR",
            stockQuantity = 2,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 2,
            isDeleted = false,
        )
        coEvery { dao.getProduct(userId, existing.id) } returns existing
        val saved = slot<ProductEntity>()
        coEvery { dao.upsertProduct(capture(saved)) } returns Unit

        val result = repository.updateProduct(
            Product(id = existing.id, kind = ProductKind.Service, name = "After", price = 25.0, currency = "SAR"),
        )

        assertTrue(result.isSuccess)
        assertEquals("LOCAL_ONLY", saved.captured.syncStatus)
        assertEquals(3, saved.captured.localRevision)
        assertEquals("service", saved.captured.kind)
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun createDocument_legacyCloudWithoutLeaseSavesInvoiceWithoutNetwork() = runBlocking {
        coEvery { dao.getActiveLease(userId, any(), any()) } returns null
        coEvery { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) } throws IOException("offline")
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.upsertCustomer(any()) } returns Unit
        val document = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(document)) } answers {
            coEvery { dao.getDocument(userId, document.captured.id) } returns document.captured
            Unit
        }
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        val event = slot<DocumentCreationEventEntity>()
        coEvery { dao.insertCreationEvent(capture(event)) } returns 1L

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertNull(event.captured.leaseId)
        coVerify(exactly = 0) { backendApiClient.createDocument(any()) }
        coVerify(exactly = 0) { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
        coVerify(exactly = 1) { dao.upsertCustomer(any()) }
        coVerify(exactly = 1) { dao.upsertDocument(any()) }
        coVerify(exactly = 1) { dao.insertDocumentItems(any()) }
        coVerify(exactly = 1) { dao.insertCreationEvent(any()) }
    }

    @Test
    fun createDocument_legacyCloudWithoutLeaseSavesQuoteWithoutNetwork() = runBlocking {
        coEvery { dao.getActiveLease(userId, any(), any()) } returns null
        coEvery { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) } throws IOException("offline")
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.upsertCustomer(any()) } returns Unit
        val document = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(document)) } answers {
            coEvery { dao.getDocument(userId, document.captured.id) } returns document.captured
            Unit
        }
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        val event = slot<DocumentCreationEventEntity>()
        coEvery { dao.insertCreationEvent(capture(event)) } returns 1L

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Quote,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertNull(event.captured.leaseId)
        coVerify(exactly = 0) { backendApiClient.createDocument(any()) }
        coVerify(exactly = 0) { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
        coVerify(exactly = 1) { dao.upsertDocument(any()) }
        coVerify(exactly = 1) { dao.insertDocumentItems(any()) }
    }

    @Test
    fun createDocument_legacyCloudEnforcesCachedLimitWithoutNetworkOrPartialRows() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement(documentsUsed = 5).copy(dataMode = "legacy_cloud")
        coEvery { dao.getActiveLease(userId, any(), any()) } returns null
        coEvery { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) } throws IOException("offline")
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.upsertCustomer(any()) } returns Unit
        val document = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(document)) } answers {
            coEvery { dao.getDocument(userId, document.captured.id) } returns document.captured
            Unit
        }
        coEvery { dao.insertDocumentItems(any()) } returns Unit

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(!result.ok)
        assertEquals("DOCUMENT_LIMIT_REACHED", result.code)
        coVerify(exactly = 0) { backendApiClient.createDocument(any()) }
        coVerify(exactly = 0) { backendApiClient.requestOfflineLease(any<OfflineLeaseRequest>()) }
        coVerify(exactly = 0) { dao.upsertCustomer(any()) }
        coVerify(exactly = 0) { dao.upsertDocument(any()) }
        coVerify(exactly = 0) { dao.insertDocumentItems(any()) }
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
    }

    @Test
    fun createDocument_legacyCloudPersistsThePreparedLeaseId() = runBlocking {
        val installationId = AppPreferences.getInstallationId(context)
        val lease = validOfflineQuotaLease().copy(
            deviceId = installationId,
            entitlementVersion = 1L,
            periodMonth = "lifetime",
        )
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement().copy(dataMode = "legacy_cloud")
        coEvery { dao.getActiveLease(userId, installationId, any()) } returns lease
        every { dao.observeDocuments(userId) } returns flowOf(emptyList())
        coEvery { dao.getPendingCreationEvents(userId) } returns emptyList()
        coEvery { dao.getCreationEventByDocument(userId, any()) } returns null
        coEvery { dao.upsertCustomer(any()) } returns Unit
        coEvery { dao.upsertDocument(any()) } answers {
            val saved = firstArg<DocumentEntity>()
            coEvery { dao.getDocument(userId, saved.id) } returns saved
            Unit
        }
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        val event = slot<DocumentCreationEventEntity>()
        coEvery { dao.insertCreationEvent(capture(event)) } returns 1L

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Quote,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
                currency = "SAR",
            ),
        )

        assertTrue(result.ok)
        assertEquals(lease.id, event.captured.leaseId)
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun updateDocument_legacyCloudStaysLocalOnlyWithoutOperationalOutbox() = runBlocking {
        val documentId = "offline-document"
        val existing = DocumentEntity(
            id = documentId,
            userId = userId,
            customerId = "customer",
            type = "invoice",
            documentNumber = "INV-00001",
            status = "draft",
            paymentStatus = "unpaid",
            amountPaid = null,
            issueDate = "2026-08-01",
            total = BigDecimal("10.00"),
            currency = "SAR",
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 2,
            isDeleted = false,
        )
        coEvery { dao.getDocument(userId, documentId) } returns existing
        coEvery { dao.deleteDocumentItems(userId, documentId) } returns Unit
        coEvery { dao.insertDocumentItems(any()) } returns Unit
        val saved = slot<DocumentEntity>()
        coEvery { dao.upsertDocument(capture(saved)) } returns Unit

        val result = repository.updateDocument(
            documentId,
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Customer", "111"),
                items = listOf(DocumentItemInput(name = "Updated service", quantity = 2, unitPrice = 15.0)),
                currency = "SAR",
                notes = "Updated locally",
            ),
        )

        assertTrue(result.ok)
        assertEquals("LOCAL_ONLY", saved.captured.syncStatus)
        assertEquals(3, saved.captured.localRevision)
        coVerify(exactly = 0) { backendApiClient.updateDocument(any(), any()) }
        coVerify(exactly = 0) { dao.upsertOutbox(any()) }
    }

    @Test
    fun acknowledgedQuotaCreationEventConsumesItsLeaseExactlyOnce() = runBlocking {
        val event = DocumentCreationEventEntity(
            id = "event_ack",
            userId = userId,
            documentId = "doc_ack",
            operationId = "operation_ack",
            installationId = "test_installation",
            leaseId = "lease_ack",
            planCode = "free",
            quotaScope = "lifetime",
            periodKey = "lifetime",
            status = "PENDING",
            createdAtClient = 1L,
            acknowledgedAtServer = null,
            entitlementVersion = 1L,
            source = "local_create",
        )
        coEvery { dao.getCreationEventByDocument(userId, event.documentId) } returns event
        coEvery { dao.acknowledgeCreationEvent(userId, event.documentId, any()) } returns 1
        coEvery { dao.consumeLeaseCredit(userId, "lease_ack") } returns 1

        assertTrue(repository.acknowledgeCreationEventAndConsumeLease(userId, event.documentId, 2L))
        coVerify(exactly = 1) { dao.consumeLeaseCredit(userId, "lease_ack") }

        coEvery { dao.acknowledgeCreationEvent(userId, event.documentId, any()) } returns 0
        assertTrue(!repository.acknowledgeCreationEventAndConsumeLease(userId, event.documentId, 3L))
        coVerify(exactly = 1) { dao.consumeLeaseCredit(userId, "lease_ack") }
    }

    @Test
    fun acknowledgedQuotaCreationEventCannotReuseAnExhaustedLease() = runBlocking {
        val event = DocumentCreationEventEntity(
            id = "event_exhausted",
            userId = userId,
            documentId = "doc_exhausted",
            operationId = "operation_exhausted",
            installationId = "test_installation",
            leaseId = "lease_exhausted",
            planCode = "free",
            quotaScope = "lifetime",
            periodKey = "lifetime",
            status = "PENDING",
            createdAtClient = 1L,
            acknowledgedAtServer = null,
            entitlementVersion = 1L,
            source = "local_create",
        )
        coEvery { dao.getCreationEventByDocument(userId, event.documentId) } returns event
        coEvery { dao.acknowledgeCreationEvent(userId, event.documentId, any()) } returns 1
        coEvery { dao.consumeLeaseCredit(userId, "lease_exhausted") } returns 0

        val failure = runCatching {
            repository.acknowledgeCreationEventAndConsumeLease(userId, event.documentId, 2L)
        }.exceptionOrNull()
        assertEquals("OFFLINE_LEASE_EXHAUSTED", failure?.message)
        coVerify(exactly = 1) { dao.consumeLeaseCredit(userId, "lease_exhausted") }
    }

    @Test
    fun createDocument_rejectsExpiredLegacyCloudEntitlementWithoutDeletingCachedData() = runBlocking {
        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement().copy(dataMode = "legacy_cloud", expiresAt = 1L)
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

        val result = repository.createDocument(
            CreateDocumentRequest(
                type = DocumentType.Invoice,
                customer = DocumentCustomerInput("Offline customer", "1234567"),
                items = listOf(DocumentItemInput(name = "Service", quantity = 1, unitPrice = 10.0)),
            ),
        )

        assertTrue(!result.ok)
        assertEquals("ENTITLEMENT_INITIALIZATION_REQUIRED", result.code)
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
        coVerify(exactly = 0) { dao.upsertCustomer(any()) }
        coVerify(exactly = 0) { dao.upsertDocument(any()) }
        coVerify(exactly = 0) { dao.insertDocumentItems(any()) }
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
    fun createCustomer_rejectsLegacyCloudWhenActiveLimitReached() = runBlocking {
        coEvery { dao.countActiveCustomers(userId) } returns 5

        val result = repository.createCustomer(Customer(name = "Sixth", whatsappNumber = "555"))

        assertTrue(result.isFailure)
        assertEquals("CUSTOMER_LIMIT_REACHED", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.upsertCustomer(any()) }
    }

    @Test
    fun createProduct_rejectsLegacyCloudWhenActiveLimitReached() = runBlocking {
        coEvery { dao.countActiveProducts(userId) } returns 5

        val result = repository.createProduct(
            Product(kind = ProductKind.Product, name = "Sixth", price = 10.0, currency = "SAR"),
        )

        assertTrue(result.isFailure)
        assertEquals("PRODUCT_LIMIT_REACHED", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { dao.upsertProduct(any()) }
    }

    @Test
    fun legacyCloudCustomerDelete_isSoftAndRestorableEvenWhenReferenced() = runBlocking {
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
        coEvery { dao.getCustomer(userId, customer.id) } returns customer andThen customer.copy(isDeleted = true, localRevision = 2)
        coEvery { dao.countActiveCustomers(userId) } returns 4

        repository.deleteCustomer(customer.id).getOrThrow()
        repository.restoreCustomer(customer.id).getOrThrow()

        coVerify(exactly = 0) { dao.countDocumentsForCustomer(customer.id) }
        coVerify(exactly = 0) { dao.deleteCustomer(userId, customer.id) }
        coVerify(exactly = 1) { dao.upsertDeletedRecord(match { it.entityType == "customer" && it.entityId == customer.id }) }
        coVerify(exactly = 1) { dao.deleteDeletedRecord(userId, "customer", customer.id) }
        coVerify(exactly = 2) { dao.upsertCustomer(any()) }
    }

    @Test
    fun legacyCloudDocumentRestore_doesNotConsumeAnotherCredit() = runBlocking {
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
        coEvery { dao.getDocument(userId, documentId) } returns document

        val result = repository.restoreDocument(documentId)

        assertTrue(result.ok)
        coVerify(exactly = 1) { dao.upsertDocument(match { !it.isDeleted && it.id == documentId }) }
        coVerify(exactly = 0) { dao.insertCreationEvent(any()) }
        coVerify(exactly = 0) { dao.deleteCreationEventsForUser(any()) }
    }

    @Test
    fun legacyCloudProductDeleteAndRestoreRemainRoomOnly() = runBlocking {
        val product = ProductEntity(
            id = "product-local",
            userId = userId,
            kind = "product",
            name = "Product",
            description = null,
            price = BigDecimal("12.00"),
            currency = "SAR",
            stockQuantity = null,
            syncedAt = 0L,
            syncStatus = "LOCAL_ONLY",
            localRevision = 1,
            isDeleted = false,
        )
        coEvery { dao.getProduct(userId, product.id) } returns product andThen product.copy(isDeleted = true, localRevision = 2)
        coEvery { dao.countActiveProducts(userId) } returns 4

        repository.deleteProduct(product.id).getOrThrow()
        repository.restoreProduct(product.id).getOrThrow()

        coVerify(exactly = 0) { dao.countDocumentItemsForProduct(product.id) }
        coVerify(exactly = 0) { dao.deleteProduct(userId, product.id) }
        coVerify(exactly = 1) { dao.upsertDeletedRecord(match { it.entityType == "product" && it.entityId == product.id }) }
        coVerify(exactly = 1) { dao.deleteDeletedRecord(userId, "product", product.id) }
        coVerify(exactly = 2) { dao.upsertProduct(any()) }
    }

    @Test
    fun refreshOperationalData_doesNotHydrateLegacyCloudRecordsIntoRoom() = runBlocking {
        val operationalRepository = TestableTijarioRepository(
            context,
            database,
            supabaseClient,
            backendApiClient,
            userId,
            fakeCustomersList = listOf(Customer(id = "cust_remote", name = "Remote customer", whatsappNumber = "555")),
            fakeProductsList = listOf(
                Product(
                    id = "prod_remote",
                    userId = userId,
                    kind = ProductKind.Product,
                    name = "Remote product",
                    price = 12.0,
                    currency = "SAR",
                ),
            ),
            fakeDocumentsList = listOf(
                DocumentSummary(
                    id = "doc_remote",
                    customerId = "cust_remote",
                    type = DocumentType.Invoice,
                    documentNumber = "INV-00001",
                    status = "draft",
                    issueDate = "2026-08-13",
                    total = 12.0,
                    currency = "SAR",
                ),
            ),
        )

        operationalRepository.refreshCustomers().getOrThrow()
        operationalRepository.refreshProducts().getOrThrow()
        operationalRepository.refreshDocuments().getOrThrow()

        coVerify(exactly = 0) { dao.upsertCustomers(any()) }
        coVerify(exactly = 0) { dao.upsertProducts(any()) }
        coVerify(exactly = 0) { dao.upsertDocuments(any()) }
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
    fun finalizeOrVerifyQuota_rejectsWhenCachedEntitlementUsageIsAtLimit() = runBlocking {
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

        coEvery { dao.getAccountEntitlement(userId) } returns localDriveEntitlement(documentsUsed = 5).copy(dataMode = "legacy_cloud")
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
