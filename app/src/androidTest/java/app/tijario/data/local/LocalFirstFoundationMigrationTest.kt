package app.tijario.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalFirstFoundationMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TijarioDatabase::class.java,
    )

    @Test
    fun migrate15To16_preservesUsageEventsAndAddsLocalFirstTables() {
        migrationHelper.createDatabase(TEST_DATABASE, 15).use { legacy ->
            legacy.execSQL(
                """
                INSERT INTO local_usage_ledger (
                    usage_event_id, user_id, document_id, operation_id, lease_id,
                    period_month, status, created_at, synced_at
                ) VALUES (
                    'event-1', 'user-1', 'document-1', 'operation-1', 'lease-1',
                    '2026-07', 'SYNCED', 100, 200
                )
                """.trimIndent(),
            )
        }

        val database = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            16,
            true,
            TijarioDatabase.MIGRATION_15_16,
        )

        database.query("SELECT * FROM document_creation_events WHERE id = 'event-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("user-1", cursor.getString(cursor.getColumnIndexOrThrow("user_id")))
            assertEquals("document-1", cursor.getString(cursor.getColumnIndexOrThrow("document_id")))
            assertEquals("operation-1", cursor.getString(cursor.getColumnIndexOrThrow("operation_id")))
            assertEquals("ACKNOWLEDGED", cursor.getString(cursor.getColumnIndexOrThrow("status")))
            assertEquals(200L, cursor.getLong(cursor.getColumnIndexOrThrow("acknowledged_at_server")))
        }

        val tables = buildSet {
            database.query("SELECT name FROM sqlite_master WHERE type = 'table'").use { cursor ->
                while (cursor.moveToNext()) add(cursor.getString(0))
            }
        }
        assertFalse("Legacy mutable ledger must be removed after data migration", "local_usage_ledger" in tables)
        assertTrue("account_entitlements" in tables)
        assertTrue("backup_settings" in tables)
        assertTrue("backup_records" in tables)
        assertTrue("backup_file_entries" in tables)
        assertTrue("device_bindings" in tables)
        assertTrue("deleted_record_history" in tables)
        database.close()
    }

    private companion object {
        const val TEST_DATABASE = "tijario-local-first-foundation-migration-test"
    }
}
