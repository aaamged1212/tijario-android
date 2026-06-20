package app.tijario.features.documents

import app.tijario.data.model.BusinessSettings
import app.tijario.data.model.CompleteDocument
import app.tijario.data.model.Customer
import app.tijario.data.model.DocumentItem
import app.tijario.data.model.DocumentType
import app.tijario.ui.state.DocumentFormState
import app.tijario.ui.state.DocumentItemState

object DocumentFixtures {
    val business = BusinessSettings(
        businessName = "متجر تجاريو للتجربة",
        whatsappNumber = "+966500000000",
        country = "السعودية",
        city = "الرياض",
        currency = "SAR",
        invoiceNote = "شكرا لتعاملكم معنا",
        termsText = "الدفع خلال سبعة أيام.",
    )

    val customer = Customer(
        id = "customer-1",
        name = "شركة العميل التجارية",
        whatsappNumber = "+966511111111",
        city = "جدة",
    )

    fun draftForm(): DocumentFormState =
        DocumentFormState(
            customerId = "customer-1",
            customerName = customer.name,
            customerWhatsapp = customer.whatsappNumber,
            items = listOf(
                DocumentItemState(id = "item-1", name = "تصميم فاتورة", quantity = "2", unitPrice = "150"),
                DocumentItemState(id = "item-2", name = "خدمة متابعة", quantity = "1", unitPrice = "75.50"),
            ),
            discount = "25",
            extraFees = "10",
            notes = "ملاحظة خاصة <script>alert(1)</script>",
            terms = "لا يتم نسخ أي قالب تجاري.",
        )

    fun saved(type: DocumentType = DocumentType.Invoice, paymentStatus: String? = "paid"): CompleteDocument =
        CompleteDocument(
            id = "doc-1",
            userId = "user-1",
            customerId = "customer-1",
            type = type,
            documentNumber = if (type == DocumentType.Invoice) "INV-1130" else "QT-1024",
            status = "sent",
            paymentStatus = paymentStatus,
            issueDate = "2026-06-20",
            subtotal = 375.50,
            discount = 25.0,
            extraFees = 10.0,
            total = 360.50,
            currency = "SAR",
            notes = "ملاحظة رسمية",
            termsText = "الشروط الرسمية",
            customer = customer,
            items = listOf(
                DocumentItem("line-1", "doc-1", null, "تصميم فاتورة", "وصف طويل قابل للالتفاف", 2, 150.0),
                DocumentItem("line-2", "doc-1", null, "خدمة متابعة", null, 1, 75.50),
            ),
        )
}
