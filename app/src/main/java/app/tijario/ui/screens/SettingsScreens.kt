package app.tijario.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.AppLanguage
import app.tijario.MainActivity
import app.tijario.config.Supabase
import app.tijario.config.t
import app.tijario.data.remote.ResetPasswordRequest
import app.tijario.ui.state.TijarioDataViewModel
import app.tijario.ui.state.PlanUsageState
import app.tijario.features.notifications.NotificationSettingsSection
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
    onStoreSettings: () -> Unit,
    onAccountSettings: () -> Unit,
    onAppSettings: () -> Unit,
    onUpgrade: () -> Unit,
    onLogout: () -> Unit,
) {
    val uiState by dataViewModel.uiState.collectAsStateWithLifecycle()
    val planUsageState by dataViewModel.planUsageState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { dataViewModel.refreshPlanUsage() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("settings"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF022C30), Color(0xFF0D6E76))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    when (planUsageState) {
                        is PlanUsageState.Loading, PlanUsageState.Idle -> {
                            PlanUsageSkeleton()
                        }
                        is PlanUsageState.Error -> {
                            val errorMessage = (planUsageState as PlanUsageState.Error).message
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(t("current_plan"), color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                                Text(errorMessage, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { dataViewModel.refreshPlanUsage() }) {
                                    Text(t("retry"))
                                }
                            }
                        }
                        is PlanUsageState.Success -> {
                            val usage = (planUsageState as PlanUsageState.Success).value
                            val displayPlanName = when (usage.planCode.lowercase()) {
                                "free" -> t("free_plan")
                                "pro" -> "Pro"
                                "business" -> "Business"
                                else -> usage.planName
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(t("current_plan"), color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                                    Text(displayPlanName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            UsageLine(t("documents"), usage.documentsUsed, usage.documentsLimit, Icons.Filled.Description, Color(0xFFCCFBF1))
                            UsageLine(t("ai_uses"), usage.aiUsed, usage.aiLimit, Icons.Filled.AutoAwesome, Color(0xFFBAE6FD))
                            UsageLine(t("tab_customers"), usage.customersUsed, usage.customersLimit, Icons.Filled.Person, Color(0xFFFDE68A))
                            UsageLine(t("tab_products"), usage.productsUsed, usage.productsLimit, Icons.Filled.ShoppingBag, Color(0xFFC4B5FD))
                        }
                    }
                }
            }

            SettingsOption(Icons.Filled.Business, t("store_settings"), t("store_settings_desc"), onStoreSettings)
            SettingsOption(Icons.Filled.AccountCircle, t("account_settings"), t("account_settings_desc"), onAccountSettings)
            SettingsOption(Icons.Filled.Settings, t("app_settings"), t("app_settings_desc"), onAppSettings)
            SettingsOption(Icons.Filled.WorkspacePremium, t("upgrade_plan"), t("upgrade_plan_desc"), onUpgrade)

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                ),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(t("logout"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PlanUsageSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White.copy(alpha = 0.18f))
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.22f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: suspend () -> Result<Unit> = { Result.success(Unit) },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val noEmailMsg = t("no_email_associated")
    val unexpectedErrorMsg = t("unexpected_error")
    val unknownUserMsg = t("unknown_user")
    val editNameInvalidMsg = t("edit_name_invalid")
    val nameUpdatedMsg = t("name_updated")
    val nameUpdateFailedMsg = t("name_update_failed")
    val savingMsg = t("saving")
    val btnSaveMsg = t("btn_save")
    val sendingPasswordLinkMsg = t("sending_password_link")
    val passwordResetSentMsg = t("password_reset_link_sent")
    val passwordResetFailedMsg = t("password_reset_link_failed")
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var profileFullName by remember { mutableStateOf("") }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameValue by remember { mutableStateOf("") }
    var editNameError by remember { mutableStateOf<String?>(null) }
    var isSavingName by remember { mutableStateOf(false) }
    var isPasswordResetLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val profilePicFile = remember { File(context.filesDir, "personal_profile_pic.jpg") }
    var profilePicBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        email = Supabase.client.auth.currentUserOrNull()?.email.orEmpty()
        profileFullName = dataViewModel.fetchCurrentProfileFullName().orEmpty()
        if (profilePicFile.exists()) {
            profilePicBitmap = android.graphics.BitmapFactory.decodeFile(profilePicFile.absolutePath)
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bytes = input.readBytes()
                    profilePicFile.writeBytes(bytes)
                }
                profilePicBitmap = android.graphics.BitmapFactory.decodeFile(profilePicFile.absolutePath)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val userEmail = email.ifBlank { noEmailMsg }
    val displayName = remember(profileFullName, unknownUserMsg) {
        profileFullName.trim().ifBlank { unknownUserMsg }
    }

    fun maskEmail(mail: String): String {
        if (!mail.contains("@")) return mail
        val parts = mail.split("@")
        val local = parts[0]
        val domain = parts[1]
        if (local.length <= 2) return "$local****@$domain"
        return "${local.take(1)}******${local.takeLast(4)}@$domain"
    }



    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(t("delete_account"), fontWeight = FontWeight.Bold) },
            text = { Text(t("delete_account_desc")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = onDeleteAccount()
                            if (result.isSuccess) {
                                showDeleteConfirm = false
                                onLogout()
                            } else {
                                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: unexpectedErrorMsg)
                            }
                        }
                    }
                ) { Text(t("delete_account")) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(t("btn_cancel")) }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSavingName) {
                    showEditNameDialog = false
                    editNameError = null
                }
            },
            title = { Text(t("edit_name"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(t("edit_name_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    OutlinedTextField(
                        value = editNameValue,
                        onValueChange = {
                            editNameValue = it
                            editNameError = null
                        },
                        label = { Text(t("edit_name_label")) },
                        singleLine = true,
                        enabled = !isSavingName,
                        isError = editNameError != null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    editNameError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isSavingName) return@TextButton
                        val normalized = editNameValue.trim()
                        if (normalized.length !in 2..80) {
                            editNameError = editNameInvalidMsg
                            return@TextButton
                        }
                        scope.launch {
                            isSavingName = true
                            val result = dataViewModel.updateCurrentProfileFullName(normalized)
                            isSavingName = false
                            if (result.isSuccess) {
                                profileFullName = normalized
                                editNameValue = normalized
                                editNameError = null
                                showEditNameDialog = false
                                snackbarHostState.showSnackbar(nameUpdatedMsg)
                            } else {
                                snackbarHostState.showSnackbar(nameUpdateFailedMsg)
                            }
                        }
                    },
                    enabled = !isSavingName
                ) {
                    Text(if (isSavingName) savingMsg else btnSaveMsg)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isSavingName) {
                            showEditNameDialog = false
                            editNameError = null
                        }
                    },
                    enabled = !isSavingName
                ) { Text(t("btn_cancel")) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("tab_personal_account"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Welcome Header Blue Gradient Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF081C36), Color(0xFF0F2D54))
                            )
                        )
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFF0D9488).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text(
                                text = t("personal_account"),
                                color = Color(0xFF2DD4BF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = t("personal_account_desc"),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    // Avatar with edit badge
                    Box(
                        modifier = Modifier.clickable { photoPicker.launch("image/*") }
                    ) {
                        Surface(
                            color = Color(0xFF0D9488),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp),
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (profilePicBitmap != null) {
                                    Image(
                                        bitmap = profilePicBitmap!!.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = displayName.take(2),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                        Surface(
                            color = Color.White,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = (-2).dp, y = 2.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = Color(0xFF081C36),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Account info Card containing name and email
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(20.dp))
                        Text(t("account_info"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Full Name Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(t("fullname"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Icon(Icons.Filled.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            editNameValue = profileFullName
                            editNameError = null
                            showEditNameDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(t("edit_name"), fontWeight = FontWeight.Bold)
                    }

                    // Email Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(maskEmail(userEmail), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(t("email"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Icon(Icons.Filled.MailOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Change Password Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isPasswordResetLoading) {
                        scope.launch {
                            if (email.isBlank()) {
                                snackbarHostState.showSnackbar(noEmailMsg)
                            } else {
                                isPasswordResetLoading = true
                                try {
                                    val result = Supabase.apiClient.requestPasswordReset(ResetPasswordRequest(email))
                                    snackbarHostState.showSnackbar(if (result.ok) passwordResetSentMsg else passwordResetFailedMsg)
                                } finally {
                                    isPasswordResetLoading = false
                                }
                            }
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(18.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isPasswordResetLoading) sendingPasswordLinkMsg else t("change_password"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                            )
                            Text(t("update_password_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        if (isPasswordResetLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
            // Log Out Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFCE8E6),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFC5221F), modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(t("logout"), fontWeight = FontWeight.Bold, color = Color(0xFFC5221F), fontSize = 14.sp)
                            Text(t("logout_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteConfirm = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = null, tint = Color(0xFFB91C1C), modifier = Modifier.size(18.dp))
                            }
                        }
                        Column {
                            Text(t("delete_account"), fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), fontSize = 14.sp)
                            Text(t("delete_account_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(onBack: () -> Unit) {
    val language = LocalLanguage.current
    val context = LocalContext.current
    var showLangDialog by remember { mutableStateOf(false) }

    if (showLangDialog) {
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            title = { Text(t("settings_lang"), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            MainActivity.currentLanguage = AppLanguage.AR
                            AppPreferences.setLanguage(context, AppLanguage.AR)
                            showLangDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("العربية", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    TextButton(
                        onClick = {
                            MainActivity.currentLanguage = AppLanguage.EN
                            AppPreferences.setLanguage(context, AppLanguage.EN)
                            showLangDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("English", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFE4F2F1),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Settings, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(t("app_settings"), fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Language option row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLangDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFE8F0FE),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Language, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(t("settings_lang"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (language == AppLanguage.AR) "العربية" else "English",
                                    color = Color(0xFF0D9488),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                    // Theme option row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFF3E8FF),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.DarkMode, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(t("settings_theme"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (MainActivity.isDarkMode) t("theme_dark") else t("theme_light"),
                                    color = Color(0xFF0D9488),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Switch(
                            checked = MainActivity.isDarkMode,
                            onCheckedChange = {
                                MainActivity.isDarkMode = it
                                AppPreferences.setDarkMode(context, it)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                    NotificationSettingsSection()

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                    // Privacy policy row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://tijario.site/privacy"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0xFFE6F4EA),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Color(0xFF137333), modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(t("privacy_policy"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "tijario.site/privacy",
                                    color = Color(0xFF0D9488),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradePlanScreen(
    dataViewModel: TijarioDataViewModel,
    onBack: () -> Unit,
) {
    val language = LocalLanguage.current
    val isArabic = language == AppLanguage.AR
    val planUsageState by dataViewModel.planUsageState.collectAsStateWithLifecycle()
    var annualBilling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dataViewModel.refreshPlanUsage()
    }

    val usage = (planUsageState as? PlanUsageState.Success)?.value
    val currentPlanCode = when (usage?.planCode?.lowercase()) {
        "starter" -> "pro"
        null, "" -> "free"
        else -> usage.planCode.lowercase()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("upgrade_plan"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val annualDiscountPercent = pricingAnnualDiscountPercent()

            PricingHeroCard(
                isArabic = isArabic,
                annualBilling = annualBilling,
                annualDiscountPercent = annualDiscountPercent,
                onToggleBilling = { annualBilling = it },
            )

            PricingPlansSection(
                isArabic = isArabic,
                currentPlanCode = currentPlanCode,
                annualBilling = annualBilling,
            )

            PricingComparisonSection(isArabic = isArabic)
            PricingFaqSection(isArabic = isArabic)

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (isArabic) "الترقية الفعلية عبر Google Play ستُفعَّل لاحقًا." else "Google Play upgrades will be enabled later.",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                    ) {
                        Text(t("coming_soon_upgrade"), fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

private data class PricingPlanUi(
    val code: String,
    val nameAr: String,
    val nameEn: String,
    val descAr: String,
    val descEn: String,
    val monthlyPriceCents: Int,
    val annualPriceCents: Int,
    val monthlyDocumentLimit: Int,
    val monthlyAiLimit: Int,
    val customerLimit: Int?,
    val productLimit: Int?,
    val templatesAr: String,
    val templatesEn: String,
    val supportAr: String,
    val supportEn: String,
    val featured: Boolean,
)

@Composable
private fun PricingHeroCard(
    isArabic: Boolean,
    annualBilling: Boolean,
    annualDiscountPercent: Int,
    onToggleBilling: (Boolean) -> Unit,
) {
    val title = if (isArabic) "خطة تناسب كل مرحلة من نمو متجرك" else "A plan for every stage of your store"
    val subtitle = if (isArabic) {
        "ابدأ مجانًا ثم قم بالترقية عندما يكبر عملك."
    } else {
        "Start free, then upgrade as your documents, customers, products, and AI usage grow."
    }
    val monthly = if (isArabic) "شهري" else "Monthly"
    val annual = if (isArabic) "سنوي" else "Annual"
    val save = if (isArabic) "خصم سنوي حتى $annualDiscountPercent%" else "Save up to $annualDiscountPercent% annually"

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(color = Color(0xFFE6FFFA), shape = CircleShape, modifier = Modifier.size(72.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.RocketLaunch,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                PricingToggleChip(
                    label = monthly,
                    selected = !annualBilling,
                    onClick = { onToggleBilling(false) },
                )
                Spacer(modifier = Modifier.size(10.dp))
                PricingToggleChip(
                    label = annual,
                    selected = annualBilling,
                    onClick = { onToggleBilling(true) },
                )
            }
            Text(save, color = Color(0xFF0F766E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PricingToggleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF0D9488) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CurrentUsagePricingCard(
    isArabic: Boolean,
    usage: app.tijario.data.model.UserPlanUsage?,
    currentPlanCode: String,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(t("current_plan"), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = when (currentPlanCode) {
                            "free" -> if (isArabic) "الخطة المجانية" else "Free"
                            "pro" -> "Pro"
                            "business" -> "Business"
                            else -> currentPlanCode
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
                Surface(color = Color(0xFFE6FFFA), shape = CircleShape, modifier = Modifier.size(52.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }

            if (usage != null) {
                UsageLine(t("documents"), usage.documentsUsed, usage.documentsLimit, Icons.Filled.Description, Color(0xFFCCFBF1))
                UsageLine(t("ai_uses"), usage.aiUsed, usage.aiLimit, Icons.Filled.AutoAwesome, Color(0xFFBAE6FD))
                UsageLine(t("tab_customers"), usage.customersUsed, usage.customersLimit, Icons.Filled.Person, Color(0xFFFDE68A))
                UsageLine(t("tab_products"), usage.productsUsed, usage.productsLimit, Icons.Filled.ShoppingBag, Color(0xFFC4B5FD))
            } else {
                Text(
                    text = if (isArabic) "جارٍ تحميل الاستهلاك الحالي..." else "Loading current usage...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PricingPlansSection(
    isArabic: Boolean,
    currentPlanCode: String,
    annualBilling: Boolean,
) {
    val plans = listOf(
        PricingPlanUi(
            code = "free",
            nameAr = "مجاني",
            nameEn = "Free",
            descAr = "ابدأ مجانًا مع الحدود الأساسية.",
            descEn = "Start free with the core limits.",
            monthlyPriceCents = 0,
            annualPriceCents = 0,
            monthlyDocumentLimit = 5,
            monthlyAiLimit = 10,
            customerLimit = 10,
            productLimit = 10,
            templatesAr = "القالب الأساسي الأخضر والأبيض فقط",
            templatesEn = "Basic green and white template only",
            supportAr = "دعم قياسي",
            supportEn = "Standard support",
            featured = false,
        ),
        PricingPlanUi(
            code = "pro",
            nameAr = "Pro",
            nameEn = "Pro",
            descAr = "الأكثر اختيارًا للمتاجر النامية.",
            descEn = "Most chosen for growing stores.",
            monthlyPriceCents = 499,
            annualPriceCents = 4900,
            monthlyDocumentLimit = 50,
            monthlyAiLimit = 100,
            customerLimit = null,
            productLimit = null,
            templatesAr = "جميع القوالب الحالية",
            templatesEn = "All current templates",
            supportAr = "دعم قياسي",
            supportEn = "Standard support",
            featured = true,
        ),
        PricingPlanUi(
            code = "business",
            nameAr = "Business",
            nameEn = "Business",
            descAr = "للاستخدام المرتفع والمتاجر النشطة.",
            descEn = "For higher usage and active stores.",
            monthlyPriceCents = 999,
            annualPriceCents = 9900,
            monthlyDocumentLimit = 200,
            monthlyAiLimit = 500,
            customerLimit = null,
            productLimit = null,
            templatesAr = "جميع القوالب الحالية",
            templatesEn = "All current templates",
            supportAr = "دعم أولوية",
            supportEn = "Priority support",
            featured = false,
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (isArabic) "الباقات" else "Plans",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        plans.forEach { plan ->
            PricingPlanCard(
                plan = plan,
                currentPlanCode = currentPlanCode,
                annualBilling = annualBilling,
                isArabic = isArabic,
            )
        }
    }
}

@Composable
private fun PricingPlanCard(
    plan: PricingPlanUi,
    currentPlanCode: String,
    annualBilling: Boolean,
    isArabic: Boolean,
) {
    val isCurrent = plan.code == currentPlanCode
    val isDarkTheme = MainActivity.isDarkMode
    val accent = when (plan.code) {
        "free" -> Color(0xFF0D9488)
        "pro" -> Color(0xFF2563EB)
        else -> Color(0xFF7C3AED)
    }
    val priceCents = if (annualBilling) plan.annualPriceCents else plan.monthlyPriceCents
    val priceLabel = if (priceCents == 0) {
        if (isArabic) "مجاني" else "Free"
    } else {
        val amount = priceCents / 100f
        val period = if (annualBilling) {
            if (isArabic) " / سنويًا" else " / year"
        } else {
            if (isArabic) " / شهريًا" else " / month"
        }
        val planDiscountPercent = pricingAnnualDiscountPercent(plan.monthlyPriceCents, plan.annualPriceCents)
        val discount = if (annualBilling && plan.annualPriceCents > 0) {
            " • ${planDiscountPercent}% ${if (isArabic) "خصم" else "off"}"
        } else {
            ""
        }
        "$${String.format("%.2f", amount)}$period$discount"
    }
    var expanded by remember(plan.code) { mutableStateOf(false) }
    val currentContainer = when {
        isCurrent && isDarkTheme -> Color.White
        isCurrent -> Color(0xFFEFFAF7)
        else -> MaterialTheme.colorScheme.surface
    }
    val currentOnSurface = when {
        isCurrent && isDarkTheme -> Color(0xFF111827)
        isCurrent -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val currentSurfaceVariant = when {
        isCurrent && isDarkTheme -> Color(0xFFF3F4F6)
        isCurrent -> Color(0xFFE0F2FE)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = currentContainer),
        border = BorderStroke(
            1.dp,
            when {
                isCurrent && isDarkTheme -> Color(0xFFE5E7EB)
                plan.featured -> accent
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (plan.featured) 4.dp else 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isArabic) plan.nameAr else plan.nameEn,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = currentOnSurface,
                        )
                        if (plan.featured) {
                            Surface(color = if (isCurrent && isDarkTheme) Color(0xFFF3F4F6) else Color(0xFFE0F2FE), shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = if (isArabic) "الأكثر اختيارًا" else "Most chosen",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (isCurrent && isDarkTheme) Color(0xFF111827) else Color(0xFF075985),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        if (isCurrent) {
                            Surface(color = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFFD1FAE5), shape = RoundedCornerShape(999.dp)) {
                                Text(
                                    text = t("current_plan"),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = if (isDarkTheme) Color(0xFF111827) else Color(0xFF065F46),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isArabic) plan.descAr else plan.descEn,
                        color = if (isCurrent && isDarkTheme) Color(0xFF374151) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = currentSurfaceVariant, shape = CircleShape, modifier = Modifier.size(46.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (plan.code == "free") Icons.Filled.Check else Icons.Filled.WorkspacePremium,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (expanded) {
                                if (isArabic) "إخفاء المزايا" else "Hide features"
                            } else {
                                if (isArabic) "عرض المزايا" else "Show features"
                            },
                            tint = currentOnSurface,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f),
                        )
                    }
                }
            }

            Text(
                text = priceLabel,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = accent,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PricingCompactChip(
                    text = if (isArabic) "مستندات ${plan.monthlyDocumentLimit}" else "Docs ${plan.monthlyDocumentLimit}",
                    accent = accent,
                )
                PricingCompactChip(
                    text = if (isArabic) "AI ${plan.monthlyAiLimit}" else "AI ${plan.monthlyAiLimit}",
                    accent = accent,
                )
                PricingCompactChip(
                    text = if (plan.customerLimit == null) {
                        if (isArabic) "عملاء غير محدود" else "Customers unlimited"
                    } else {
                        if (isArabic) "عملاء ${plan.customerLimit}" else "Customers ${plan.customerLimit}"
                    },
                    accent = accent,
                )
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PricingFeatureRow(
                        text = if (isArabic) "مستندات شهرية: ${plan.monthlyDocumentLimit}" else "Documents/month: ${plan.monthlyDocumentLimit}",
                        accent = accent,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) "عمليات AI شهرية: ${plan.monthlyAiLimit}" else "AI/month: ${plan.monthlyAiLimit}",
                        accent = accent,
                    )
                    PricingFeatureRow(
                        text = if (plan.customerLimit == null) {
                            if (isArabic) "العملاء: غير محدود" else "Customers: Unlimited"
                        } else {
                            if (isArabic) "العملاء: ${plan.customerLimit}" else "Customers: ${plan.customerLimit}"
                        },
                        accent = accent,
                    )
                    PricingFeatureRow(
                        text = if (plan.productLimit == null) {
                            if (isArabic) "المنتجات: غير محدود" else "Products: Unlimited"
                        } else {
                            if (isArabic) "المنتجات: ${plan.productLimit}" else "Products: ${plan.productLimit}"
                        },
                        accent = accent,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) plan.templatesAr else plan.templatesEn,
                        accent = accent,
                    )
                    PricingFeatureRow(
                        text = if (isArabic) plan.supportAr else plan.supportEn,
                        accent = accent,
                    )

                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrent) accent else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = if (isCurrent) accent else MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Text(
                            text = when {
                                isCurrent -> if (isArabic) "الخطة الحالية" else "Current plan"
                                else -> t("coming_soon_upgrade")
                            },
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingCompactChip(text: String, accent: Color) {
    Surface(color = accent.copy(alpha = 0.12f), shape = RoundedCornerShape(999.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = accent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PricingFeatureRow(text: String, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(color = accent.copy(alpha = 0.12f), shape = CircleShape, modifier = Modifier.size(22.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
        Text(text, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun pricingAnnualDiscountPercent(): Int {
    val discounts = listOf(
        pricingAnnualDiscountPercent(499, 4900),
        pricingAnnualDiscountPercent(999, 9900),
    )
    return discounts.maxOrNull() ?: 0
}

private fun pricingAnnualDiscountPercent(monthlyPriceCents: Int, annualPriceCents: Int): Int {
    if (monthlyPriceCents <= 0 || annualPriceCents <= 0) return 0
    return (((monthlyPriceCents * 12) - annualPriceCents) * 100f / (monthlyPriceCents * 12)).toInt()
}

@Composable
private fun PricingComparisonSection(isArabic: Boolean) {
    val title = if (isArabic) "مقارنة كاملة" else "Full comparison"
    val rows = listOf(
        ComparisonRow(
            categoryAr = "الاستخدام والحدود",
            categoryEn = "Usage and limits",
            featureAr = "المستندات الشهرية",
            featureEn = "Monthly documents",
            freeAr = "5",
            freeEn = "5",
            proAr = "50",
            proEn = "50",
            businessAr = "200",
            businessEn = "200",
        ),
        ComparisonRow(
            categoryAr = "الاستخدام والحدود",
            categoryEn = "Usage and limits",
            featureAr = "عمليات AI الشهرية",
            featureEn = "Monthly AI uses",
            freeAr = "10",
            freeEn = "10",
            proAr = "100",
            proEn = "100",
            businessAr = "500",
            businessEn = "500",
        ),
        ComparisonRow(
            categoryAr = "العملاء والمنتجات",
            categoryEn = "Customers and products",
            featureAr = "العملاء",
            featureEn = "Customers",
            freeAr = "10",
            freeEn = "10",
            proAr = "غير محدود",
            proEn = "Unlimited",
            businessAr = "غير محدود",
            businessEn = "Unlimited",
        ),
        ComparisonRow(
            categoryAr = "العملاء والمنتجات",
            categoryEn = "Customers and products",
            featureAr = "المنتجات",
            featureEn = "Products",
            freeAr = "10",
            freeEn = "10",
            proAr = "غير محدود",
            proEn = "Unlimited",
            businessAr = "غير محدود",
            businessEn = "Unlimited",
        ),
        ComparisonRow(
            categoryAr = "العروض والفواتير",
            categoryEn = "Quotes and invoices",
            featureAr = "إنشاء عروض الأسعار والفواتير",
            featureEn = "Quote and invoice creation",
            freeAr = "موجود",
            freeEn = "Included",
            proAr = "موجود",
            proEn = "Included",
            businessAr = "موجود",
            businessEn = "Included",
        ),
        ComparisonRow(
            categoryAr = "أدوات AI",
            categoryEn = "AI tools",
            featureAr = "Smart Reply",
            featureEn = "Smart Reply",
            freeAr = "محدود",
            freeEn = "Limited",
            proAr = "100",
            proEn = "100",
            businessAr = "500",
            businessEn = "500",
        ),
        ComparisonRow(
            categoryAr = "أدوات AI",
            categoryEn = "AI tools",
            featureAr = "Smart Caption",
            featureEn = "Smart Caption",
            freeAr = "محدود",
            freeEn = "Limited",
            proAr = "100",
            proEn = "100",
            businessAr = "500",
            businessEn = "500",
        ),
        ComparisonRow(
            categoryAr = "القوالب",
            categoryEn = "Templates",
            featureAr = "القوالب الحالية",
            featureEn = "Current templates",
            freeAr = "قالب واحد",
            freeEn = "One template",
            proAr = "جميع القوالب",
            proEn = "All templates",
            businessAr = "جميع القوالب",
            businessEn = "All templates",
        ),
        ComparisonRow(
            categoryAr = "الدعم والحساب",
            categoryEn = "Support and account",
            featureAr = "الدعم",
            featureEn = "Support",
            freeAr = "قياسي",
            freeEn = "Standard",
            proAr = "قياسي",
            proEn = "Standard",
            businessAr = "أولوية",
            businessEn = "Priority",
        ),
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonTableHeader(isArabic)
                rows.forEach { row ->
                    PricingComparisonCategoryRow(
                        isArabic = isArabic,
                        row = row,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonTableHeader(isArabic: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ComparisonCell(
            text = if (isArabic) "الميزة" else "Feature",
            bold = true,
            modifier = Modifier.weight(1.3f),
        )
        ComparisonCell(
            text = if (isArabic) "مجاني" else "Free",
            bold = true,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = "Pro",
            bold = true,
            modifier = Modifier.weight(1f),
        )
        ComparisonCell(
            text = if (isArabic) "بزنس" else "Business",
            bold = true,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PricingComparisonCategoryRow(
    isArabic: Boolean,
    row: ComparisonRow,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (isArabic) row.categoryAr else row.categoryEn,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ComparisonCell(
                text = if (isArabic) row.featureAr else row.featureEn,
                modifier = Modifier.weight(1.3f),
            )
            ComparisonCell(
                text = if (isArabic) row.freeAr else row.freeEn,
                modifier = Modifier.weight(1f),
            )
            ComparisonCell(
                text = if (isArabic) row.proAr else row.proEn,
                modifier = Modifier.weight(1f),
            )
            ComparisonCell(
                text = if (isArabic) row.businessAr else row.businessEn,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ComparisonCell(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            fontSize = 12.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
        )
    }
}

private data class ComparisonRow(
    val categoryAr: String,
    val categoryEn: String,
    val featureAr: String,
    val featureEn: String,
    val freeAr: String,
    val freeEn: String,
    val proAr: String,
    val proEn: String,
    val businessAr: String,
    val businessEn: String,
)

@Composable
private fun PricingFaqSection(isArabic: Boolean) {
    val title = if (isArabic) "الأسئلة الشائعة" else "FAQ"
    val items = if (isArabic) {
        listOf(
            "ماذا يحدث عند الوصول إلى الحد؟" to "يمنع الخادم الإجراء الجديد وتظهر لك رسالة واضحة مع زر عرض الباقات.",
            "متى يتجدد الاستخدام؟" to "يتجدد المستند والذكاء الاصطناعي في بداية كل شهر بالتوقيت العالمي.",
            "هل يمكن تغيير أو إلغاء الخطة؟" to "سيُربط الشراء والإلغاء عبر Google Play عند تفعيل الشراء الفعلي.",
            "ماذا يحدث بعد تخفيض الخطة؟" to "لا تُحذف بياناتك. يمكنك عرض الموجود وتعديله، لكن الإضافة الجديدة تخضع للحد الحالي.",
            "هل يمكن البدء مجانًا؟" to "نعم، خطة Free متاحة بدون دفع وتغطي الأساسيات.",
        )
    } else {
        listOf(
            "What happens at the limit?" to "The server blocks the new action and shows a clear limit state with a plans button.",
            "When does usage reset?" to "Document and AI usage reset at the beginning of each UTC month.",
            "Can I change or cancel?" to "Upgrade and cancellation will be handled through Google Play once purchase products are active.",
            "What happens after downgrade?" to "Your data stays. You can view and edit existing records, but new additions follow the current plan limit.",
            "Can I start free?" to "Yes. Free is available without payment and covers the core workflow.",
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        items.forEach { (question, answer) ->
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(question, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(answer, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsOption(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(14.dp), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(icon: ImageVector, title: String, trailing: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(title, fontWeight = FontWeight.Bold)
            }
            trailing()
        }
    }
}

@Composable
private fun UsageLine(title: String, used: Int, limit: Int?, icon: ImageVector, color: Color) {
    val language = LocalLanguage.current
    val isUnlimited = limit == null
    val progress = if (limit == null || limit <= 0) 0f else (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(
                text = when {
                    isUnlimited -> if (language == AppLanguage.AR) "$used من غير محدود" else "$used of unlimited"
                    limit <= 0 -> "$used"
                    else -> "$used ${t("of")} $limit"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        LinearProgressIndicator(
            progress = { progress }, 
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(999.dp)), 
            color = Color(0xFF2DD4BF), 
            trackColor = Color.White.copy(alpha = 0.18f)
        )
    }
}

@Composable
private fun PlanCard(
    title: String, 
    subtitle: String, 
    features: List<String>,
    badgeColor: Color = Color(0xFFE6FFFA),
    badgeTextColor: Color = Color(0xFF0F766E),
    border: BorderStroke? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Surface(
                        color = badgeColor, 
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = title, 
                            color = badgeTextColor, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                    Text(
                        text = subtitle, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        color = Color(0xFF081C36)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
            features.forEach {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFE6FFFA), 
                        shape = CircleShape, 
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome, 
                                contentDescription = null, 
                                tint = Color(0xFF0D9488), 
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = it, 
                        fontWeight = FontWeight.Medium, 
                        fontSize = 14.sp, 
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}
