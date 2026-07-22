package app.tijario.features.backup

import org.junit.Assert.assertThrows
import org.junit.Test

class LogicalBackupValidatorTest {
    @Test
    fun duplicateRecordIdsAreRejectedBeforeRestore() {
        val snapshot = snapshot("documents_cache", listOf("id", "user_id"), listOf("doc-1", "user-1"), listOf("doc-1", "user-1"))

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupValidator.validate(listOf(snapshot))
        }
    }

    @Test
    fun missingDocumentRelationshipIsRejectedBeforeRestore() {
        val documents = snapshot("documents_cache", listOf("id", "user_id"), listOf("doc-1", "user-1"))
        val items = snapshot("document_items_cache", listOf("id", "user_id", "document_id"), listOf("item-1", "user-1", "missing"))

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupValidator.validate(listOf(documents, items))
        }
    }

    @Test
    fun validRelationshipsAreAccepted() {
        val customers = snapshot("customers_cache", listOf("id", "user_id"), listOf("customer-1", "user-1"))
        val documents = snapshot(
            "documents_cache",
            listOf("id", "user_id", "customer_id"),
            listOf("doc-1", "user-1", "customer-1"),
        )
        val items = snapshot("document_items_cache", listOf("id", "user_id", "document_id"), listOf("item-1", "user-1", "doc-1"))

        LogicalBackupValidator.validate(listOf(customers, documents, items))
    }

    @Test
    fun requiredRelationshipColumnsAreNotSilentlySkipped() {
        val documents = snapshot("documents_cache", listOf("id", "user_id"), listOf("doc-1", "user-1"))
        val customers = snapshot("customers_cache", listOf("id", "user_id"), listOf("customer-1", "user-1"))

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupValidator.validate(listOf(customers, documents))
        }
    }

    @Test
    fun documentMetadataRequiresAValidDocumentRelationship() {
        val documents = snapshot("documents_cache", listOf("id", "user_id"), listOf("doc-1", "user-1"))
        val metadata = snapshot("local_document_metadata", listOf("id", "user_id"), listOf("meta-1", "user-1"))

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupValidator.validate(listOf(documents, metadata))
        }
    }

    private fun snapshot(table: String, columns: List<String>, vararg values: List<String>) = LogicalTableSnapshot(
        table = table,
        columns = columns,
        rows = values.map { row -> row.map { LogicalBackupValue("text", it) } },
    )
}
