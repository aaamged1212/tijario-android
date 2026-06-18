package app.tijario.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConfigurationRequiredScreen() {
    ScreenSurface {
        SectionTitle(
            title = "إعدادات الاتصال مطلوبة",
            subtitle = "أضف قيم Supabase العامة ورابط API في إعدادات Gradle المحلية غير المتتبعة قبل تشغيل التطبيق.",
        )
        InfoCard(
            title = "لا تضف أسرار",
            body = "مسموح فقط برابط Supabase العام، المفتاح العام، ورابط API. لا تضف service-role key أو Replicate token أو كلمات مرور.",
        )
    }
}

@Composable
fun DashboardScreen() {
    ScreenSurface {
        SectionTitle(
            title = "تجاري",
            subtitle = "لوحة تحكم Native لإدارة العملاء، العروض، الفواتير، وأدوات الذكاء الاصطناعي.",
        )
        val actions = listOf("إنشاء عرض سعر", "إنشاء فاتورة", "إضافة عميل", "أدوات AI")
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(actions) { action ->
                InfoCard(title = action, body = "سيتم ربط هذا الإجراء بتدفقه الكامل ضمن مراحل التنفيذ التالية.")
            }
        }
    }
}

@Composable
fun CustomersScreen() {
    ScreenSurface {
        SectionTitle(
            title = "العملاء",
            subtitle = "إدارة العملاء ستستخدم RLS الحالي في جدول customers.",
        )
        Button(onClick = {}) {
            Text("إضافة عميل")
        }
        InfoCard("حالة التنفيذ", "قائمة العملاء ونماذج الإضافة والتعديل والحذف في المرحلة القادمة.")
    }
}

@Composable
fun DocumentsScreen() {
    ScreenSurface {
        SectionTitle(
            title = "المستندات",
            subtitle = "العروض والفواتير ستقرأ من Supabase، والإنشاء وPDF عبر API آمن.",
        )
        OutlinedButton(onClick = {}) {
            Text("إنشاء عرض سعر")
        }
        OutlinedButton(onClick = {}) {
            Text("إنشاء فاتورة")
        }
        InfoCard("حد أمني", "الترقيم، الإجماليات، وحدود الاستخدام لا يتم حسابها داخل Android.")
    }
}

@Composable
fun AiToolsScreen() {
    ScreenSurface {
        SectionTitle(
            title = "أدوات AI",
            subtitle = "ردود العملاء والكابتشن تعمل عبر API آمن فقط.",
        )
        InfoCard("ردود AI", "سيتم إرسال الطلب للخادم مع جلسة Supabase.")
        InfoCard("كابتشن AI", "لا يتم وضع Replicate token داخل التطبيق.")
    }
}

@Composable
fun AccountScreen() {
    ScreenSurface {
        SectionTitle(
            title = "الحساب والاستخدام",
            subtitle = "عرض الخطة الحالية وحدود المستندات وAI من الجداول الحالية.",
        )
        InfoCard("الخطة", "free / starter / pro حسب جدول plans و user_plan.")
        InfoCard("الاستخدام الشهري", "documents_used و ai_used من usage_counters.")
    }
}

@Composable
private fun ScreenSurface(content: @Composable Column.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
