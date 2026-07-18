package app.tijario.ui.screens

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FileUpload
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tijario.config.LocalLanguage
import app.tijario.config.Localization
import app.tijario.config.t
import app.tijario.features.backup.BackupViewModel
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

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"),
        backupViewModel::exportPreparedBackup,
    )
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingRestoreUri = uri
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
                                enabled = !state.isBusy,
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
                }
            }

            Button(
                onClick = { backupViewModel.createLocalBackup(exportAfterCreate = false) },
                enabled = !state.isBusy && userId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Backup, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(t("backup_now"), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { backupViewModel.createLocalBackup(exportAfterCreate = true) },
                enabled = !state.isBusy && userId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(t("backup_export_to"), fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { restoreLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) },
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
                    backupViewModel.restoreFrom(uri)
                }) { Text(t("backup_restore_confirm")) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestoreUri = null }) { Text(t("cancel")) }
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
