package app.tijario.data.repository

import app.tijario.data.local.AiGenerationHistoryEntity
import app.tijario.data.local.TijarioDao
import app.tijario.data.remote.AiV3ResponseData
import kotlinx.coroutines.flow.Flow
import java.util.UUID

const val AI_HISTORY_TYPE_REPLY = "reply"
const val AI_HISTORY_TYPE_CAPTION = "caption"

class AiHistoryRepository(
    private val dao: TijarioDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) {
    fun observe(userId: String, generationType: String): Flow<List<AiGenerationHistoryEntity>> =
        dao.observeAiGenerationHistory(userId, generationType)

    suspend fun saveGeneration(
        userId: String,
        generationType: String,
        data: AiV3ResponseData,
    ) {
        val entries = buildAiHistoryEntries(
            userId = userId,
            generationType = generationType,
            data = data,
            createdAt = clock(),
            idFactory = idFactory,
        )
        if (entries.isNotEmpty()) {
            dao.insertAiGenerationHistory(entries)
        }
    }
}

internal fun buildAiHistoryEntries(
    userId: String,
    generationType: String,
    data: AiV3ResponseData,
    createdAt: Long,
    idFactory: () -> String,
): List<AiGenerationHistoryEntity> = data.variants.mapIndexedNotNull { index, variant ->
    val text = variant.text.trim()
    if (text.isBlank()) {
        null
    } else {
        AiGenerationHistoryEntity(
            id = idFactory(),
            userId = userId,
            generationType = generationType,
            generationId = data.generationId,
            variantId = variant.id,
            variantLabel = variant.label,
            variantOrder = index,
            resultText = text,
            createdAt = createdAt,
        )
    }
}
