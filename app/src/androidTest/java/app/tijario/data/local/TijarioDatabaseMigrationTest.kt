package app.tijario.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
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

    @Test
    fun migrate18To19_addsNullableRestoreCompletionTime() {
        migrationHelper.createDatabase(TEST_DATABASE_V19, 18).use { legacy ->
            legacy.execSQL(
                """
                INSERT INTO backup_records (
                    id, user_id, local_relative_path, format_version, status,
                    size_bytes, checksum, created_at
                ) VALUES (
                    'backup-1', 'user-1', 'users/user-1/backups/backup.tijario', 1,
                    'PHONE_SAVED', 100, 'checksum', 10
                )
                """.trimIndent(),
            )
        }

        val database = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE_V19,
            19,
            true,
            TijarioDatabase.MIGRATION_18_19,
        )

        database.query("SELECT restored_at FROM backup_records WHERE id = 'backup-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertNull(cursor.getString(cursor.getColumnIndexOrThrow("restored_at")))
        }
        database.close()
    }

    @Test
    fun migrate19To20_createsAccountScopedAiHistory() {
        migrationHelper.createDatabase(TEST_DATABASE_V20, 19).close()

        val database = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE_V20,
            20,
            true,
            TijarioDatabase.MIGRATION_19_20,
        )

        val columns = buildSet {
            database.query("PRAGMA table_info(ai_generation_history)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }
        assertTrue("user_id" in columns)
        assertTrue("generation_type" in columns)
        assertTrue("result_text" in columns)

        val indices = buildSet {
            database.query("PRAGMA index_list(ai_generation_history)").use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }
        assertTrue("index_ai_generation_history_user_id" in indices)
        assertTrue("index_ai_generation_history_user_id_generation_type_created_at" in indices)
        database.close()
    }

    private companion object {
        const val TEST_DATABASE = "tijario-migration-test"
        const val TEST_DATABASE_V19 = "tijario-backup-history-migration-test"
        const val TEST_DATABASE_V20 = "tijario-ai-history-migration-test"
    }
}
