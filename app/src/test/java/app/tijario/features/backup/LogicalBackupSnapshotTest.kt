package app.tijario.features.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LogicalBackupSnapshotTest {
    @Test
    fun snapshotRoundTripPreservesTypedValues() {
        val snapshot = LogicalTableSnapshot(
            table = "documents_cache",
            columns = listOf("id", "user_id", "total", "deleted_at"),
            rows = listOf(
                listOf(
                    LogicalBackupValue("text", "document-1"),
                    LogicalBackupValue("text", "user-1"),
                    LogicalBackupValue("text", "1250000.75"),
                    LogicalBackupValue("null"),
                ),
            ),
        )

        val decoded = LogicalBackupSnapshotCodec.decode(LogicalBackupSnapshotCodec.encode(snapshot))

        assertEquals(snapshot, decoded)
        LogicalBackupSnapshotCodec.validateAccount(decoded, "user-1")
    }

    @Test
    fun restoreRejectsRowsOwnedByAnotherAccount() {
        val snapshot = LogicalTableSnapshot(
            table = "customers_cache",
            columns = listOf("id", "user_id"),
            rows = listOf(
                listOf(
                    LogicalBackupValue("text", "customer-1"),
                    LogicalBackupValue("text", "user-2"),
                ),
            ),
        )

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupSnapshotCodec.validateAccount(snapshot, "user-1")
        }
    }

    @Test
    fun malformedRowsFailBeforeDatabaseReplacement() {
        val invalid = """{"table":"documents_cache","columns":["id","user_id"],"rows":[[{"type":"text","value":"document-1"}]]}"""

        assertThrows(BackupValidationException::class.java) {
            LogicalBackupSnapshotCodec.decode(invalid.encodeToByteArray())
        }
    }

    @Test
    fun archiveInventoryIncludesEditableDocumentsAndQuotaEvents() {
        val paths = RoomLogicalBackupStore.tableSpecs.map { it.archivePath }

        assertEquals(true, "data/documents.json" in paths)
        assertEquals(true, "data/document-items.json" in paths)
        assertEquals(true, "data/document-creation-events.json" in paths)
        assertEquals(true, "data/deleted-record-history.json" in paths)
        assertEquals(true, "data/business-settings.json" in paths)
    }
}
