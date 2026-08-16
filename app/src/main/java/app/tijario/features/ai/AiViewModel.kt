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
import app.tijario.data.repository.AI_HISTORY_TYPE_CAPTION
import app.tijario.data.repository.AI_HISTORY_TYPE_REPLY
import app.tijario.data.repository.AiHistoryRepository
import app.tijario.domain.LocalizedErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
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

internal enum class AiGenerationTarget {
    Reply,
    Caption,
}

/** Keeps reply and caption output independent while they share one screen. */
internal class AiGenerationStateStore {
    private val replyStateMutable = MutableStateFlow<AiV3ScreenState>(AiV3ScreenState.Idle)
    private val captionStateMutable = MutableStateFlow<AiV3ScreenState>(AiV3ScreenState.Idle)

    val replyState: StateFlow<AiV3ScreenState> = replyStateMutable.asStateFlow()
    val captionState: StateFlow<AiV3ScreenState> = captionStateMutable.asStateFlow()

    fun current(target: AiGenerationTarget): AiV3ScreenState = mutableStateFor(target).value

    fun update(target: AiGenerationTarget, state: AiV3ScreenState) {
        mutableStateFor(target).value = state
    }

    private fun mutableStateFor(target: AiGenerationTarget): MutableStateFlow<AiV3ScreenState> = when (target) {
        AiGenerationTarget.Reply -> replyStateMutable
        AiGenerationTarget.Caption -> captionStateMutable
    }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AiViewModel(
    private val repository: AiRepositoryV3,
    private val historyRepository: AiHistoryRepository,
) : ViewModel() {
    private val generationStates = AiGenerationStateStore()
    private val activeUserId = MutableStateFlow<String?>(null)
    val replyState: StateFlow<AiV3ScreenState> = generationStates.replyState
    val captionState: StateFlow<AiV3ScreenState> = generationStates.captionState
    val replyHistory = activeUserId
        .flatMapLatest { userId ->
            userId?.let { historyRepository.observe(it, AI_HISTORY_TYPE_REPLY) }
                ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())
    val captionHistory = activeUserId
        .flatMapLatest { userId ->
            userId?.let { historyRepository.observe(it, AI_HISTORY_TYPE_CAPTION) }
                ?: flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), emptyList())

    fun setActiveUser(userId: String?) {
        activeUserId.value = userId?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun markReplyEditing() = markEditing(AiGenerationTarget.Reply)

    fun markCaptionEditing() = markEditing(AiGenerationTarget.Caption)

    private fun markEditing(target: AiGenerationTarget) {
        if (generationStates.current(target) is AiV3ScreenState.Idle) {
            generationStates.update(target, AiV3ScreenState.Editing)
        }
    }

    fun generateReply(request: AiV3ReplyRequest, onSuccess: () -> Unit) {
        generationStates.update(AiGenerationTarget.Reply, AiV3ScreenState.Loading)
        viewModelScope.launch {
            runCatching { repository.generateReply(request) }
                .onSuccess { response ->
                    val data = response.data
                    when {
                        response.ok && data != null -> {
                            generationStates.update(AiGenerationTarget.Reply, AiV3ScreenState.Success("reply", data))
                            persistHistory(AI_HISTORY_TYPE_REPLY, data)
                            onSuccess()
                        }

                        response.code.equals("ai_limit_reached", ignoreCase = true) -> {
                            generationStates.update(AiGenerationTarget.Reply, AiV3ScreenState.LimitReached(localizedAiLimitReached()))
                        }

                        else -> generationStates.update(
                            AiGenerationTarget.Reply,
                            AiV3ScreenState.Error(responseFailureMessage(response.code, response.message, response.retryable, localizedReplyError())),
                        )
                    }
                }
                .onFailure { error ->
                    generationStates.update(AiGenerationTarget.Reply, mapFailure(error, localizedReplyError()))
                }
        }
    }

    fun generateCaption(request: AiV3CaptionRequest, onSuccess: () -> Unit) {
        generationStates.update(AiGenerationTarget.Caption, AiV3ScreenState.Loading)
        viewModelScope.launch {
            runCatching { repository.generateCaption(request) }
                .onSuccess { response ->
                    val data = response.data
                    when {
                        response.ok && data != null -> {
                            generationStates.update(AiGenerationTarget.Caption, AiV3ScreenState.Success("caption", data))
                            persistHistory(AI_HISTORY_TYPE_CAPTION, data)
                            onSuccess()
                        }

                        response.code.equals("ai_limit_reached", ignoreCase = true) -> {
                            generationStates.update(AiGenerationTarget.Caption, AiV3ScreenState.LimitReached(localizedAiLimitReached()))
                        }

                        else -> generationStates.update(
                            AiGenerationTarget.Caption,
                            AiV3ScreenState.Error(responseFailureMessage(response.code, response.message, response.retryable, localizedCaptionError())),
                        )
                    }
                }
                .onFailure { error ->
                    generationStates.update(AiGenerationTarget.Caption, mapFailure(error, localizedCaptionError()))
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
        val target = targetFor(previous)
        generationStates.update(target, AiV3ScreenState.Refining(previous))
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
                            generationStates.update(target, AiV3ScreenState.Success(previous.generationType, data))
                            persistHistory(previous.generationType, data)
                            onSuccess()
                        }

                        response.code.equals("ai_limit_reached", ignoreCase = true) -> {
                            generationStates.update(target, AiV3ScreenState.LimitReached(localizedAiLimitReached()))
                        }

                        else -> generationStates.update(
                            target,
                            previous.copy(notice = responseFailureMessage(response.code, response.message, response.retryable, localizedRefineError())),
                        )
                    }
                }
                .onFailure { error ->
                    generationStates.update(target, previous.copy(notice = failureMessage(error, localizedRefineError())))
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
        val target = targetFor(previous)
        generationStates.update(target, AiV3ScreenState.Reporting(previous))
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
                    generationStates.update(
                        target,
                        previous.copy(
                            notice = if (response.ok) localizedReportSuccess() else localizedReportError(),
                        ),
                    )
                    onDone()
                }
                .onFailure { error ->
                    generationStates.update(target, previous.copy(notice = failureMessage(error, localizedReportError())))
                }
        }
    }

    private fun targetFor(success: AiV3ScreenState.Success): AiGenerationTarget =
        if (success.generationType == "caption") AiGenerationTarget.Caption else AiGenerationTarget.Reply

    private fun persistHistory(generationType: String, data: AiV3ResponseData) {
        val userId = activeUserId.value ?: return
        viewModelScope.launch {
            runCatching {
                historyRepository.saveGeneration(
                    userId = userId,
                    generationType = generationType,
                    data = data,
                )
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

    private fun responseFailureMessage(
        code: String?,
        message: String?,
        retryable: Boolean?,
        fallback: String,
    ): String {
        if (retryable == true) {
            return Localization.getString("ai_error_provider_unavailable", AppRuntimeState.currentLanguage)
        }
        return LocalizedErrorMapper.map(code, message, AppRuntimeState.currentLanguage)
            .takeIf { it.isNotBlank() }
            ?: fallback
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
    private val historyRepository: AiHistoryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiViewModel(repository, historyRepository) as T
    }
}
