package app.tijario.features.backup

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.tijario.data.local.BusinessSettingsEntity
import app.tijario.data.local.CustomerEntity
import app.tijario.data.local.DocumentCreationEventEntity
import app.tijario.data.local.DocumentEntity
import app.tijario.data.local.DocumentItemEntity
import app.tijario.data.local.LocalDocumentMetadataEntity
import app.tijario.data.local.LocalPaymentMethodEntity
import app.tijario.data.local.LocalSignatureEntity
import app.tijario.data.local.LocalTaxEntity
import app.tijario.data.local.LocalTermsEntity
import app.tijario.data.local.ProductEntity
import app.tijario.data.local.TijarioDatabase
import app.tijario.features.backup.drive.DriveBackupClient
import app.tijario.features.backup.drive.DriveBackupFile
import app.tijario.features.backup.drive.DriveConnectionState
import app.tijario.features.backup.drive.DriveUploadMetadata
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal

/** Exercises the logical archive and assets together without an external Drive account. */
class LocalBackupRoundTripIntegrationTest {
    private lateinit var database: TijarioDatabase
    private lateinit var filesRoot: File
    private val userId = "backup-round-trip-user"
    private val key = ByteArray(32) { (it + 1).toByte() }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, TijarioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        filesRoot = File(context.cacheDir, "backup-round-trip-${System.nanoTime()}").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        database.close()
        filesRoot.deleteRecursively()
        key.fill(0)
    }

    @Test
    fun encryptedArchiveSurvivesFakeDriveDownloadAndRestoresRecordsAndAssets() = runBlocking {
        seedAccount()
        val creator = LocalBackupCreator(database, filesRoot)
        val record = creator.create(
            LocalBackupRequest(
                userId = userId,
                installationId = "install-a",
                sequence = 1L,
                applicationVersion = "test",
                minimumApplicationVersion = "test",
                keyVersion = 1,
                encryptionKey = key.copyOf(),
            ),
        )

        val fakeDrive = FakeDriveBackupClient()
        val drive = app.tijario.features.backup.drive.DriveBackupRepository(database, filesRoot, fakeDrive)
        val uploaded = drive.upload(userId, record.id)
        val downloaded = File(filesRoot, "downloaded.tijario")
        val remote = requireNotNull(fakeDrive.lastFile)
        drive.download(userId, remote, downloaded)
        assertEquals("DRIVE_UPLOADED", uploaded.status)

        RoomLogicalBackupStore.tableSpecs.asReversed().forEach { spec ->
            database.openHelper.writableDatabase.execSQL("DELETE FROM \"${spec.table}\" WHERE user_id = ?", arrayOf(userId))
        }
        deleteAssets()

        val restorer = LocalBackupRestorer(database, filesRoot)
        val decoded = restorer.validate(downloaded, key.copyOf(), userId, File(filesRoot, "restore-temp"))
        restorer.restore(decoded, userId)
        decoded.discardStaging()

        val dao = database.tijarioDao()
        assertEquals("Tijario Shop", dao.getBusinessSettings(userId)?.businessName)
        assertEquals("Customer One", dao.getCustomer(userId, "customer-1")?.name)
        assertEquals("Product One", dao.getProduct(userId, "product-1")?.name)
        assertEquals("INV-00001", dao.getDocument(userId, "document-1")?.documentNumber)
        assertEquals(1, dao.getDocumentItems(userId, "document-1").size)
        assertEquals(1, dao.observeLocalTaxes(userId).first().size)
        assertEquals(1, dao.observeLocalPaymentMethods(userId).first().size)
        assertEquals(1, dao.observeLocalSignatures(userId).first().size)
        assertEquals(1, dao.observeLocalTerms(userId).first().size)
        assertEquals("shipping", dao.getDocumentMetadata(userId, "document-1")?.shippingLabel)
        assertArrayEquals("logo".encodeToByteArray(), File(filesRoot, "users/$userId/business/logo/logo.png").readBytes())
        assertArrayEquals("image".encodeToByteArray(), File(filesRoot, "product_images/product-1.jpg").readBytes())
        assertArrayEquals("pdf".encodeToByteArray(), File(filesRoot, "documents/pdfs/$userId/document-1.pdf").readBytes())
    }

    private suspend fun seedAccount() {
        val dao = database.tijarioDao()
        dao.upsertBusinessSettings(BusinessSettingsEntity(userId, "remote", "Tijario Shop", "+9665", "SA", "Riyadh", currency = "SAR", logoUrl = null, instagramUrl = null, invoiceNote = null, termsText = null, syncedAt = 1L))
        dao.upsertCustomer(CustomerEntity("customer-1", userId, "Customer One", "+966500000", "Riyadh", "note", 1L))
        dao.upsertProduct(ProductEntity("product-1", userId, "product", "Product One", "desc", BigDecimal("19.50"), "SAR", 4, "goods", 1L))
        dao.upsertDocument(
            DocumentEntity(
                id = "document-1",
                userId = userId,
                customerId = "customer-1",
                type = "invoice",
                documentNumber = "INV-00001",
                status = "draft",
                paymentStatus = "unpaid",
                amountPaid = BigDecimal.ZERO,
                issueDate = "2026-07-29",
                total = BigDecimal("19.50"),
                currency = "SAR",
                syncedAt = 1L,
                subtotal = BigDecimal("19.50"),
                taxName = "VAT",
                taxRate = BigDecimal("15"),
                taxAmount = BigDecimal("2.93"),
                documentTitle = "Invoice",
                localPdfRelativePath = "documents/pdfs/$userId/document-1.pdf",
                pdfGenerationStatus = "available",
            ),
        )
        dao.insertDocumentItems(listOf(DocumentItemEntity("item-1", userId, "document-1", "product-1", "Product One", "desc", 1, BigDecimal("19.50"), BigDecimal("19.50"), 0)))
        dao.upsertLocalTax(LocalTaxEntity("tax-1", "VAT", 15.0, userId))
        dao.upsertLocalPaymentMethod(LocalPaymentMethodEntity("payment-1", "Cash", "counter", userId))
        dao.upsertLocalSignature(LocalSignatureEntity("signature-1", "Owner", "signature", userId))
        dao.upsertLocalTerms(LocalTermsEntity("terms-1", "Terms", "content", userId))
        dao.upsertDocumentMetadata(LocalDocumentMetadataEntity("document-1", "SAR", "signature", "Cash", 15.0, "VAT", "fixed", "0", 5.0, "shipping", userId))
        dao.insertCreationEvent(DocumentCreationEventEntity("event-1", userId, "document-1", "operation-1", "install-a", null, "free", "lifetime", "lifetime", "PENDING", 1L, null, 1L, "local"))
        File(filesRoot, "users/$userId/business/logo").apply { mkdirs() }.resolve("logo.png").writeBytes("logo".encodeToByteArray())
        File(filesRoot, "product_images").apply { mkdirs() }.resolve("product-1.jpg").writeBytes("image".encodeToByteArray())
        File(filesRoot, "documents/pdfs/$userId").apply { mkdirs() }.resolve("document-1.pdf").writeBytes("pdf".encodeToByteArray())
    }

    private fun deleteAssets() {
        File(filesRoot, "users/$userId/business").deleteRecursively()
        File(filesRoot, "product_images").deleteRecursively()
        File(filesRoot, "documents").deleteRecursively()
    }

    private class FakeDriveBackupClient : DriveBackupClient {
        private var payload = ByteArray(0)
        var lastFile: DriveBackupFile? = null

        override suspend fun connectionState() = DriveConnectionState.Connected("unused")
        override suspend fun findFolder(name: String, parentId: String?) = if (parentId == null) "root" else "backups"
        override suspend fun createFolder(name: String, parentId: String?) = "created-$name"
        override suspend fun findBackup(folderId: String, accountId: String, backupId: String) = null
        override suspend fun uploadBackup(folderId: String, file: File, metadata: DriveUploadMetadata, onProgress: suspend (Long, Long) -> Unit): DriveBackupFile {
            payload = file.readBytes()
            onProgress(payload.size.toLong(), payload.size.toLong())
            return DriveBackupFile("remote-1", file.name, payload.size.toLong(), metadata.checksum, metadata.accountId, metadata.backupId, metadata.createdAt).also { lastFile = it }
        }
        override suspend fun listBackups(folderId: String, accountId: String) = listOfNotNull(lastFile)
        override suspend fun downloadBackup(fileId: String, destination: File, onProgress: suspend (Long, Long) -> Unit) {
            destination.parentFile?.mkdirs()
            destination.writeBytes(payload)
            onProgress(payload.size.toLong(), payload.size.toLong())
        }
        override suspend fun deleteFile(fileId: String) = Unit
    }
}
