package app.tijario.data.repository

import app.tijario.data.local.TijarioDao
import app.tijario.data.local.TijarioDatabase
import app.tijario.data.local.CustomerEntity
import app.tijario.data.local.ProductEntity
import app.tijario.data.local.SyncOutboxEntity
import app.tijario.data.model.Customer
import app.tijario.data.model.Product
import app.tijario.data.model.ProductKind
import app.tijario.data.remote.BackendApiClient
import io.github.jan.supabase.SupabaseClient
import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class TijarioRepositoryOfflineTests {

    private val database = mockk<TijarioDatabase>(relaxed = true)
    private val dao = mockk<TijarioDao>(relaxed = true)
    private val supabaseClient = mockk<SupabaseClient>(relaxed = true)
    private val backendApiClient = mockk<BackendApiClient>(relaxed = true)

    private lateinit var repository: TijarioRepository
    private val userId = "test_user_123"

    // Subclass of repository under test to stub out active Supabase Auth & Remote Fetch connections
    private open class TestableTijarioRepository(
        database: TijarioDatabase,
        supabaseClient: SupabaseClient,
        backendApiClient: BackendApiClient,
        private val stubUserId: String,
        private val fakeProductsList: List<Product> = emptyList()
    ) : TijarioRepository(database, supabaseClient, backendApiClient) {
        
        override suspend fun currentUserId(): String? {
            return stubUserId
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
        repository = TestableTijarioRepository(database, supabaseClient, backendApiClient, userId)
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

        val ingestionRepo = TestableTijarioRepository(database, supabaseClient, backendApiClient, userId, remoteProducts)

        ingestionRepo.refreshProducts()

        // Verify that products cache update was never called because it is marked as PENDING_SYNC
        coVerify(exactly = 0) { dao.upsertProducts(any()) }
    }
}
