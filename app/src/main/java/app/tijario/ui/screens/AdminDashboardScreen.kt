package app.tijario.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tijario.config.AppLanguage
import app.tijario.config.LocalLanguage
import app.tijario.data.remote.AdminAccountActionRequest
import app.tijario.data.remote.AdminAccountDto
import app.tijario.ui.state.TijarioDataViewModel
import kotlinx.coroutines.launch

private data class PendingAdminAction(
    val request: AdminAccountActionRequest,
    val title: String,
    val message: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
) {
    val isArabic = LocalLanguage.current == AppLanguage.AR
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var accounts by remember { mutableStateOf<List<AdminAccountDto>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<AdminAccountDto?>(null) }
    var pendingAction by remember { mutableStateOf<PendingAdminAction?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isApplying by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            isLoading = true
            errorMessage = null
            dataViewModel.getAdminAccounts(query).fold(
                onSuccess = { accounts = it },
                onFailure = { errorMessage = if (isArabic) "تعذر تحميل المستخدمين." else "Could not load users." },
            )
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isArabic) "لوحة تحكم المشرف" else "Admin dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, if (isArabic) "رجوع" else "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (isArabic) "إدارة الحسابات والصلاحيات من مصدر موثوق." else "Manage accounts and allowances from a trusted source.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(if (isArabic) "ابحث بالاسم أو البريد أو UID" else "Search name, email, or UID") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                )
                Button(onClick = ::refresh, enabled = !isLoading) {
                    Text(if (isArabic) "تحديث" else "Refresh")
                }
            }
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (accounts.isEmpty()) {
                Text(if (isArabic) "لا توجد نتائج." else "No matching users.", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                accounts.forEach { account ->
                    AdminAccountCard(account = account, isArabic = isArabic, onClick = { selectedAccount = account })
                }
            }
        }
    }

    selectedAccount?.let { account ->
        AdminAccountActionsSheet(
            account = account,
            isArabic = isArabic,
            isApplying = isApplying,
            onDismiss = { selectedAccount = null },
            onRequestAction = { pendingAction = it },
        )
    }

    pendingAction?.let { pending ->
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(pending.title) },
            text = { Text(pending.message) },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text(if (isArabic) "إلغاء" else "Cancel") } },
            confirmButton = {
                Button(
                    enabled = !isApplying,
                    onClick = {
                        val account = selectedAccount ?: return@Button
                        isApplying = true
                        scope.launch {
                            dataViewModel.applyAdminAccountAction(account.userId, pending.request).fold(
                                onSuccess = {
                                    pendingAction = null
                                    selectedAccount = null
                                    refresh()
                                },
                                onFailure = { errorMessage = if (isArabic) "تعذر تنفيذ الإجراء." else "The action could not be completed." },
                            )
                            isApplying = false
                        }
                    },
                ) { Text(if (isArabic) "تأكيد" else "Confirm") }
            },
        )
    }
}

@Composable
private fun AdminAccountCard(account: AdminAccountDto, isArabic: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (account.isBlocked) Icons.Filled.Block else Icons.Filled.VerifiedUser, null)
                Text(
                    account.fullName?.takeIf(String::isNotBlank) ?: account.email.orEmpty(),
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(account.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            account.businessName?.takeIf(String::isNotBlank)?.let {
                Text(if (isArabic) "النشاط: $it" else "Business: $it")
            }
            Text(
                if (isArabic) "الخطة: ${account.planCode} · ${if (account.isBlocked) "محظور" else account.planStatus}" else "Plan: ${account.planCode} · ${if (account.isBlocked) "Blocked" else account.planStatus}",
                color = MaterialTheme.colorScheme.primary,
            )
            Text("UID: ${account.userId}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminAccountActionsSheet(
    account: AdminAccountDto,
    isArabic: Boolean,
    isApplying: Boolean,
    onDismiss: () -> Unit,
    onRequestAction: (PendingAdminAction) -> Unit,
) {
    var planCode by remember(account.userId) { mutableStateOf(account.planCode) }
    var durationMonths by remember(account.userId) { mutableStateOf("1") }
    var documents by remember(account.userId) { mutableStateOf("") }
    var customers by remember(account.userId) { mutableStateOf("") }
    var products by remember(account.userId) { mutableStateOf("") }
    var aiGenerations by remember(account.userId) { mutableStateOf("") }
    val parsedDuration = durationMonths.toIntOrNull()?.coerceIn(1, 36)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(account.fullName?.takeIf(String::isNotBlank) ?: account.email.orEmpty(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("UID: ${account.userId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text(if (isArabic) "تغيير الخطة" else "Change plan", fontWeight = FontWeight.Bold)
            OutlinedTextField(planCode, { planCode = it.lowercase() }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "الخطة: free / starter / pro" else "Plan: free / starter / pro") }, singleLine = true)
            OutlinedTextField(durationMonths, { durationMonths = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "المدة بالشهور (1 إلى 36)" else "Duration in months (1 to 36)") }, singleLine = true)
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = parsedDuration != null && planCode in setOf("free", "starter", "pro") && !isApplying,
                onClick = {
                    onRequestAction(PendingAdminAction(
                        AdminAccountActionRequest("set_plan", planCode = planCode, durationMonths = parsedDuration),
                        if (isArabic) "تأكيد تغيير الخطة" else "Confirm plan change",
                        if (isArabic) "سيتم تفعيل خطة $planCode لمدة $parsedDuration شهر." else "$planCode will be active for $parsedDuration month(s).",
                    ))
                },
            ) { Text(if (isArabic) "حفظ الخطة" else "Save plan") }

            Text(if (isArabic) "إضافة سماحات" else "Add allowances", fontWeight = FontWeight.Bold)
            OutlinedTextField(documents, { documents = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "مستندات إضافية" else "Additional documents") }, singleLine = true)
            OutlinedTextField(customers, { customers = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "عملاء إضافيون" else "Additional customers") }, singleLine = true)
            OutlinedTextField(products, { products = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "منتجات إضافية" else "Additional products") }, singleLine = true)
            OutlinedTextField(aiGenerations, { aiGenerations = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(if (isArabic) "نقاط AI إضافية" else "Additional AI credits") }, singleLine = true)
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = listOf(documents, customers, products, aiGenerations).any { it.toIntOrNull()?.let { value -> value > 0 } == true } && !isApplying,
                onClick = {
                    onRequestAction(PendingAdminAction(
                        AdminAccountActionRequest(
                            action = "grant_allowance",
                            documents = documents.toIntOrNull() ?: 0,
                            customers = customers.toIntOrNull() ?: 0,
                            products = products.toIntOrNull() ?: 0,
                            aiGenerations = aiGenerations.toIntOrNull() ?: 0,
                        ),
                        if (isArabic) "تأكيد إضافة السماحات" else "Confirm allowances",
                        if (isArabic) "ستضاف السماحات إلى هذا الحساب." else "The allowances will be added to this account.",
                    ))
                },
            ) { Text(if (isArabic) "إضافة السماحات" else "Add allowances") }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isApplying,
                onClick = {
                    val action = if (account.isBlocked) "unblock" else "block"
                    onRequestAction(PendingAdminAction(
                        AdminAccountActionRequest(action),
                        if (isArabic) if (account.isBlocked) "رفع الحظر" else "حظر الحساب" else if (account.isBlocked) "Unblock account" else "Block account",
                        if (isArabic) if (account.isBlocked) "سيتمكن المستخدم من تسجيل الدخول مجددًا." else "سيتم منع المستخدم من تسجيل الدخول." else if (account.isBlocked) "The user will be able to sign in again." else "The user will no longer be able to sign in.",
                    ))
                },
            ) { Text(if (isArabic) if (account.isBlocked) "رفع الحظر" else "حظر الحساب" else if (account.isBlocked) "Unblock account" else "Block account") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isApplying,
                onClick = {
                    onRequestAction(PendingAdminAction(
                        AdminAccountActionRequest("delete"),
                        if (isArabic) "حذف الحساب نهائيًا" else "Delete account permanently",
                        if (isArabic) "سيُحذف الحساب وبياناته نهائيًا ولا يمكن التراجع عن ذلك." else "This permanently deletes the account and its data and cannot be undone.",
                    ))
                },
            ) {
                Icon(Icons.Filled.DeleteForever, null)
                Text(if (isArabic) "حذف الحساب" else "Delete account")
            }
        }
    }
}
