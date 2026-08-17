package app.tijario.features.ai

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiHistoryUiContractTest {
    @Test
    fun replyAndCaptionHistoryRemainSeparateAndCopyable() {
        val viewModelSource = File("src/main/java/app/tijario/features/ai/AiViewModel.kt").readText()
        val screenSource = File("src/main/java/app/tijario/features/ai/AiScreens.kt").readText()

        assertTrue(viewModelSource.contains("val replyHistory"))
        assertTrue(viewModelSource.contains("val captionHistory"))
        assertTrue(viewModelSource.contains("AI_HISTORY_TYPE_REPLY"))
        assertTrue(viewModelSource.contains("AI_HISTORY_TYPE_CAPTION"))
        assertTrue(screenSource.contains("if (selectedTab == 0) replyHistory else captionHistory"))
        assertTrue(screenSource.contains("AiHistoryContent"))
        assertTrue(screenSource.contains("clipboard.setText(AnnotatedString(entry.resultText))"))

        val aboveForms = screenSource
            .substringAfter("// Tab Segmented Control")
            .substringBefore("if (selectedTab == 0)")
        val actionRow = screenSource
            .substringAfter("private fun AiGenerateActionRow")
            .substringBefore("private fun ContextSelectorButton")
        assertTrue(screenSource.contains("onHistoryClick = { showHistorySheet = true }"))
        assertFalse(aboveForms.contains("ai_history"))
        assertTrue(actionRow.contains("modifier = Modifier.size(48.dp)"))
        assertTrue(actionRow.contains("contentDescription = t(\"ai_history\")"))
        assertFalse(actionRow.contains("Text(t(\"ai_history\")"))
    }
}
