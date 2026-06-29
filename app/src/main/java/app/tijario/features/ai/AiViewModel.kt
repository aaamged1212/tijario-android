package app.tijario.features.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tijario.data.remote.AiV3CaptionRequest
import app.tijario.data.remote.AiV3ReplyRequest
import app.tijario.data.remote.AiV3ReportRequest
import app.tijario.data.remote.AiV3RefineRequest
import app.tijario.data.remote.AiV3ResponseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
                            stateMutable.value = AiV3ScreenState.LimitReached(response.message ?: "تم الوصول إلى حد استخدام الذكاء الاصطناعي.")
                        }
                        else -> stateMutable.value = AiV3ScreenState.Error(response.message ?: "تعذر توليد الرد الآن.")
                    }
                }
                .onFailure { error -> stateMutable.value = mapFailure(error, "تعذر توليد الرد الآن.") }
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
                            stateMutable.value = AiV3ScreenState.LimitReached(response.message ?: "تم الوصول إلى حد استخدام الذكاء الاصطناعي.")
                        }
                        else -> stateMutable.value = AiV3ScreenState.Error(response.message ?: "تعذر توليد الكابشن الآن.")
                    }
                }
                .onFailure { error -> stateMutable.value = mapFailure(error, "تعذر توليد الكابشن الآن.") }
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
                            stateMutable.value = AiV3ScreenState.LimitReached(response.message ?: "تم الوصول إلى حد استخدام الذكاء الاصطناعي.")
                        }
                        else -> stateMutable.value = previous.copy(notice = response.message ?: "تعذر تحسين النص الآن.")
                    }
                }
                .onFailure { error ->
                    stateMutable.value = previous.copy(notice = failureMessage(error, "تعذر تحسين النص الآن."))
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
                        notice = if (response.ok) response.message ?: "تم إرسال البلاغ." else response.message ?: "تعذر إرسال البلاغ.",
                    )
                    onDone()
                }
                .onFailure { error ->
                    stateMutable.value = previous.copy(notice = failureMessage(error, "تعذر إرسال البلاغ."))
                }
        }
    }

    private fun mapFailure(error: Throwable, fallback: String): AiV3ScreenState =
        if (error is IOException) {
            AiV3ScreenState.Offline("الميزة تحتاج اتصال بالإنترنت. تحقق من الشبكة وحاول مرة أخرى.")
        } else {
            AiV3ScreenState.Error(failureMessage(error, fallback))
        }

    private fun failureMessage(error: Throwable, fallback: String): String =
        error.message?.takeIf { it.isNotBlank() } ?: fallback
}

class AiViewModelFactory(
    private val repository: AiRepositoryV3,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AiViewModel(repository) as T
    }
}
