package app.tijario.features.backup

object LogicalBackupValidator {
    internal const val MAX_ROWS_PER_TABLE = 100_000
    internal const val MAX_TOTAL_ROWS = 500_000

    fun validate(snapshots: Collection<LogicalTableSnapshot>) {
        if (snapshots.sumOf { it.rows.size.toLong() } > MAX_TOTAL_ROWS) {
            throw BackupValidationException("Backup contains too many records")
        }
        snapshots.forEach { snapshot ->
            if (snapshot.rows.size > MAX_ROWS_PER_TABLE) {
                throw BackupValidationException("Backup table contains too many records")
            }
            validateUniqueIds(snapshot)
        }

        val byTable = snapshots.associateBy { it.table }
        validateReference(byTable, "documents_cache", "customer_id", "customers_cache")
        validateReference(byTable, "document_items_cache", "document_id", "documents_cache")
        validateReference(byTable, "document_creation_events", "document_id", "documents_cache")
        validateReference(byTable, "local_document_metadata", "document_id", "documents_cache")
    }

    private fun validateUniqueIds(snapshot: LogicalTableSnapshot) {
        val idIndex = snapshot.columns.indexOf("id")
        if (idIndex < 0) return
        val ids = mutableSetOf<String>()
        snapshot.rows.forEach { row ->
            val id = row[idIndex].value ?: throw BackupValidationException("Backup record identity is missing")
            if (!ids.add(id)) throw BackupValidationException("Backup contains duplicate record identities")
        }
    }

    private fun validateReference(
        snapshots: Map<String, LogicalTableSnapshot>,
        sourceTable: String,
        sourceColumn: String,
        targetTable: String,
    ) {
        val source = snapshots[sourceTable] ?: return
        val target = snapshots[targetTable]
            ?: throw BackupValidationException("Backup relationship target is missing: $targetTable")
        val sourceIndex = source.columns.indexOf(sourceColumn)
        val targetIdIndex = target.columns.indexOf("id")
        if (sourceIndex < 0) {
            throw BackupValidationException("Backup relationship column is missing: $sourceTable.$sourceColumn")
        }
        if (targetIdIndex < 0) {
            throw BackupValidationException("Backup relationship identity is missing: $targetTable.id")
        }
        val targetIds = target.rows.mapNotNull { it[targetIdIndex].value }.toSet()
        source.rows.forEach { row ->
            val reference = row[sourceIndex]
            if (reference.type != "null" && reference.value !in targetIds) {
                throw BackupValidationException("Backup contains an invalid record relationship")
            }
        }
    }
}
