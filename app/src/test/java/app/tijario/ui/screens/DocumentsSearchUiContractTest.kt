package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentsSearchUiContractTest {
    @Test
    fun invoiceAndQuoteTabsRetainIndependentCompactSearchFields() {
        val source = File("src/main/java/app/tijario/ui/screens/CoreScreens.kt").readText()
        val components = File("src/main/java/app/tijario/ui/components/TijarioComponents.kt").readText()

        assertTrue(source.contains("invoiceSearchQuery"))
        assertTrue(source.contains("quoteSearchQuery"))
        assertTrue(source.contains("search_invoices_placeholder"))
        assertTrue(source.contains("search_quotes_placeholder"))
        assertTrue(source.contains("filterDocumentsBySearch"))
        assertTrue(source.contains("TijarioSearchField("))
        assertTrue(components.contains("fun TijarioSearchField("))
        assertTrue(components.contains("modifier = modifier.height(50.dp)"))
        assertTrue(source.contains("private fun CompactFilterButton("))
        assertTrue(source.contains("imageVector = Icons.Filled.FilterList"))
    }
}
