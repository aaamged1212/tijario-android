package app.tijario.features.billing

import app.tijario.domain.CountryCatalog

/**
 * Keeps the local payment-method screen limited to Yemen without using IP
 * geolocation or sending any location data to a server.
 */
object YemenPaymentEligibility {
    fun isEligible(
        businessCountry: String?,
        deviceCountryCode: String?,
    ): Boolean =
        CountryCatalog.find(businessCountry)?.countryCode == "YE" ||
            deviceCountryCode.equals("YE", ignoreCase = true)
}
