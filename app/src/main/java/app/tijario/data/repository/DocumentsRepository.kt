package app.tijario.data.repository

import app.tijario.data.remote.ApiResult
import app.tijario.data.remote.BackendApiClient
import app.tijario.data.remote.CreateDocumentRequest
import app.tijario.data.remote.CreateDocumentResponse

class DocumentsRepository(
    private val backendApiClient: BackendApiClient,
) {
    suspend fun createDocument(request: CreateDocumentRequest): ApiResult<CreateDocumentResponse> =
        backendApiClient.createDocument(request)

    suspend fun updateDocument(
        documentId: String,
        request: CreateDocumentRequest,
    ): ApiResult<CreateDocumentResponse> =
        backendApiClient.updateDocument(documentId, request)

    suspend fun fetchPdf(documentId: String): ByteArray =
        backendApiClient.fetchDocumentPdf(documentId)
}
