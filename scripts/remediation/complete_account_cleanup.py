from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()


def patch(path: str, old: str, new: str, label: str) -> None:
    file = ROOT / path
    source = file.read_text(encoding="utf-8")
    if new in source:
        return
    if source.count(old) != 1:
        raise RuntimeError(f"{label}: expected one old block, found {source.count(old)}")
    file.write_text(source.replace(old, new, 1), encoding="utf-8")


patch(
    "app/src/main/java/app/tijario/data/local/TijarioDao.kt",
    '''    @Query("DELETE FROM sync_state")
    suspend fun clearSyncState()''',
    '''    @Query("DELETE FROM sync_state WHERE user_id = :userId")
    suspend fun deleteSyncStateForUser(userId: String)

    @Query("DELETE FROM sync_state")
    suspend fun clearSyncState()''',
    "sync-state per-user cleanup",
)

patch(
    "app/src/main/java/app/tijario/data/local/NotificationsDao.kt",
    '''    @Query("DELETE FROM announcements_cache WHERE user_id = :userId")
    suspend fun deleteAnnouncementsForUser(userId: String)''',
    '''    @Query("DELETE FROM announcements_cache WHERE user_id = :userId")
    suspend fun deleteAnnouncementsForUser(userId: String)

    @Query("DELETE FROM announcements_cache")
    suspend fun clearAnnouncements()''',
    "announcement global cleanup",
)
patch(
    "app/src/main/java/app/tijario/data/local/NotificationsDao.kt",
    '''    @Query("DELETE FROM announcement_receipt_outbox WHERE user_id = :userId")
    suspend fun deleteReceiptOutboxForUser(userId: String)
}''',
    '''    @Query("DELETE FROM announcement_receipt_outbox WHERE user_id = :userId")
    suspend fun deleteReceiptOutboxForUser(userId: String)

    @Query("DELETE FROM announcement_receipt_outbox")
    suspend fun clearReceiptOutbox()
}''',
    "receipt global cleanup",
)

patch(
    "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt",
    '''    private val dao = database.tijarioDao()
    private val syncStateMutable''',
    '''    private val dao = database.tijarioDao()
    private val notificationsDao = database.notificationsDao()
    private val syncStateMutable''',
    "notifications DAO dependency",
)
patch(
    "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt",
    '''                dao.clearLeases()
                dao.clearLedger()
            }''',
    '''                dao.clearLeases()
                dao.clearLedger()
                notificationsDao.clearAnnouncements()
                notificationsDao.clearReceiptOutbox()
            }''',
    "logout notification cleanup",
)
patch(
    "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt",
    '''                dao.deleteLocalDocumentMetadataForUser(userId)
                dao.deleteOutboxForUser(userId)''',
    '''                dao.deleteLocalDocumentMetadataForUser(userId)
                dao.deleteSyncStateForUser(userId)
                dao.deleteOutboxForUser(userId)
                notificationsDao.deleteAnnouncementsForUser(userId)
                notificationsDao.deleteReceiptOutboxForUser(userId)''',
    "account sync and notification cleanup",
)

print("complete account cleanup applied")
