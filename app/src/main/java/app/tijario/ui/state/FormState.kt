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

data class DocumentItemState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productId: String? = null,
    val name: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val description: String = "",
    val unitOfMeasure: String = "",
    val discount: String = "",
    val discountType: String = "Percentage", // Percentage or Fixed
    val taxRate: String = "",
) {
    val nameError: String? get() = Validation.required(name, "اسم البند")
    val quantityError: String? get() = Validation.positiveInt(quantity, "الكمية")
    val unitPriceError: String? get() = Validation.nonNegativeMoney(unitPrice, "سعر الوحدة")
    val isValid: Boolean get() = name.isNotBlank() && quantity.isNotBlank() && unitPrice.isNotBlank() && nameError == null && quantityError == null && unitPriceError == null
}

data class DocumentFormState(
    val customerId: String? = null,
    val customerName: String = "",
    val customerWhatsapp: String = "",
    val customerCity: String? = null,
    val items: List<DocumentItemState> = listOf(DocumentItemState()),
    val discount: String = "",
    val extraFees: String = "",
    val paymentStatus: String = "unpaid",
    val amountPaid: String = "",
    val notes: String = "",
    val terms: String = "",
    val documentNumber: String = "",
    val issueDate: String = "",
    val creationDate: String = "",
    val dueTerms: String = "None",
    val dueDate: String = "",
    val poNumber: String = "",
    val documentTitle: String = "Online Orders",
    val finalTaxRate: String = "",
    val finalTaxName: String = "الضريبة",
    val documentLanguage: String = "AR", // "AR" or "EN"
) {
    val customerNameError: String? get() = Validation.required(customerName, "اسم العميل")
    val discountError: String? get() = Validation.nonNegativeMoney(discount, "الخصم")
    val extraFeesError: String? get() = Validation.nonNegativeMoney(extraFees, "الرسوم الإضافية")
    val finalTaxRateError: String? get() = Validation.nonNegativeMoney(finalTaxRate, "الضريبة النهائية")
    val amountPaidError: String? get() = if (paymentStatus == "partial") Validation.nonNegativeMoney(amountPaid, "المبلغ المدفوع") else null
    val canSubmit: Boolean get() =
        customerId != null &&
            customerName.isNotEmpty() &&
            items.isNotEmpty() &&
            items.all { it.isValid } &&
            discountError == null &&
            extraFeesError == null &&
            finalTaxRateError == null &&
            amountPaidError == null
}

data class AiReplyFormState(
    val caseType: String = "customer_inquiry",
    val customerName: String = "",
    val customerMessage: String = "",
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
    val stockQuantity: String = "",
    val kind: app.tijario.data.model.ProductKind = app.tijario.data.model.ProductKind.Product,
    val currency: String = "SAR",
) {
    val nameError: String? get() = Validation.required(name, "الاسم")
    val priceError: String? get() = Validation.nonNegativeMoney(price, "السعر")
    val stockQuantityError: String? get() = Validation.nonNegativeInt(stockQuantity, "الكمية")
    val canSubmit: Boolean get() = nameError == null && price.isNotBlank() && priceError == null && stockQuantityError == null
}
