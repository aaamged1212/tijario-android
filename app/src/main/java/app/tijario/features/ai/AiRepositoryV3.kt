package app.tijario.features.ai

import app.tijario.data.remote.AiV3CaptionRequest
import app.tijario.data.remote.AiV3ReplyRequest
import app.tijario.data.remote.AiV3ReportRequest
import app.tijario.data.remote.AiV3ReportResponse
import app.tijario.data.remote.AiV3RefineRequest
import app.tijario.data.remote.AiV3Response
import app.tijario.data.remote.BackendApiClient

class AiRepositoryV3(
    private val backendApiClient: BackendApiClient,
) {
    suspend fun generateReply(request: AiV3ReplyRequest): AiV3Response =
        backendApiClient.generateAiReplyV3(request)

    suspend fun generateCaption(request: AiV3CaptionRequest): AiV3Response =
        backendApiClient.generateAiCaptionV3(request)

    suspend fun refine(request: AiV3RefineRequest): AiV3Response =
        backendApiClient.refineAiV3(request)

    suspend fun report(request: AiV3ReportRequest): AiV3ReportResponse =
        backendApiClient.reportAiV3(request)
}
