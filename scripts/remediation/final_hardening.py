from __future__ import annotations
from pathlib import Path
import sys

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def write(path: str, text: str) -> None:
    (ROOT / path).write_text(text, encoding="utf-8")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if new in text:
        return text
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{label}: expected one old block, found {count}")
    return text.replace(old, new, 1)


dao_path = "app/src/main/java/app/tijario/data/local/TijarioDao.kt"
dao = read(dao_path)
for old, new, label in [
    (
        '''    @Query("DELETE FROM local_taxes")
    suspend fun clearLocalTaxes()''',
        '''    @Query("DELETE FROM local_taxes")
    suspend fun clearLocalTaxes()

    @Query("DELETE FROM local_taxes WHERE user_id = :userId")
    suspend fun deleteLocalTaxesForUser(userId: String)''',
        "tax user cleanup",
    ),
    (
        '''    @Query("DELETE FROM local_payment_methods")
    suspend fun clearLocalPaymentMethods()''',
        '''    @Query("DELETE FROM local_payment_methods")
    suspend fun clearLocalPaymentMethods()

    @Query("DELETE FROM local_payment_methods WHERE user_id = :userId")
    suspend fun deleteLocalPaymentMethodsForUser(userId: String)''',
        "payment user cleanup",
    ),
    (
        '''    @Query("DELETE FROM local_signatures")
    suspend fun clearLocalSignatures()''',
        '''    @Query("DELETE FROM local_signatures")
    suspend fun clearLocalSignatures()

    @Query("DELETE FROM local_signatures WHERE user_id = :userId")
    suspend fun deleteLocalSignaturesForUser(userId: String)''',
        "signature user cleanup",
    ),
    (
        '''    @Query("DELETE FROM local_terms")
    suspend fun clearLocalTerms()''',
        '''    @Query("DELETE FROM local_terms")
    suspend fun clearLocalTerms()

    @Query("DELETE FROM local_terms WHERE user_id = :userId")
    suspend fun deleteLocalTermsForUser(userId: String)''',
        "terms user cleanup",
    ),
    (
        '''    @Query("DELETE FROM local_document_metadata")
    suspend fun clearLocalDocumentMetadata()''',
        '''    @Query("DELETE FROM local_document_metadata")
    suspend fun clearLocalDocumentMetadata()

    @Query("DELETE FROM local_document_metadata WHERE user_id = :userId")
    suspend fun deleteLocalDocumentMetadataForUser(userId: String)''',
        "metadata user cleanup",
    ),
]:
    dao = replace_once(dao, old, new, label)
write(dao_path, dao)


repository_path = "app/src/main/java/app/tijario/data/repository/TijarioRepository.kt"
repository = read(repository_path)
repository = replace_once(
    repository,
    '''                dao.deleteDocuments(userId)
                dao.deleteOutboxForUser(userId)
                dao.deleteLeasesForUser(userId)
                dao.deleteLedgerForUser(userId)''',
    '''                dao.deleteDocuments(userId)
                dao.deleteLocalTaxesForUser(userId)
                dao.deleteLocalPaymentMethodsForUser(userId)
                dao.deleteLocalSignaturesForUser(userId)
                dao.deleteLocalTermsForUser(userId)
                dao.deleteLocalDocumentMetadataForUser(userId)
                dao.deleteOutboxForUser(userId)
                dao.deleteLeasesForUser(userId)
                dao.deleteLedgerForUser(userId)''',
    "account local-only cleanup",
)
repository = repository.replace(
    '''            when {
            first.operation == "CREATE"''',
    '''            when {
                first.operation == "CREATE"''',
    1,
)
repository = repository.replace(
    '''            }
            }
        }
        SyncScheduler(context).triggerSync(userId)''',
    '''            }
        }
        SyncScheduler(context).triggerSync(userId)''',
    1,
)
write(repository_path, repository)


analytics_path = "app/src/main/java/app/tijario/analytics/TijarioAnalytics.kt"
analytics = read(analytics_path)
if "private var enabled: Boolean = false" not in analytics:
    analytics = '''package app.tijario.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import app.tijario.BuildConfig
import com.facebook.appevents.AppEventsLogger

object TijarioAnalytics {
    @Volatile
    private var enabled: Boolean = false
    private var logger: AppEventsLogger? = null

    fun initialize(context: Context) {
        runCatching {
            logger = AppEventsLogger.newLogger(context.applicationContext)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "Meta logger initialization failed", error)
        }
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun logEvent(name: String, params: Bundle? = null) {
        if (!enabled) return
        runCatching {
            logger?.logEvent(name, params)
        }.onFailure { error ->
            if (BuildConfig.DEBUG) Log.w("TijarioAnalytics", "Analytics event failed: $name", error)
        }
    }
}
'''
write(analytics_path, analytics)


api_path = "app/src/main/java/app/tijario/data/remote/BackendApiClient.kt"
api = read(api_path)
api = replace_once(
    api,
    '''    private suspend inline fun <reified T> HttpResponse.decodeApiResultOrFallback(
        crossinline fallback: suspend () -> ApiResult<T>,
    ): ApiResult<T> {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        return runCatching {
            apiJson.decodeFromString<ApiResult<T>>(text)
        }.getOrElse {
            if (contentType.contains("application/json", ignoreCase = true)) {
                ApiResult(
                    ok = false,
                    code = "invalid_api_response",
                    message = "تعذر قراءة رد الخادم. حاول مرة أخرى بعد قليل.",
                )
            } else {
                fallback()
            }
        }
    }''',
    '''    private suspend inline fun <reified T> HttpResponse.decodeApiResultOrFallback(
        crossinline fallback: suspend () -> ApiResult<T>,
    ): ApiResult<T> {
        val contentType = headers[HttpHeaders.ContentType].orEmpty()
        val text = bodyAsText()
        val parsed = runCatching { apiJson.decodeFromString<ApiResult<T>>(text) }.getOrNull()
        if (parsed != null) return parsed
        if (status.value in setOf(404, 405, 501)) return fallback()
        return ApiResult(
            ok = false,
            code = if (contentType.contains("application/json", ignoreCase = true)) {
                "invalid_api_response"
            } else {
                "unexpected_api_response"
            },
            message = "تعذر قراءة رد الخادم. حاول مرة أخرى بعد قليل.",
        )
    }''',
    "API result fallback policy",
)
write(api_path, api)

print("final hardening applied")
