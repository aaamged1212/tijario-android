package app.tijario.features.ai

import app.tijario.data.remote.AiV3ResponseData
import app.tijario.data.remote.AiV3Usage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiGenerationStateStoreTest {
    @Test
    fun replyResultDoesNotReplaceCaptionResult() {
        val store = AiGenerationStateStore()
        val caption = success("caption-id", "caption")
        val reply = success("reply-id", "reply")

        store.update(AiGenerationTarget.Caption, caption)
        store.update(AiGenerationTarget.Reply, reply)

        assertEquals(reply, store.replyState.value)
        assertEquals(caption, store.captionState.value)
    }

    @Test
    fun captionResultDoesNotReplaceReplyResult() {
        val store = AiGenerationStateStore()
        val reply = success("reply-id", "reply")
        val caption = success("caption-id", "caption")

        store.update(AiGenerationTarget.Reply, reply)
        store.update(AiGenerationTarget.Caption, caption)

        assertEquals(reply, store.replyState.value)
        assertEquals(caption, store.captionState.value)
    }

    @Test
    fun editingOneToolDoesNotChangeTheOtherToolState() {
        val store = AiGenerationStateStore()
        store.update(AiGenerationTarget.Reply, AiV3ScreenState.Editing)

        assertEquals(AiV3ScreenState.Editing, store.replyState.value)
        assertTrue(store.captionState.value is AiV3ScreenState.Idle)
    }

    private fun success(id: String, type: String) = AiV3ScreenState.Success(
        generationType = type,
        data = AiV3ResponseData(
            generationId = id,
            usage = AiV3Usage(used = 1, limit = 10, remaining = 9),
        ),
    )
}
