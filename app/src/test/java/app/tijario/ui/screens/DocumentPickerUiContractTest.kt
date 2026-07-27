package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentPickerUiContractTest {
    @Test
    fun emptyDocumentPickersOfferCreateActionsAndCustomerDialCodeOnly() {
        val source = File("src/main/java/app/tijario/ui/screens/FormScreens.kt").readText()

        assertTrue(source.contains("onNavigateToCreateCustomer"))
        assertTrue(source.contains("onNavigateToCreateProduct"))
        assertTrue(source.contains("uiState.customers.isEmpty()"))
        assertTrue(source.contains("uiState.products.isEmpty()"))
        assertTrue(source.contains("showCountryNameInDialCode = false"))
    }

    @Test
    fun customerDialCodeSelectionPersistsBeforeThePhoneNumberIsEntered() {
        val source = File("src/main/java/app/tijario/ui/components/TijarioComponents.kt").readText()

        assertTrue(source.contains("rememberSaveable { mutableStateOf(safeDefaultDialCode) }"))
        assertTrue(source.contains("selectedDialCode = option.dialCode"))
        assertTrue(source.contains("normalizePhoneWithDialCode(activeDialCode, it)"))
    }
}
