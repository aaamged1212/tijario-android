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
import androidx.compose.ui.unit.dp
import app.tijario.domain.Validation
import app.tijario.ui.components.TijarioCard
import app.tijario.ui.components.TijarioPage
import app.tijario.ui.components.TijarioTextField

@Composable
fun CustomerFormScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    TijarioPage(
        title = "إضافة عميل",
        subtitle = "يحفظ العميل في جدول customers تحت RLS المستخدم الحالي.",
    ) {
        TijarioTextField("اسم العميل", name, { name = it }, error = Validation.required(name, "اسم العميل"))
        TijarioTextField("رقم واتساب", whatsapp, { whatsapp = it }, error = Validation.whatsapp(whatsapp))
        TijarioTextField("المدينة", city, { city = it }, error = null)
        TijarioTextField("ملاحظات", notes, { notes = it }, error = null, singleLine = false)
        Button(onClick = {}, enabled = false) {
            Text("حفظ العميل")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}

@Composable
fun BusinessSettingsScreen(onBack: () -> Unit) {
    var businessName by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf("") }

    TijarioPage(
        title = "إعدادات المتجر",
        subtitle = "تحديث بيانات النشاط التجاري المستخدمة في المستندات.",
    ) {
        TijarioTextField("اسم المتجر", businessName, { businessName = it }, error = Validation.required(businessName, "اسم المتجر"))
        TijarioTextField("رقم واتساب", whatsapp, { whatsapp = it }, error = Validation.whatsapp(whatsapp))
        TijarioTextField("المدينة", city, { city = it }, error = null)
        TijarioTextField("الشروط", terms, { terms = it }, error = null, singleLine = false)
        Button(onClick = {}, enabled = false) {
            Text("حفظ الإعدادات")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}

@Composable
fun DocumentFormScreen(typeLabel: String, onBack: () -> Unit) {
    var customerName by remember { mutableStateOf("") }
    var customerWhatsapp by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }

    TijarioPage(
        title = "إنشاء $typeLabel",
        subtitle = "الإرسال النهائي يتم عبر API آمن يحسب الرقم والإجماليات وحدود الاستخدام على الخادم.",
    ) {
        TijarioTextField("اسم العميل", customerName, { customerName = it }, error = Validation.required(customerName, "اسم العميل"))
        TijarioTextField("رقم واتساب", customerWhatsapp, { customerWhatsapp = it }, error = Validation.whatsapp(customerWhatsapp))
        TijarioTextField("اسم البند", itemName, { itemName = it }, error = Validation.required(itemName, "اسم البند"))
        TijarioTextField("الكمية", quantity, { quantity = it }, error = Validation.positiveInt(quantity, "الكمية"))
        TijarioTextField("سعر الوحدة", unitPrice, { unitPrice = it }, error = Validation.nonNegativeMoney(unitPrice, "سعر الوحدة"))
        TijarioTextField("الخصم", discount, { discount = it }, error = Validation.nonNegativeMoney(discount, "الخصم"))
        TijarioCard(
            title = "حماية منطق الأعمال",
            body = "هذا النموذج يجمع البيانات فقط. الحساب النهائي والترقيم والاستهلاك الشهري لا يحدث داخل Android.",
        )
        Button(onClick = {}, enabled = false) {
            Text("حفظ $typeLabel")
        }
        OutlinedButton(onClick = onBack) {
            Text("رجوع")
        }
    }
}
