package app.tijario.features.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.config.AppRuntimeState
import app.tijario.config.AppLanguage
import app.tijario.config.Localization
import app.tijario.data.remote.AiV3CaptionRequest
import app.tijario.data.remote.AiV3ReplyRequest
import app.tijario.data.remote.AiV3ReportRequest
import app.tijario.data.remote.AiV3RefineRequest
import app.tijario.data.remote.AiV3ResponseData
import app.tijario.domain.LocalizedErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

sealed interface AiV3ScreenState {
    data object Idle : AiV3ScreenState
    data object Editing : AiV3ScreenState
    data object Loading : AiV3ScreenState
    data class Success(
        val generationType: String,
        val data: AiV3ResponseData,
        val notice: String? = null,
    ) : AiV3ScreenState

    data class Refining(val previous: Success) : AiV3ScreenState
    data class Reporting(val previous: Success) : AiV3ScreenState
    data class Error(val message: String) : AiV3ScreenState
    data class Offline(val message: String) : AiV3ScreenState
    data class LimitReached(val message: String) : AiV3ScreenState
}

class AiViewModel(
    private val repository: AiRepositoryV3,
) : ViewModel() {
    private val stateMutable = MutableStateFlow<AiV3ScreenState>(AiV3ScreenState.Idle)
    val state: StateFlow<AiV3ScreenState> = stateMutable.asStateFlow()

    fun markEditing() {
        if (stateMutable.value is AiV3ScreenState.Idle) {
            stateMutable.value = AiV3ScreenState.Editing
        }
    }

    fun generateReply(request: AiV3ReplyRequest, onSuccess: () -> Unit) {
        stateMutable.value = AiV3ScreenState.Loading
        viewModelScope.launch {
            runCatching { repository.generateReply(request) }
                .onSuccess { response ->
                    val data = response.data
                    when {
                        response.ok && data != null -> {
                            stateMutable.value = AiV3ScreenState.Success("reply", data)
                            onSuccess()
                        }

                        response.code == "ai_limit_reached" -> {
                            stateMutable.value = AiV3ScreenState.LimitReached(localizedAiLimitReached())
                        }

                        else -> stateMutable.value = AiV3ScreenState.Error(localizedReplyError())
                    }
                }
                .onFailure { error ->
                    stateMutable.value = mapFailure(error, localizedReplyError())
                }
        }
    }

    fun generateCaption(request: AiV3CaptionRequest, onSuccess: () -> Unit) {
        stateMutable.value = AiV3ScreenState.Loading
        viewModelScope.launch {
            runCatching { repository.generateCaption(request) }
                .onSuccess { response ->
                    val data = response.data
                    when {
                        response.ok && data != null -> {
                            stateMutable.value = AiV3ScreenState.Success("caption", data)
                            onSuccess()
                        }

                        response.code == "ai_limit_reached" -> {
                            stateMutable.value = AiV3ScreenState.LimitReached(localizedAiLimitReached())
                        }

                        else -> stateMutable.value = AiV3ScreenState.Error(localizedCaptionError())
                    }
                }
                .onFailure { error ->
                    stateMutable.value = mapFailure(error, localizedCaptionError())
                }
        }
    }

    fun refine(
        previous: AiV3ScreenState.Success,
        variantId: String,
        preset: String,
        dialect: String?,
        language: String,
        onSuccess: () -> Unit,
    ) {
        stateMutable.value = AiV3ScreenState.Refining(previous)
        viewModelScope.launch {
            val request = AiV3RefineRequest(
                clientRequestId = UUID.randomUUID().toString(),
                generationId = previous.data.generationId,
                variantId = variantId,
                preset = preset,
                dialect = dialect,
                language = language,
            )
            runCatching { repository.refine(request) }
                .onSuccess { response ->
                    val data = response.data
                    when {
                        response.ok && data != null -> {
                            stateMutable.value = AiV3ScreenState.Success(previous.generationType, data)
                            onSuccess()
                        }

                        response.code == "ai_limit_reached" -> {
                            stateMutable.value = AiV3ScreenState.LimitReached(localizedAiLimitReached())
                        }

                        else -> stateMutable.value = previous.copy(notice = localizedRefineError())
                    }
                }
                .onFailure { error ->
                    stateMutable.value = previous.copy(notice = failureMessage(error, localizedRefineError()))
                }
        }
    }

    fun report(
        previous: AiV3ScreenState.Success,
        variantId: String,
        issueType: String,
        note: String?,
        onDone: () -> Unit,
    ) {
        stateMutable.value = AiV3ScreenState.Reporting(previous)
        viewModelScope.launch {
            val request = AiV3ReportRequest(
                clientRequestId = UUID.randomUUID().toString(),
                generationType = previous.generationType,
                generationId = previous.data.generationId,
                variantId = variantId,
                issueType = issueType,
                note = note,
            )
            runCatching { repository.report(request) }
                .onSuccess { response ->
                    stateMutable.value = previous.copy(
                        notice = if (response.ok) localizedReportSuccess() else localizedReportError(),
                    )
                    onDone()
                }
                .onFailure { error ->
                    stateMutable.value = previous.copy(notice = failureMessage(error, localizedReportError()))
                }
        }
    }

    private fun mapFailure(error: Throwable, fallback: String): AiV3ScreenState =
        if (error is IOException) {
            AiV3ScreenState.Offline(localizedOfflineMessage())
        } else {
            AiV3ScreenState.Error(failureMessage(error, fallback))
        }

    private fun failureMessage(error: Throwable, fallback: String): String {
        val mapped = LocalizedErrorMapper.map(null, error.message, AppRuntimeState.currentLanguage)
        return mapped.takeIf { it.isNotBlank() } ?: fallback
    }

    private fun localizedAiLimitReached(): String =
        Localization.getString("ai_limit_reached", AppRuntimeState.currentLanguage)

    private fun localizedReplyError(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "تعذر توليد الرد الآن."
    } else {
        "Could not generate the reply right now."
    }

    private fun localizedCaptionError(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "تعذر توليد الكابشن الآن."
    } else {
        "Could not generate the caption right now."
    }

    private fun localizedRefineError(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "تعذر تحسين النص الآن."
    } else {
        "Could not refine the text right now."
    }

    private fun localizedReportError(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "تعذر إرسال البلاغ."
    } else {
        "Could not send the report."
    }

    private fun localizedReportSuccess(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "تم إرسال البلاغ."
    } else {
        "Report sent."
    }

    private fun localizedOfflineMessage(): String = if (AppRuntimeState.currentLanguage == AppLanguage.AR) {
        "الميزة تحتاج اتصالًا بالإنترنت. تحقق من الشبكة وحاول مرة أخرى."
    } else {
        "This feature needs an internet connection. Check your network and try again."
    }
}

class AiViewModelFactory(
    private val repository: AiRepositoryV3,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiViewModel(repository) as T
    }
}
