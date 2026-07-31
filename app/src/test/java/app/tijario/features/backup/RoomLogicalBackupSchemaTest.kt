package app.tijario.features.backup

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomLogicalBackupSchemaTest {
    @Test
    fun oldSnapshotMayOmitANewDefaultedColumn() {
        assertTrue(
            isBackupSchemaCompatible(
                listOf(
                    BackupLiveColumn("id", true, null, 1),
                    BackupLiveColumn("user_id", true, null, 0),
                    BackupLiveColumn("new_default", true, "0", 0),
                ),
                listOf("id", "user_id"),
            ),
        )
    }

    @Test
    fun oldSnapshotMissingANewRequiredColumnIsRejectedBeforeMutation() {
        assertFalse(
            isBackupSchemaCompatible(
                listOf(
                    BackupLiveColumn("id", true, null, 1),
                    BackupLiveColumn("user_id", true, null, 0),
                    BackupLiveColumn("required_value", true, null, 0),
                ),
                listOf("id", "user_id"),
            ),
        )
    }
}
