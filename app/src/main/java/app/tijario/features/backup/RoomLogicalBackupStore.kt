package app.tijario.features.backup

import android.database.Cursor
import app.tijario.data.local.TijarioDatabase
import java.util.Base64

data class BackupTableSpec(
    val table: String,
    val archivePath: String,
)

class RoomLogicalBackupStore(
    private val database: TijarioDatabase,
) {
    fun exportAccount(userId: String): Map<String, ByteArray> {
        require(userId.isNotBlank()) { "Backup account is required" }
        val sqlite = database.openHelper.writableDatabase
        return tableSpecs.associate { spec ->
            val snapshot = sqlite.query(
                "SELECT * FROM ${quoted(spec.table)} WHERE user_id = ?",
                arrayOf(userId),
            ).use { cursor -> cursor.toSnapshot(spec.table) }
            spec.archivePath to LogicalBackupSnapshotCodec.encode(snapshot)
        }
    }

    fun restoreAccount(
        userId: String,
        logicalEntries: Map<String, ByteArray>,
    ) {
        require(userId.isNotBlank()) { "Restore account is required" }
        val sqlite = database.openHelper.writableDatabase
        val staged = tableSpecs.associateWith { spec ->
            val bytes = logicalEntries[spec.archivePath]
                ?: throw BackupValidationException("Backup table is missing: ${spec.table}")
            LogicalBackupSnapshotCodec.decode(bytes).also { snapshot ->
                if (snapshot.table != spec.table) throw BackupValidationException("Backup table identity is invalid")
                LogicalBackupSnapshotCodec.validateAccount(snapshot, userId)
                validateLiveColumns(sqliteColumns(spec.table), snapshot)
            }
        }

        sqlite.beginTransaction()
        try {
            tableSpecs.asReversed().forEach { spec ->
                sqlite.execSQL("DELETE FROM ${quoted(spec.table)} WHERE user_id = ?", arrayOf(userId))
            }
            tableSpecs.forEach { spec ->
                insertSnapshot(staged.getValue(spec))
            }
            sqlite.setTransactionSuccessful()
        } finally {
            sqlite.endTransaction()
        }
    }

    private fun insertSnapshot(snapshot: LogicalTableSnapshot) {
        if (snapshot.rows.isEmpty()) return
        val sqlite = database.openHelper.writableDatabase
        val columns = snapshot.columns.joinToString(",") { quoted(it) }
        val placeholders = List(snapshot.columns.size) { "?" }.joinToString(",")
        val statement = sqlite.compileStatement(
            "INSERT OR ABORT INTO ${quoted(snapshot.table)} ($columns) VALUES ($placeholders)",
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

    private fun sqliteColumns(table: String): Set<String> =
        database.openHelper.writableDatabase.query("PRAGMA table_info(${quoted(table)})").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet {
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }

    private fun validateLiveColumns(liveColumns: Set<String>, snapshot: LogicalTableSnapshot) {
        if (!liveColumns.containsAll(snapshot.columns)) {
            throw BackupValidationException("Backup requires unsupported database columns")
        }
    }

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
            BackupTableSpec("document_creation_events", "data/document-creation-events.json"),
            BackupTableSpec("deleted_record_history", "data/deleted-record-history.json"),
            BackupTableSpec("account_entitlements", "data/account-entitlements.json"),
            BackupTableSpec("backup_settings", "data/backup-settings.json"),
            BackupTableSpec("device_bindings", "data/device-bindings.json"),
            BackupTableSpec("offline_quota_lease", "data/offline-quota-leases.json"),
        )
    }
}
