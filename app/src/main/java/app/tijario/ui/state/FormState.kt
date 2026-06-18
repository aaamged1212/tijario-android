package app.tijario.ui.state

import app.tijario.domain.Validation

data class LoginFormState(
    val email: String = "",
    val password: String = "",
) {
    val emailError: String? get() = Validation.email(email)
    val passwordError: String? get() = Validation.password(password)
    val canSubmit: Boolean get() = emailError == null && passwordError == null
}

data class RegisterFormState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
) {
    val fullNameError: String? get() = Validation.required(fullName, "الاسم")
    val emailError: String? get() = Validation.email(email)
    val passwordError: String? get() = Validation.password(password)
    val canSubmit: Boolean get() = fullNameError == null && emailError == null && passwordError == null
}

data class BusinessSettingsFormState(
    val businessName: String = "",
    val whatsapp: String = "",
    val country: String = "السعودية",
    val city: String = "",
    val currency: String = "SAR",
    val terms: String = "",
) {
    val businessNameError: String? get() = Validation.required(businessName, "اسم المتجر")
    val whatsappError: String? get() = Validation.whatsapp(whatsapp)
    val countryError: String? get() = Validation.required(country, "الدولة")
    val currencyError: String? get() = Validation.required(currency, "العملة")
    val canSubmit: Boolean get() =
        businessNameError == null && whatsappError == null && countryError == null && currencyError == null
}

data class CustomerFormState(
    val name: String = "",
    val whatsapp: String = "",
    val city: String = "",
    val notes: String = "",
) {
    val nameError: String? get() = Validation.required(name, "اسم العميل")
    val whatsappError: String? get() = Validation.whatsapp(whatsapp)
    val canSubmit: Boolean get() = nameError == null && whatsappError == null
}

data class DocumentFormState(
    val customerId: String? = null,
    val customerName: String = "",
    val customerWhatsapp: String = "",
    val productId: String? = null,
    val itemName: String = "",
    val quantity: String = "1",
    val unitPrice: String = "0",
    val discount: String = "0",
    val extraFees: String = "0",
    val notes: String = "",
    val terms: String = "",
) {
    val customerNameError: String? get() = Validation.required(customerName, "اسم العميل")
    val itemNameError: String? get() = Validation.required(itemName, "اسم البند")
    val quantityError: String? get() = Validation.positiveInt(quantity, "الكمية")
    val unitPriceError: String? get() = Validation.nonNegativeMoney(unitPrice, "سعر الوحدة")
    val discountError: String? get() = Validation.nonNegativeMoney(discount, "الخصم")
    val extraFeesError: String? get() = Validation.nonNegativeMoney(extraFees, "الرسوم الإضافية")
    val canSubmit: Boolean get() =
        customerId != null &&
            customerName.isNotEmpty() &&
            itemName.isNotEmpty() &&
            quantityError == null &&
            unitPriceError == null &&
            discountError == null &&
            extraFeesError == null
}

data class AiReplyFormState(
    val caseType: String = "customer_inquiry",
    val customerName: String = "",
    val dialect: String = "gulf",
    val tone: String = "friendly",
    val length: String = "short",
    val extraNote: String = "",
)

data class AiCaptionFormState(
    val captionType: String = "product_post",
    val platform: String = "instagram",
    val dialect: String = "gulf",
    val tone: String = "sales",
    val length: String = "short",
    val productOrService: String = "",
    val offer: String = "",
    val extraNote: String = "",
) {
    val productOrServiceError: String? get() = Validation.required(productOrService, "المنتج أو الخدمة")
    val canSubmit: Boolean get() = productOrServiceError == null
}

data class ProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val kind: app.tijario.data.model.ProductKind = app.tijario.data.model.ProductKind.Product,
    val currency: String = "SAR",
) {
    val nameError: String? get() = Validation.required(name, "الاسم")
    val priceError: String? get() = Validation.nonNegativeMoney(price, "السعر")
    val canSubmit: Boolean get() = nameError == null && priceError == null
}
