package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardRecentDocumentsUiContractTest {
    @Test
    fun viewAllPlacesTextBeforeDirectionAwareForwardArrow() {
        val source = File("src/main/java/app/tijario/ui/screens/CoreScreens.kt").readText()
        val action = source
            .substringAfter("TextButton(onClick = { onViewAllDocuments(latestDocumentsType) })")
            .substringBefore("TijarioFilterChip(")

        assertTrue(action.indexOf("Text(t(\"view_all\")") < action.indexOf("Icon("))
        assertTrue(action.contains("Icons.AutoMirrored.Filled.KeyboardArrowRight"))
        assertFalse(action.contains("Icons.AutoMirrored.Filled.KeyboardArrowLeft"))
    }
}
