package app.tijario.data.repository

import app.tijario.data.local.AiGenerationHistoryEntity
import app.tijario.data.local.TijarioDao
import app.tijario.data.remote.AiV3ResponseData
import app.tijario.data.remote.AiV3Usage
import app.tijario.data.remote.AiV3Variant
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiHistoryRepositoryTest {
    @Test
    fun saveGeneration_persistsEveryNonBlankVariantUnderItsOwnType() = runBlocking {
        val dao = mockk<TijarioDao>()
        coEvery { dao.insertAiGenerationHistory(any()) } returns Unit
        val repository = AiHistoryRepository(
            dao = dao,
            clock = { 1_234L },
            idFactory = sequenceOf("history-1", "history-2").iterator()::next,
        )

        repository.saveGeneration(
            userId = "user-1",
            generationType = AI_HISTORY_TYPE_REPLY,
            data = responseData(
                AiV3Variant("quick", "Quick", "  First reply  "),
                AiV3Variant("empty", "Empty", "   "),
                AiV3Variant("professional", "Professional", "Second reply"),
            ),
        )

        val entries = slot<List<AiGenerationHistoryEntity>>()
        coVerify(exactly = 1) { dao.insertAiGenerationHistory(capture(entries)) }
        assertEquals(listOf("First reply", "Second reply"), entries.captured.map { it.resultText })
        assertTrue(entries.captured.all { it.userId == "user-1" })
        assertTrue(entries.captured.all { it.generationType == AI_HISTORY_TYPE_REPLY })
        assertEquals(listOf(0, 2), entries.captured.map { it.variantOrder })
        assertTrue(entries.captured.all { it.createdAt == 1_234L })
    }

    @Test
    fun buildEntries_keepsCaptionAndReplyHistorySeparatedByType() {
        val data = responseData(AiV3Variant("compact", "Compact", "Generated text"))

        val reply = buildAiHistoryEntries("user-1", AI_HISTORY_TYPE_REPLY, data, 10L) { "reply-id" }
        val caption = buildAiHistoryEntries("user-1", AI_HISTORY_TYPE_CAPTION, data, 20L) { "caption-id" }

        assertEquals(AI_HISTORY_TYPE_REPLY, reply.single().generationType)
        assertEquals(AI_HISTORY_TYPE_CAPTION, caption.single().generationType)
        assertEquals("reply-id", reply.single().id)
        assertEquals("caption-id", caption.single().id)
    }

    private fun responseData(vararg variants: AiV3Variant) = AiV3ResponseData(
        generationId = "generation-1",
        variants = variants.toList(),
        usage = AiV3Usage(used = 1, limit = 10, remaining = 9),
    )
}
