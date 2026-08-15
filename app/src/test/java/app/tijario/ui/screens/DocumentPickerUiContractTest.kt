package app.tijario.ui.screens

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentPickerUiContractTest {
    @Test
    fun documentPickersAlwaysOfferCreateActionsAndCustomerDialCodeOnly() {
        val source = File("src/main/java/app/tijario/ui/screens/FormScreens.kt").readText()

        assertTrue(source.contains("onNavigateToCreateCustomer"))
        assertTrue(source.contains("onNavigateToCreateProduct"))
        assertTrue(!source.contains("if (uiState.customers.isEmpty())"))
        assertTrue(!source.contains("if (uiState.products.isEmpty())"))
        assertTrue(source.contains("showCountryNameInDialCode = false"))
        assertTrue(source.contains("Text(t(\"picker_new\"))"))
        assertTrue(source.contains("onNavigateToCreateProduct(rowIndex)"))
    }

    @Test
    fun customerDialCodeSelectionPersistsBeforeThePhoneNumberIsEntered() {
        val source = File("src/main/java/app/tijario/ui/components/TijarioComponents.kt").readText()

        assertTrue(source.contains("rememberSaveable { mutableStateOf(safeDefaultDialCode) }"))
        assertTrue(source.contains("selectedDialCode = option.dialCode"))
        assertTrue(source.contains("normalizePhoneWithDialCode(activeDialCode, it)"))
        assertTrue(source.contains("filterDialCodeOptions(dialCodeQuery, language)"))
        assertTrue(source.contains("ModalBottomSheet"))
    }

    @Test
    fun documentScopedCreationReturnsTheSavedCustomerOrProductToTheForm() {
        val formSource = File("src/main/java/app/tijario/ui/screens/FormScreens.kt").readText()
        val appSource = File("src/main/java/app/tijario/ui/TijarioApp.kt").readText()

        assertTrue(formSource.contains("onCustomerSaved(customer)"))
        assertTrue(formSource.contains("onProductSaved(product)"))
        assertTrue(formSource.contains("onSelectedCustomerConsumed()"))
        assertTrue(appSource.contains("customer-form-for-document"))
        assertTrue(appSource.contains("product-form-for-document?rowIndex={rowIndex}"))
        assertTrue(appSource.contains("activeSelectedCustomer = customer"))
        assertTrue(appSource.contains("activeSelectedProduct = product"))
    }
}
