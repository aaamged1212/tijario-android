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

}
