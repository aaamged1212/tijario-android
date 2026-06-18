package app.tijario.data.repository

import app.tijario.data.remote.AiCaptionRequest
import app.tijario.data.remote.AiReplyRequest
import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.BackendApiClient

class AiRepository(
    private val backendApiClient: BackendApiClient,
) {
    suspend fun generateReply(request: AiReplyRequest): ApiResult<Map<String, String>> =
        backendApiClient.generateAiReply(request)

    suspend fun generateCaption(request: AiCaptionRequest): ApiResult<Map<String, String>> =
        backendApiClient.generateAiCaption(request)
}
