package app.tijario.features.notifications

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tijario.config.AppLanguage
import app.tijario.config.AppPreferences
import app.tijario.config.LocalLanguage
import app.tijario.config.t

@Composable
fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge {
                        Text(if (unreadCount > 99) "99+" else unreadCount.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = t("notifications"),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    initialAnnouncementId: String?,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalLanguage.current
    var selected by remember { mutableStateOf<Announcement?>(null) }

    LaunchedEffect(initialAnnouncementId, state.items) {
        if (!initialAnnouncementId.isNullOrBlank()) {
            state.items.firstOrNull { it.id == initialAnnouncementId }?.let {
                selected = it
                viewModel.markRead(it.id, "push")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(t("notifications"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("btn_back"))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.markAllRead() }) {
                        Icon(Icons.Filled.DoneAll, contentDescription = t("mark_all_read"))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(t("notifications_empty"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedButton(onClick = { state.userId?.let(viewModel::refresh) }) {
                            Text(t("retry"))
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (state.isOffline) {
                            item {
                                Text(
                                    text = t("notifications_offline_cache"),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        items(state.items, key = { it.id }) { item ->
                            AnnouncementCard(
                                announcement = item,
                                language = language,
                                onClick = {
                                    selected = item
                                    viewModel.markRead(item.id, "inbox")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    selected?.let { announcement ->
        AnnouncementDetailDialog(
            announcement = announcement,
            language = language,
            onClose = { selected = null },
        )
    }
}

@Composable
fun StartupAnnouncementDialog(
    announcement: Announcement,
    language: AppLanguage,
    onViewDetails: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.NotificationsActive, contentDescription = null) },
        title = { Text(announcement.title(language), fontWeight = FontWeight.Bold) },
        text = { Text(announcement.body(language)) },
        confirmButton = {
            Button(onClick = onViewDetails) {
                Text(t("view_details"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(t("close"))
            }
        },
    )
}

@Composable
fun NotificationPermissionPrompt(
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        AppPreferences.setPushEnabled(context, it)
        onFinished()
    }

    AlertDialog(
        onDismissRequest = onFinished,
        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
        title = { Text(t("notification_permission_title"), fontWeight = FontWeight.Bold) },
        text = { Text(t("notification_permission_body")) },
        confirmButton = {
            Button(
                onClick = {
                    AppPreferences.setNotificationExplained(context)
                    if (Build.VERSION.SDK_INT >= 33) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        AppPreferences.setPushEnabled(context, true)
                        onFinished()
                    }
                }
            ) {
                Text(t("enable_notifications"))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    AppPreferences.setNotificationExplained(context)
                    onFinished()
                }
            ) {
                Text(t("later"))
            }
        },
    )
}

@Composable
fun NotificationSettingsSection() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(AppPreferences.isPushEnabled(context)) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(t("notifications"), fontWeight = FontWeight.Bold)
                    Text(
                        t("notification_settings_desc"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        AppPreferences.setPushEnabled(context, it)
                    },
                )
            }
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(t("open_system_settings"))
            }
        }
    }
}

@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    language: AppLanguage,
    onClick: () -> Unit,
) {
    val background = if (announcement.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = background),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                modifier = Modifier.size(42.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    announcement.title(language),
                    fontWeight = if (announcement.isRead) FontWeight.SemiBold else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    announcement.body(language),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                announcement.publishedAt?.take(10)?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementDetailDialog(
    announcement: Announcement,
    language: AppLanguage,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val action = announcement.actionLabel(language)
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(announcement.title(language), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(announcement.body(language))
                announcement.publishedAt?.take(10)?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            if (!announcement.deepLink.isNullOrBlank() && !action.isNullOrBlank()) {
                Button(
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(announcement.deepLink))) }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(action)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(t("close"))
            }
        },
    )
}
