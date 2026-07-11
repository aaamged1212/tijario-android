package app.tijario.data.repository

import app.tijario.data.remote.AiCaptionRequest
import app.tijario.data.remote.AiCaptionResponse
import app.tijario.data.remote.AiReplyRequest
import app.tijario.data.remote.AiReplyResponse
import app.tijario.data.remote.AiV2CaptionRequest
import app.tijario.data.remote.AiV2ReplyRequest
import app.tijario.data.remote.AiV2Response
import app.tijario.data.remote.AiV2UsageResponse
import app.tijario.data.remote.AiV2ReportRequest
import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.BackendApiClient

class AiRepository(
    private val backendApiClient: BackendApiClient,
) {
    suspend fun generateReply(request: AiReplyRequest): ApiResult<AiReplyResponse> =
        backendApiClient.generateAiReply(request)

    suspend fun generateCaption(request: AiCaptionRequest): ApiResult<AiCaptionResponse> =
        backendApiClient.generateAiCaption(request)

    suspend fun generateReplyV2(request: AiV2ReplyRequest): AiV2Response =
        backendApiClient.generateAiReplyV2(request)

    suspend fun generateCaptionV2(request: AiV2CaptionRequest): AiV2Response =
        backendApiClient.generateAiCaptionV2(request)

    suspend fun reportV2(request: AiV2ReportRequest): ApiResult<Unit> =
        backendApiClient.reportAiV2(request)

    suspend fun fetchUsageV2(): AiV2UsageResponse =
        backendApiClient.fetchAccountUsageV2()
}
