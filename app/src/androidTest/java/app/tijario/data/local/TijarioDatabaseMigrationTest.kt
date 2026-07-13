package app.tijario.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TijarioDatabaseMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TijarioDatabase::class.java,
    )

    @Test
    fun migrate6To7_createsRetryColumnAndIndex() {
        migrationHelper.createDatabase(TEST_DATABASE, 6).close()

        val database = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            7,
            true,
            TijarioDatabase.MIGRATION_6_7,
        )

        val columns = buildSet {
            database.query("PRAGMA table_info(sync_outbox)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
        assertTrue(
            "sync_outbox must contain next_retry_at after migrating from version 6",
            "next_retry_at" in columns,
        )

        val indices = buildSet {
            database.query("PRAGMA index_list(sync_outbox)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
        assertTrue(
            "sync_outbox must index next_retry_at after migrating from version 6",
            "index_sync_outbox_next_retry_at" in indices,
        )

        database.close()
    }

    private companion object {
        const val TEST_DATABASE = "tijario-migration-test"
    }
}
