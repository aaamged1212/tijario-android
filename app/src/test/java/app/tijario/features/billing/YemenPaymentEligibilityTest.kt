package app.tijario.features.billing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class YemenPaymentEligibilityTest {
    @Test
    fun businessCountryInYemenEnablesLocalPaymentMethods() {
        assertTrue(YemenPaymentEligibility.isEligible("Yemen", "SA"))
        assertTrue(YemenPaymentEligibility.isEligible("اليمن", "US"))
    }

    @Test
    fun yemenDeviceCountryEnablesLocalPaymentMethods() {
        assertTrue(YemenPaymentEligibility.isEligible(null, "ye"))
    }

    @Test
    fun nonYemenBusinessAndDeviceKeepGooglePlayOnly() {
        assertFalse(YemenPaymentEligibility.isEligible("Saudi Arabia", "SA"))
        assertFalse(YemenPaymentEligibility.isEligible(null, "US"))
    }
}
