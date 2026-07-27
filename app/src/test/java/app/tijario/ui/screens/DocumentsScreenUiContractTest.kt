package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentsScreenUiContractTest {
    @Test
    fun quotesIgnoreInvoiceOnlyFiltersAndEmptyStatesMatchTheSelectedTab() {
        val source = File("src/main/java/app/tijario/ui/screens/CoreScreens.kt").readText()

        assertTrue(source.contains("if (selectedSection == 1)"))
        assertTrue(source.contains("\"no_invoices_yet\""))
        assertTrue(source.contains("\"no_quotes_yet\""))
        assertTrue(source.contains("text = t(if (selectedSection == 0) \"btn_create_invoice\" else \"btn_create_quote\")"))
    }
}
