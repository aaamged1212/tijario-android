package app.tijario.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountIsolationMigrationTest {

    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TijarioDatabase::class.java,
    )

    @Test
    fun migrate14To15_recreatesLocalOnlyTablesWithUserScope() {
        val legacy = migrationHelper.createDatabase(TEST_DATABASE, 14)
        legacy.execSQL("INSERT INTO local_taxes (id, name, rate) VALUES ('legacy-tax', 'Legacy', 5.0)")
        legacy.close()

        val database = migrationHelper.runMigrationsAndValidate(
            TEST_DATABASE,
            15,
            true,
            TijarioDatabase.MIGRATION_14_15,
        )

        listOf(
            "local_taxes",
            "local_payment_methods",
            "local_signatures",
            "local_terms",
            "local_document_metadata",
        ).forEach { table ->
            val columns = buildSet {
                database.query("PRAGMA table_info($table)").use { cursor ->
                    val nameIndex = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) add(cursor.getString(nameIndex))
                }
            }
            assertTrue("$table must contain user_id", "user_id" in columns)
        }

        database.query("SELECT COUNT(*) FROM local_taxes").use { cursor ->
            cursor.moveToFirst()
            assertEquals("Unscoped legacy rows must not be assigned to another account", 0, cursor.getInt(0))
        }
        database.close()
    }

    private companion object {
        const val TEST_DATABASE = "tijario-account-isolation-migration-test"
    }
}
