package app.tijario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tijario.ui.components.TijarioCard
import app.tijario.ui.components.TijarioPage
import app.tijario.ui.components.TijarioTextField
import app.tijario.ui.state.BusinessSettingsFormState
import app.tijario.ui.state.CustomerFormState
import app.tijario.ui.state.DocumentFormState

@Composable
fun CustomerFormScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf(CustomerFormState()) }

    TijarioPage(
        title = "إضافة عميل",
        subtitle = "يحفظ العميل في جدول customers تحت RLS المستخدم الحالي.",
    ) {
        TijarioTextField("اسم العميل", form.name, { form = form.copy(name = it) }, error = form.nameError)
        TijarioTextField("رقم واتساب", form.whatsapp, { form = form.copy(whatsapp = it) }, error = form.whatsappError)
        TijarioTextField("المدينة", form.city, { form = form.copy(city = it) }, error = null)
        TijarioTextField("ملاحظات", form.notes, { form = form.copy(notes = it) }, error = null, singleLine = false)
        Button(onClick = {}, enabled = false && form.canSubmit) {
            Text("حفظ العميل")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}

@Composable
fun BusinessSettingsScreen(onBack: () -> Unit) {
    var form by remember { mutableStateOf(BusinessSettingsFormState()) }

    TijarioPage(
        title = "إعدادات المتجر",
        subtitle = "تحديث بيانات النشاط التجاري المستخدمة في المستندات.",
    ) {
        TijarioTextField("اسم المتجر", form.businessName, { form = form.copy(businessName = it) }, error = form.businessNameError)
        TijarioTextField("رقم واتساب", form.whatsapp, { form = form.copy(whatsapp = it) }, error = form.whatsappError)
        TijarioTextField("المدينة", form.city, { form = form.copy(city = it) }, error = null)
        TijarioTextField("الشروط", form.terms, { form = form.copy(terms = it) }, error = null, singleLine = false)
        Button(onClick = {}, enabled = false && form.canSubmit) {
            Text("حفظ الإعدادات")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}

@Composable
fun DocumentFormScreen(typeLabel: String, onBack: () -> Unit) {
    var form by remember { mutableStateOf(DocumentFormState()) }

    TijarioPage(
        title = "إنشاء $typeLabel",
        subtitle = "الإرسال النهائي يتم عبر API آمن يحسب الرقم والإجماليات وحدود الاستخدام على الخادم.",
    ) {
        TijarioTextField("اسم العميل", form.customerName, { form = form.copy(customerName = it) }, error = form.customerNameError)
        TijarioTextField("رقم واتساب", form.customerWhatsapp, { form = form.copy(customerWhatsapp = it) }, error = form.customerWhatsappError)
        TijarioTextField("اسم البند", form.itemName, { form = form.copy(itemName = it) }, error = form.itemNameError)
        TijarioTextField("الكمية", form.quantity, { form = form.copy(quantity = it) }, error = form.quantityError)
        TijarioTextField("سعر الوحدة", form.unitPrice, { form = form.copy(unitPrice = it) }, error = form.unitPriceError)
        TijarioTextField("الخصم", form.discount, { form = form.copy(discount = it) }, error = form.discountError)
        TijarioTextField("رسوم إضافية", form.extraFees, { form = form.copy(extraFees = it) }, error = form.extraFeesError)
        TijarioCard(
            title = "حماية منطق الأعمال",
            body = "هذا النموذج يجمع البيانات فقط. الحساب النهائي والترقيم والاستهلاك الشهري لا يحدث داخل Android.",
        )
        Button(onClick = {}, enabled = false && form.canSubmit) {
            Text("حفظ $typeLabel")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}
