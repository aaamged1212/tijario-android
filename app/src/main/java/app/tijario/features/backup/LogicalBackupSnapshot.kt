package app.tijario.features.backup

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class LogicalBackupValue(
    val type: String,
    val value: String? = null,
)

@Serializable
data class LogicalTableSnapshot(
    val table: String,
    val columns: List<String>,
    val rows: List<List<LogicalBackupValue>>,
)

object LogicalBackupSnapshotCodec {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = false }
    private val identifier = Regex("[A-Za-z_][A-Za-z0-9_]*")
    private val supportedTypes = setOf("null", "integer", "real", "text", "blob")

    fun encode(snapshot: LogicalTableSnapshot): ByteArray {
        validateShape(snapshot)
        return json.encodeToString(snapshot).encodeToByteArray()
    }

    fun decode(bytes: ByteArray): LogicalTableSnapshot = try {
        json.decodeFromString<LogicalTableSnapshot>(bytes.decodeToString()).also(::validateShape)
    } catch (error: BackupValidationException) {
        throw error
    } catch (error: Exception) {
        throw BackupValidationException("Backup table data is invalid", error)
    }

    fun validateAccount(snapshot: LogicalTableSnapshot, expectedAccountId: String) {
        val userColumn = snapshot.columns.indexOf("user_id")
        if (userColumn < 0) throw BackupValidationException("Backup table is not account scoped")
        snapshot.rows.forEach { row ->
            val owner = row[userColumn]
            if (owner.type != "text" || owner.value != expectedAccountId) {
                throw BackupValidationException("Backup contains data for a different account")
            }
        }
    }

    private fun validateShape(snapshot: LogicalTableSnapshot) {
        if (!identifier.matches(snapshot.table)) throw BackupValidationException("Backup table name is invalid")
        if (snapshot.columns.isEmpty() || snapshot.columns.distinct().size != snapshot.columns.size) {
            throw BackupValidationException("Backup table columns are invalid")
        }
        if (snapshot.columns.any { !identifier.matches(it) }) {
            throw BackupValidationException("Backup table column name is invalid")
        }
        snapshot.rows.forEach { row ->
            if (row.size != snapshot.columns.size || row.any { it.type !in supportedTypes }) {
                throw BackupValidationException("Backup table row is invalid")
            }
            row.forEach { value ->
                if (value.type == "null" && value.value != null) {
                    throw BackupValidationException("Backup null value is invalid")
                }
                if (value.type != "null" && value.value == null) {
                    throw BackupValidationException("Backup value is missing")
                }
            }
        }
    }
}
