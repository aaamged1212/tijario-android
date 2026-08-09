package app.tijario.features.backup

import android.database.Cursor
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import app.tijario.data.local.TijarioDatabase
import java.util.Base64

data class BackupTableSpec(
    val table: String,
    val archivePath: String,
    val restoreMode: BackupRestoreMode = BackupRestoreMode.Replace,
)

internal data class BackupLiveColumn(
    val name: String,
    val notNull: Boolean,
    val defaultValue: String?,
    val primaryKeyPosition: Int,
)

internal fun isBackupSchemaCompatible(liveColumns: List<BackupLiveColumn>, snapshotColumns: Collection<String>): Boolean {
    val snapshotNames = snapshotColumns.toSet()
    val liveNames = liveColumns.mapTo(mutableSetOf()) { it.name }
    return liveNames.containsAll(snapshotNames) && liveColumns.none {
        it.name !in snapshotNames &&
            ((it.notNull && it.defaultValue == null && it.primaryKeyPosition == 0) || it.primaryKeyPosition != 0)
    }
}

enum class BackupRestoreMode {
    Replace,
    MergeWithoutOverwrite,
    PreserveCurrent,
}

class RoomLogicalBackupStore(
    private val database: TijarioDatabase,
) {
    fun exportAccount(userId: String): Map<String, ByteArray> {
        require(userId.isNotBlank()) { "Backup account is required" }
        val sqlite = database.openHelper.writableDatabase
        sqlite.beginTransaction()
        return try {
            tableSpecs.associate { spec ->
                val snapshot = sqlite.query(
                    "SELECT * FROM ${quoted(spec.table)} WHERE user_id = ?",
                    arrayOf(userId),
                ).use { cursor -> cursor.toSnapshot(spec.table) }
                spec.archivePath to LogicalBackupSnapshotCodec.encode(snapshot)
            }.also {
                sqlite.setTransactionSuccessful()
            }
        } finally {
            sqlite.endTransaction()
        }
    }

    fun restoreAccount(
        userId: String,
        logicalEntries: Map<String, ByteArray>,
        beforeCommit: () -> Unit = {},
    ) {
        require(userId.isNotBlank()) { "Restore account is required" }
        val sqlite = database.openHelper.writableDatabase
        val staged = tableSpecs.associateWith { spec ->
            val bytes = logicalEntries[spec.archivePath]
                ?: throw BackupValidationException("Backup table is missing: ${spec.table}")
            LogicalBackupSnapshotCodec.decode(bytes).also { snapshot ->
                if (snapshot.table != spec.table) throw BackupValidationException("Backup table identity is invalid")
                LogicalBackupSnapshotCodec.validateAccount(snapshot, userId)
                validateLiveSchema(spec.table, sqliteColumns(spec.table), snapshot)
            }
        }
        LogicalBackupValidator.validate(staged.values)

        BackupDiagnostics.stage("restore", "DB_TRANSACTION_STARTED")
        sqlite.beginTransaction()
        var transactionSuccessful = false
        try {
            tableSpecs.asReversed().filter { it.restoreMode == BackupRestoreMode.Replace }.forEach { spec ->
                BackupDiagnostics.stage("restore", "DB_DELETE_${spec.table}", spec.table)
                try {
                    sqlite.execSQL("DELETE FROM ${quoted(spec.table)} WHERE user_id = ?", arrayOf(userId))
                } catch (error: SQLiteException) {
                    throw BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_DELETE_FAILED, error, spec.table)
                }
            }
            tableSpecs.filter { it.restoreMode != BackupRestoreMode.PreserveCurrent }.forEach { spec ->
                BackupDiagnostics.stage("restore", "DB_INSERT_${spec.table}", spec.table)
                try {
                    insertSnapshot(staged.getValue(spec), spec.restoreMode)
                } catch (error: SQLiteConstraintException) {
                    throw BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_CONSTRAINT_FAILED, error, spec.table)
                } catch (error: SQLiteException) {
                    throw BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_INSERT_FAILED, error, spec.table)
                }
            }
            BackupDiagnostics.stage("restore", "DB_FOREIGN_KEY_CHECK")
            if (hasForeignKeyViolation(sqlite)) {
                throw BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_FOREIGN_KEY_FAILED)
            }
            beforeCommit()
            sqlite.setTransactionSuccessful()
            transactionSuccessful = true
        } finally {
            try {
                sqlite.endTransaction()
                BackupDiagnostics.stage("restore", if (transactionSuccessful) "DB_TRANSACTION_COMMITTED" else "ROLLBACK_COMPLETED")
            } catch (error: Exception) {
                val code = if (transactionSuccessful) {
                    BackupRestoreException.Code.RESTORE_DB_COMMIT_FAILED
                } else {
                    BackupRestoreException.Code.RESTORE_ROLLBACK_FAILED
                }
                BackupDiagnostics.stage("restore", if (transactionSuccessful) "DB_TRANSACTION_COMMIT_FAILED" else "ROLLBACK_FAILED", error = error)
                throw BackupRestoreException(code, error)
            }
        }
    }

    private fun insertSnapshot(snapshot: LogicalTableSnapshot, restoreMode: BackupRestoreMode) {
        if (snapshot.rows.isEmpty()) return
        val sqlite = database.openHelper.writableDatabase
        val columns = snapshot.columns.joinToString(",") { quoted(it) }
        val placeholders = List(snapshot.columns.size) { "?" }.joinToString(",")
        val conflict = if (restoreMode == BackupRestoreMode.MergeWithoutOverwrite) "IGNORE" else "ABORT"
        val statement = sqlite.compileStatement(
            "INSERT OR $conflict INTO ${quoted(snapshot.table)} ($columns) VALUES ($placeholders)",
        )
        snapshot.rows.forEach { row ->
            statement.clearBindings()
            row.forEachIndexed { index, value ->
                val parameter = index + 1
                when (value.type) {
                    "null" -> statement.bindNull(parameter)
                    "integer" -> statement.bindLong(parameter, value.value!!.toLong())
                    "real" -> statement.bindDouble(parameter, value.value!!.toDouble())
                    "text" -> statement.bindString(parameter, value.value!!)
                    "blob" -> statement.bindBlob(parameter, Base64.getDecoder().decode(value.value!!))
                    else -> throw BackupValidationException("Backup value type is unsupported")
                }
            }
            statement.executeInsert()
        }
    }

    private fun sqliteColumns(table: String): List<BackupLiveColumn> =
        database.openHelper.writableDatabase.query("PRAGMA table_info(${quoted(table)})").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            val notNullIndex = cursor.getColumnIndexOrThrow("notnull")
            val defaultIndex = cursor.getColumnIndexOrThrow("dflt_value")
            val pkIndex = cursor.getColumnIndexOrThrow("pk")
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        BackupLiveColumn(
                            name = cursor.getString(nameIndex),
                            notNull = cursor.getInt(notNullIndex) != 0,
                            defaultValue = cursor.getString(defaultIndex),
                            primaryKeyPosition = cursor.getInt(pkIndex),
                        ),
                    )
                }
            }
        }

    private fun validateLiveSchema(table: String, liveColumns: List<BackupLiveColumn>, snapshot: LogicalTableSnapshot) {
        if (!isBackupSchemaCompatible(liveColumns, snapshot.columns)) {
            BackupDiagnostics.stage("restore", "DB_SCHEMA_INCOMPATIBLE", table)
            throw BackupRestoreException(BackupRestoreException.Code.RESTORE_DB_SCHEMA_INCOMPATIBLE, table = table)
        }
    }

    private fun hasForeignKeyViolation(sqlite: androidx.sqlite.db.SupportSQLiteDatabase): Boolean =
        sqlite.query("PRAGMA foreign_key_check").use { cursor -> cursor.moveToFirst() }

    private fun Cursor.toSnapshot(table: String): LogicalTableSnapshot {
        val columns = columnNames.toList()
        val rows = buildList {
            while (moveToNext()) {
                add(
                    columns.indices.map { index ->
                        when (getType(index)) {
                            Cursor.FIELD_TYPE_NULL -> LogicalBackupValue("null")
                            Cursor.FIELD_TYPE_INTEGER -> LogicalBackupValue("integer", getLong(index).toString())
                            Cursor.FIELD_TYPE_FLOAT -> LogicalBackupValue("real", getDouble(index).toString())
                            Cursor.FIELD_TYPE_BLOB -> LogicalBackupValue(
                                "blob",
                                Base64.getEncoder().encodeToString(getBlob(index)),
                            )
                            else -> LogicalBackupValue("text", getString(index))
                        }
                    },
                )
            }
        }
        return LogicalTableSnapshot(table, columns, rows)
    }

    private fun quoted(identifier: String): String = "\"$identifier\""

    companion object {
        val tableSpecs = listOf(
            BackupTableSpec("business_settings_cache", "data/business-settings.json"),
            BackupTableSpec("customers_cache", "data/customers.json"),
            BackupTableSpec("products_cache", "data/products.json"),
            BackupTableSpec("documents_cache", "data/documents.json"),
            BackupTableSpec("document_items_cache", "data/document-items.json"),
            BackupTableSpec("local_taxes", "data/taxes.json"),
            BackupTableSpec("local_payment_methods", "data/payment-methods.json"),
            BackupTableSpec("local_signatures", "data/signatures.json"),
            BackupTableSpec("local_terms", "data/terms.json"),
            BackupTableSpec("local_document_metadata", "data/document-metadata.json"),
            BackupTableSpec(
                "document_creation_events",
                "data/document-creation-events.json",
                BackupRestoreMode.MergeWithoutOverwrite,
            ),
            BackupTableSpec("deleted_record_history", "data/deleted-record-history.json"),
            BackupTableSpec(
                "account_entitlements",
                "data/account-entitlements.json",
                BackupRestoreMode.MergeWithoutOverwrite,
            ),
            BackupTableSpec("backup_settings", "data/backup-settings.json"),
            BackupTableSpec(
                "device_bindings",
                "data/device-bindings.json",
                BackupRestoreMode.PreserveCurrent,
            ),
            BackupTableSpec(
                "offline_quota_lease",
                "data/offline-quota-leases.json",
                BackupRestoreMode.PreserveCurrent,
            ),
        )
    }
}
