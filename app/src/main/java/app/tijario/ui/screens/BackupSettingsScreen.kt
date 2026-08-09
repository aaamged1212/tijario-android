package app.tijario.ui.screens

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.features.backup.BackupViewModel
import app.tijario.features.backup.BackupWorkNotifier
import app.tijario.features.backup.PhoneBackupRepository
import app.tijario.features.backup.RestoreBackupDocumentContract
import app.tijario.features.backup.backupCompletedAt
import app.tijario.features.backup.drive.DriveConnectionState
import app.tijario.features.backup.drive.DriveBackupFile
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    userId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val backupViewModel: BackupViewModel = viewModel(
        key = "backup-settings-$userId",
        factory = remember(context, userId) {
            BackupViewModel.factory(context.applicationContext as Application, userId)
        },
    )
    val state by backupViewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingDriveRestore by remember { mutableStateOf<DriveBackupFile?>(null) }
    var pendingLocalRestore by remember { mutableStateOf<app.tijario.data.local.BackupRecordEntity?>(null) }
    var pendingNotificationAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pendingLegacyPhoneBackupAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var showLegacyFolderChoice by remember { mutableStateOf(false) }
    val backupNotifier = remember(context) { BackupWorkNotifier(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestPendingNotificationAction by rememberUpdatedState(pendingNotificationAction)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
        backupViewModel::exportPreparedBackup,
    )
    val restoreLauncher = rememberLauncherForActivityResult(
        remember(context, userId) { RestoreBackupDocumentContract(context, userId) },
    ) { uri ->
        pendingRestoreUri = uri
    }
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        backupViewModel.rememberPhoneBackupFolder(it)
    }
    val legacyStoragePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pendingLegacyPhoneBackupAction
        pendingLegacyPhoneBackupAction = null
        if (granted) action?.invoke() else showLegacyFolderChoice = true
    }
    val driveAuthorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        backupViewModel.completeGoogleDriveAuthorization(result.data)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && backupNotifier.notificationsAvailable()) {
            pendingNotificationAction?.invoke()
            pendingNotificationAction = null
        } else {
            showNotificationRationale = true
        }
    }
    val startTrackedOperation: ((() -> Unit) -> Unit) = { action ->
        if (!backupNotifier.notificationsAvailable()) {
            pendingNotificationAction = action
            showNotificationRationale = true
        } else {
            action()
        }
    }
    val startPhoneBackup: () -> Unit = {
        val action = { startTrackedOperation { backupViewModel.saveBackupToPhone() } }
        if (PhoneBackupRepository(context).requiresLegacyWritePermission(userId)) {
            pendingLegacyPhoneBackupAction = action
            legacyStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            action()
        }
    }

    DisposableEffect(lifecycleOwner, backupNotifier) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && backupNotifier.notificationsAvailable()) {
                latestPendingNotificationAction?.let { action ->
                    pendingNotificationAction = null
                    showNotificationRationale = false
                    action()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(state.exportFileName) {
        state.exportFileName?.let { name ->
            backupViewModel.consumeExportRequest()
            exportLauncher.launch(name)
        }
    }
    LaunchedEffect(state.messageKey) {
        state.messageKey?.let { key ->
            snackbar.showSnackbar(Localization.getString(key, language))
            backupViewModel.consumeMessage()
        }
    }
    LaunchedEffect(state.openDriveFolderUrl) {
        state.openDriveFolderUrl?.let { url ->
            backupViewModel.consumeOpenDriveFolderRequest()
            runCatching {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
    LaunchedEffect(state.shareIntent) {
        state.shareIntent?.let { intent ->
            backupViewModel.consumeShareRequest()
            runCatching { context.startActivity(intent) }
        }
    }
    LaunchedEffect(state.driveAuthorizationIntentSender) {
        state.driveAuthorizationIntentSender?.let { sender ->
            backupViewModel.consumeDriveAuthorizationRequest()
            driveAuthorizationLauncher.launch(IntentSenderRequest.Builder(sender).build())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("backup_restore"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(t("backup_local_title"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(t("backup_local_description"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.latestBackup?.let { record ->
                        Text(
                            text = "${t("backup_last_created")}: ${formatBackupTime(record.createdAt, language.name)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "${t("backup_size")}: ${formatBytes(record.sizeBytes)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "${t("backup_phone_location")}: ${state.phoneBackupDestinationDisplayName ?: t(state.phoneBackupDestination.ifBlank { "backup_phone_folder_required" })}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedButton(
                        onClick = { folderLauncher.launch(null) },
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Folder, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(t("backup_phone_folder"))
                    }
                    if (state.phoneBackupDestination == "backup_phone_folder_selected") {
                        TextButton(
                            onClick = backupViewModel::useDefaultPhoneBackupFolder,
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(t("backup_phone_folder_default")) }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(t("backup_schedule"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "manual" to t("backup_frequency_manual"),
                            "daily" to t("backup_frequency_daily"),
                            "weekly" to t("backup_frequency_weekly"),
                        ).forEach { (value, label) ->
                            FilterChip(
                                selected = state.settings?.frequency == value,
                                onClick = { backupViewModel.updateFrequency(value) },
                                label = { Text(label) },
                                enabled = !state.isBusy && state.backupPlanPolicy.allows(value),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t("backup_charging_only"), fontWeight = FontWeight.SemiBold)
                            Text(
                                t("backup_charging_only_desc"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(
                            checked = state.settings?.chargingOnly == true,
                            onCheckedChange = backupViewModel::updateChargingOnly,
                            enabled = !state.isBusy,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(t("backup_wifi_only"), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.settings?.wifiOnly == true,
                            onCheckedChange = backupViewModel::updateWifiOnly,
                            enabled = !state.isBusy,
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(t("backup_drive_title"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (state.driveConnectionState) {
                            DriveConnectionState.NotConfigured -> t("backup_drive_not_configured")
                            DriveConnectionState.Disconnected -> t("backup_drive_disconnected")
                            DriveConnectionState.AuthorizationRequired -> t("backup_drive_permission_denied")
                            DriveConnectionState.Authorizing -> t("backup_drive_authorizing")
                            DriveConnectionState.ReauthorizationRequired -> t("backup_drive_reauthorization_required")
                            DriveConnectionState.InvalidConfiguration -> t("backup_drive_invalid_configuration")
                            DriveConnectionState.TemporarilyUnavailable -> t("backup_drive_temporarily_unavailable")
                            is DriveConnectionState.Connected -> t("backup_drive_connected")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (!backupNotifier.notificationsAvailable()) {
                        TextButton(
                            onClick = {
                                context.startActivity(backupNotifier.settingsIntent())
                            },
                            enabled = !state.isBusy,
                        ) { Text(t("backup_notifications_settings")) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(t("backup_drive_auto_upload"), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(
                            checked = state.settings?.driveEnabled == true,
                            onCheckedChange = backupViewModel::updateDriveEnabled,
                            enabled = !state.isBusy && state.driveConnectionState is DriveConnectionState.Connected,
                        )
                    }
                    val driveState = state.driveConnectionState
                    when (driveState) {
                        DriveConnectionState.Disconnected,
                        DriveConnectionState.AuthorizationRequired,
                        DriveConnectionState.ReauthorizationRequired,
                        DriveConnectionState.InvalidConfiguration,
                        DriveConnectionState.TemporarilyUnavailable,
                        DriveConnectionState.NotConfigured -> OutlinedButton(
                            onClick = { backupViewModel.connectGoogleDrive() },
                            enabled = !state.isBusy && driveState != DriveConnectionState.NotConfigured,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(t("backup_drive_connect")) }
                        is DriveConnectionState.Connected -> {
                            driveState.accountEmail?.let { email ->
                                Text(email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedButton(
                                onClick = { backupViewModel.connectGoogleDrive(changeAccount = true) },
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(t("backup_drive_change_account")) }
                            TextButton(
                                onClick = { backupViewModel.disconnectGoogleDrive(revoke = false) },
                                enabled = !state.isBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(t("backup_drive_disconnect")) }
                        }
                        DriveConnectionState.Authorizing -> Unit
                    }
                    state.latestBackup?.takeIf { it.status == "DRIVE_FAILED" }?.let { record ->
                        OutlinedButton(
                            onClick = { startTrackedOperation { backupViewModel.retryDriveUpload(record.id) } },
                            enabled = !state.isBusy && state.driveConnectionState is DriveConnectionState.Connected,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(t("backup_drive_retry")) }
                    }
                    if (state.driveConnectionState is DriveConnectionState.Connected) {
                        OutlinedButton(
                            onClick = backupViewModel::requestOpenDriveFolder,
                            enabled = !state.isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text(t("backup_drive_open_folder"))
                        }
                    }
                    if (state.driveBackups.isNotEmpty()) {
                        Text(t("backup_drive_available"), fontWeight = FontWeight.SemiBold)
                        state.driveBackups.take(5).forEach { remote ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(formatBackupTime(remote.createdAt, language.name), style = MaterialTheme.typography.bodySmall)
                                    Text(formatBytes(remote.sizeBytes), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { pendingDriveRestore = remote }, enabled = !state.isBusy) {
                                    Icon(Icons.Filled.CloudDownload, contentDescription = t("backup_drive_restore"))
                                }
                                IconButton(
                                    onClick = { backupViewModel.deleteDriveBackup(remote) },
                                    enabled = !state.isBusy && state.driveBackups.firstOrNull()?.id != remote.id,
                                ) { Icon(Icons.Filled.Delete, contentDescription = t("backup_drive_delete")) }
                            }
                        }
                    }
                    Button(
                        onClick = { startTrackedOperation(backupViewModel::backupNowToGoogleDrive) },
                        enabled = !state.isBusy && state.driveConnectionState is DriveConnectionState.Connected,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Backup, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(t("backup_drive_now"), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = startPhoneBackup,
                enabled = !state.isBusy && userId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Backup, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(t("backup_save_to_phone"), fontWeight = FontWeight.Bold)
            }

            state.latestBackup?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { backupViewModel.requestShareLatestBackup(preferTelegram = false) },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(t("backup_share"))
                    }
                    OutlinedButton(
                        onClick = { backupViewModel.requestShareLatestBackup(preferTelegram = true) },
                        enabled = !state.isBusy,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.padding(4.dp))
                        Text(t("backup_telegram"))
                    }
                }
            }

            if (state.history.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(t("backup_history"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        state.history.take(10).forEach { record ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    backupCompletedAt(record)?.let { completedAt ->
                                        Text(formatBackupTime(completedAt, language.name), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        Localization.getString("backup_status_${record.status.lowercase()}", language),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    record.restoredAt?.let { restoredAt ->
                                        Text(
                                            "${t("backup_last_restored")}: ${formatBackupTime(restoredAt, language.name)}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                val hasLocalArchive = record.localRelativePath.isNotBlank()
                                IconButton(onClick = { pendingLocalRestore = record }, enabled = !state.isBusy && hasLocalArchive) {
                                    Icon(Icons.Filled.Restore, contentDescription = t("backup_restore_confirm"))
                                }
                                IconButton(
                                    onClick = { backupViewModel.requestShareBackup(record, preferTelegram = false) },
                                    enabled = !state.isBusy && hasLocalArchive,
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = t("backup_share"))
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { startTrackedOperation { restoreLauncher.launch(arrayOf("application/octet-stream", "application/zip", "application/x-tijario-backup")) } },
                enabled = !state.isBusy && userId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Restore, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(t("backup_restore_file"), fontWeight = FontWeight.Bold)
            }

            if (state.isBusy) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    state.operationStage?.let { stage ->
                        Spacer(Modifier.padding(6.dp))
                        Text(
                            text = state.operationPercent?.let { "${t(stage)} $it%" } ?: t(stage),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            Text(
                text = t("backup_security_note"),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text(t("backup_restore_confirm_title"), fontWeight = FontWeight.Bold) },
            text = { Text(t("backup_restore_confirm_body")) },
            confirmButton = {
                Button(onClick = {
                    pendingRestoreUri = null
                    startTrackedOperation { backupViewModel.restoreFrom(uri) }
                }) { Text(t("backup_restore_confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) { Text(t("cancel")) }
            },
        )
    }

    pendingDriveRestore?.let { remote ->
        AlertDialog(
            onDismissRequest = { pendingDriveRestore = null },
            title = { Text(t("backup_restore_confirm_title"), fontWeight = FontWeight.Bold) },
            text = { Text(t("backup_restore_confirm_body")) },
            confirmButton = {
                Button(onClick = {
                    pendingDriveRestore = null
                    startTrackedOperation { backupViewModel.restoreFromDrive(remote) }
                }) { Text(t("backup_restore_confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDriveRestore = null }) { Text(t("cancel")) }
            },
        )
    }

    pendingLocalRestore?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingLocalRestore = null },
            title = { Text(t("backup_restore_confirm_title"), fontWeight = FontWeight.Bold) },
            text = { Text(t("backup_restore_confirm_body")) },
            confirmButton = {
                Button(onClick = {
                    pendingLocalRestore = null
                    startTrackedOperation { backupViewModel.restoreLocalRecord(record) }
                }) { Text(t("backup_restore_confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingLocalRestore = null }) { Text(t("cancel")) }
            },
        )
    }

    if (showNotificationRationale) {
        AlertDialog(
            onDismissRequest = {
                showNotificationRationale = false
            },
            title = { Text(t("backup_notifications_title"), fontWeight = FontWeight.Bold) },
            text = { Text(t("backup_notifications_body")) },
            confirmButton = {
                Button(onClick = {
                    showNotificationRationale = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        context.startActivity(backupNotifier.settingsIntent())
                    }
                }) { Text(t("backup_notifications_allow")) }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        val action = pendingNotificationAction
                        pendingNotificationAction = null
                        showNotificationRationale = false
                        action?.invoke()
                    }) { Text(t("backup_notifications_continue_without")) }
                    TextButton(onClick = {
                        showNotificationRationale = false
                        context.startActivity(backupNotifier.settingsIntent())
                    }) { Text(t("backup_notifications_settings")) }
                }
            },
        )
    }
    if (showLegacyFolderChoice) {
        AlertDialog(
            onDismissRequest = { showLegacyFolderChoice = false },
            title = { Text(t("backup_phone_folder"), fontWeight = FontWeight.Bold) },
            text = { Text(t("backup_phone_folder_required")) },
            confirmButton = {
                Button(onClick = {
                    showLegacyFolderChoice = false
                    folderLauncher.launch(null)
                }) { Text(t("backup_phone_folder")) }
            },
            dismissButton = {
                TextButton(onClick = { showLegacyFolderChoice = false }) { Text(t("cancel")) }
            },
        )
    }
}

private fun formatBackupTime(epochMillis: Long, languageName: String): String {
    val locale = if (languageName == "AR") Locale("ar") else Locale.ENGLISH
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale).format(Date(epochMillis))
}

private fun formatBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> "%.1f MB".format(Locale.ENGLISH, bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f KB".format(Locale.ENGLISH, bytes / 1024.0)
    else -> "$bytes B"
}
