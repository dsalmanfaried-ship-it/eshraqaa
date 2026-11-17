package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CatalogSeed
import com.example.notifications.DailyReminderNotificationHelper
import com.example.notifications.SpiritualReminders
import com.example.ui.IlmUiState
import com.example.ui.MoreSubPage
import com.example.ui.theme.LocalIlmColors

@Composable
fun MoreScreen(
    uiState: IlmUiState,
    onOpenSubPage: (MoreSubPage?) -> Unit,
    onAddVideo: (scholar: String, section: String, title: String, url: String) -> Unit,
    onImportYouTube: (scholar: String, section: String, lines: String) -> Unit,
    onSyncCatalog: () -> Unit,
    onToggleTheme: () -> Unit,
    onClearProgress: () -> Unit,
    onBuildExportJson: () -> String,
    onShareText: (String) -> Unit,
    onFeedback: (String) -> Unit,
    onEditUserName: () -> Unit,
    onSetReminderEnabled: (Boolean) -> Unit,
    onSetReminderTime: (hour: Int, minute: Int) -> Unit,
    onSendTestReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState.moreSubPage) {
        MoreSubPage.ADD_VIDEO -> {
            BackHandler { onOpenSubPage(null) }
            AddVideoSubPage(
                scholars = uiState.scholars.map { it.name }.ifEmpty { CatalogSeed.defaultScholars },
                onBack = { onOpenSubPage(null) },
                onSave = onAddVideo,
                modifier = modifier
            )
        }

        MoreSubPage.IMPORT_YOUTUBE -> {
            BackHandler { onOpenSubPage(null) }
            ImportYouTubeSubPage(
                scholars = uiState.scholars.map { it.name }.ifEmpty { CatalogSeed.defaultScholars },
                isImporting = uiState.isImporting,
                statusText = uiState.importStatus,
                onBack = { onOpenSubPage(null) },
                onImport = onImportYouTube,
                modifier = modifier
            )
        }

        MoreSubPage.EXPORT_DATA -> {
            BackHandler { onOpenSubPage(null) }
            ExportDataSubPage(
                exportText = remember(uiState.allVideos) { onBuildExportJson() },
                onBack = { onOpenSubPage(null) },
                onShareText = onShareText,
                onCopied = { onFeedback("اتنسخ النص ✓") },
                modifier = modifier
            )
        }

        null -> {
            MoreMenuHome(
                uiState = uiState,
                onOpenSubPage = onOpenSubPage,
                onSyncCatalog = onSyncCatalog,
                onToggleTheme = onToggleTheme,
                onClearProgress = onClearProgress,
                onEditUserName = onEditUserName,
                onSetReminderEnabled = onSetReminderEnabled,
                onSetReminderTime = onSetReminderTime,
                onSendTestReminder = onSendTestReminder,
                onFeedback = onFeedback,
                modifier = modifier
            )
        }
    }
}

private enum class PendingNotificationAction {
    ENABLE_REMINDER,
    PICK_TIME,
    SEND_TEST
}

@Composable
private fun MoreMenuHome(
    uiState: IlmUiState,
    onOpenSubPage: (MoreSubPage) -> Unit,
    onSyncCatalog: () -> Unit,
    onToggleTheme: () -> Unit,
    onClearProgress: () -> Unit,
    onEditUserName: () -> Unit,
    onSetReminderEnabled: (Boolean) -> Unit,
    onSetReminderTime: (hour: Int, minute: Int) -> Unit,
    onSendTestReminder: () -> Unit,
    onFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<PendingNotificationAction?>(null) }
    val deniedMessage = stringResource(R.string.reminder_permission_denied)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted) {
            when (action) {
                PendingNotificationAction.ENABLE_REMINDER -> onSetReminderEnabled(true)
                PendingNotificationAction.PICK_TIME -> showTimePickerDialog = true
                PendingNotificationAction.SEND_TEST -> onSendTestReminder()
                null -> Unit
            }
        } else {
            onFeedback(deniedMessage)
        }
    }

    fun runWithNotificationPermission(action: PendingNotificationAction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !DailyReminderNotificationHelper.hasNotificationPermission(context)
        ) {
            pendingAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            when (action) {
                PendingNotificationAction.ENABLE_REMINDER -> onSetReminderEnabled(true)
                PendingNotificationAction.PICK_TIME -> showTimePickerDialog = true
                PendingNotificationAction.SEND_TEST -> onSendTestReminder()
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    value = "${uiState.allVideos.size}",
                    label = stringResource(R.string.stat_videos),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${uiState.watchedCount}",
                    label = stringResource(R.string.stat_watched),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    value = "${uiState.savedCount}",
                    label = stringResource(R.string.stat_saved),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            MoreActionRow(
                icon = Icons.Rounded.Person,
                title = stringResource(R.string.user_name_setting),
                trailing = {
                    Text(
                        text = uiState.userName.ifBlank { stringResource(R.string.user_name_add) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = ilmColors.gold
                    )
                },
                onClick = onEditUserName,
                modifier = Modifier.testTag("more_username_btn")
            )

            MoreActionRow(
                icon = Icons.Rounded.AddCircleOutline,
                title = stringResource(R.string.more_add_video),
                onClick = { onOpenSubPage(MoreSubPage.ADD_VIDEO) },
                modifier = Modifier.testTag("more_add_video_btn")
            )

            MoreActionRow(
                icon = Icons.Rounded.Download,
                title = stringResource(R.string.more_import_youtube),
                onClick = { onOpenSubPage(MoreSubPage.IMPORT_YOUTUBE) },
                modifier = Modifier.testTag("more_import_btn")
            )

            MoreActionRow(
                icon = Icons.Rounded.Sync,
                title = stringResource(R.string.more_sync_catalog),
                trailing = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ilmColors.gold
                        )
                    }
                },
                onClick = onSyncCatalog,
                modifier = Modifier.testTag("more_sync_btn")
            )

            MoreActionRow(
                icon = Icons.Rounded.Upload,
                title = stringResource(R.string.more_export_data),
                onClick = { onOpenSubPage(MoreSubPage.EXPORT_DATA) },
                modifier = Modifier.testTag("more_export_btn")
            )

            DailyReminderSettingsCard(
                enabled = uiState.reminderEnabled,
                hour = uiState.reminderHour,
                minute = uiState.reminderMinute,
                onToggle = { nextEnabled ->
                    if (nextEnabled) {
                        runWithNotificationPermission(PendingNotificationAction.ENABLE_REMINDER)
                    } else {
                        onSetReminderEnabled(false)
                    }
                },
                onPickTimeClick = {
                    runWithNotificationPermission(PendingNotificationAction.PICK_TIME)
                },
                onSendTestClick = {
                    runWithNotificationPermission(PendingNotificationAction.SEND_TEST)
                }
            )

            // Theme toggle row
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ilmColors.hairline),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onToggleTheme)
                    .testTag("more_theme_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (uiState.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        contentDescription = null,
                        tint = ilmColors.gold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.more_theme_toggle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ilmColors.onGold,
                            checkedTrackColor = ilmColors.gold
                        )
                    )
                }
            }

            // Clear progress row
            MoreActionRow(
                icon = Icons.Rounded.DeleteOutline,
                title = stringResource(R.string.more_clear_progress),
                tintColor = ilmColors.heart,
                onClick = { showClearDialog = true },
                modifier = Modifier.testTag("more_clear_btn")
            )
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(text = stringResource(R.string.clear_dialog_title)) },
            text = { Text(text = stringResource(R.string.clear_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearProgress()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        color = ilmColors.heart,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showTimePickerDialog) {
        ReminderTimePickerDialog(
            initialHour = uiState.reminderHour,
            initialMinute = uiState.reminderMinute,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { hour, minute ->
                showTimePickerDialog = false
                onSetReminderTime(hour, minute)
            }
        )
    }
}

@Composable
private fun DailyReminderSettingsCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onPickTimeClick: () -> Unit,
    onSendTestClick: () -> Unit
) {
    val ilmColors = LocalIlmColors.current
    val formattedTime = SpiritualReminders.formatTimeArabic(hour, minute)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) ilmColors.gold.copy(alpha = 0.55f) else ilmColors.hairline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .testTag("daily_reminder_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(!enabled) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (enabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    tint = if (enabled) ilmColors.gold else ilmColors.mutedText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_section_title),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (enabled) {
                            stringResource(R.string.reminder_status_active, formattedTime)
                        } else {
                            stringResource(R.string.reminder_status_inactive)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) ilmColors.gold else ilmColors.mutedText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ilmColors.onGold,
                        checkedTrackColor = ilmColors.gold
                    ),
                    modifier = Modifier.testTag("reminder_toggle_switch")
                )
            }

            HorizontalDivider(color = ilmColors.hairline)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPickTimeClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("reminder_time_btn"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    tint = ilmColors.gold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.reminder_time_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ilmColors.gold.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, ilmColors.gold.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = ilmColors.gold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(visible = enabled) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = ilmColors.hairline)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onSendTestClick,
                            modifier = Modifier.testTag("reminder_test_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.reminder_test_btn),
                                style = MaterialTheme.typography.labelLarge,
                                color = ilmColors.gold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val ilmColors = LocalIlmColors.current
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reminder_time_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = ilmColors.gold
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = ilmColors.cardElevated,
                            selectorColor = ilmColors.gold,
                            clockDialSelectedContentColor = ilmColors.onGold,
                            timeSelectorSelectedContainerColor = ilmColors.gold.copy(alpha = 0.22f),
                            timeSelectorSelectedContentColor = ilmColors.gold,
                            periodSelectorSelectedContainerColor = ilmColors.gold.copy(alpha = 0.22f),
                            periodSelectorSelectedContentColor = ilmColors.gold
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ilmColors.gold,
                    contentColor = ilmColors.onGold
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reminder_time_confirm_btn")
            ) {
                Text(stringResource(R.string.save_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ilmColors.hairline),
        color = Color.Transparent,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(ilmColors.cardElevated, MaterialTheme.colorScheme.surface)
                    )
                )
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ilmColors.gold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = ilmColors.mutedText
            )
        }
    }
}

@Composable
private fun MoreActionRow(
    icon: ImageVector,
    title: String,
    tintColor: Color? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val resolvedTint = tintColor ?: MaterialTheme.colorScheme.onBackground

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, ilmColors.hairline),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor ?: ilmColors.gold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = resolvedTint,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
private fun SubPageHeader(
    title: String,
    onBack: () -> Unit
) {
    val ilmColors = LocalIlmColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("subpage_back_btn")) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back_cd),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = ilmColors.gold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScholarDropdown(
    scholars: List<String>,
    selectedScholar: String,
    onSelect: (String) -> Unit
) {
    val ilmColors = LocalIlmColors.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedScholar,
            onValueChange = {},
            readOnly = true,
            label = { Text("الشيخ") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = ilmColors.gold,
                unfocusedBorderColor = ilmColors.hairline
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            scholars.forEach { scholar ->
                DropdownMenuItem(
                    text = { Text(scholar) },
                    onClick = {
                        onSelect(scholar)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddVideoSubPage(
    scholars: List<String>,
    onBack: () -> Unit,
    onSave: (scholar: String, section: String, title: String, url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    var scholar by remember { mutableStateOf(scholars.firstOrNull().orEmpty()) }
    var section by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SubPageHeader(title = stringResource(R.string.more_add_video), onBack = onBack)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScholarDropdown(
                    scholars = scholars,
                    selectedScholar = scholar,
                    onSelect = { scholar = it }
                )

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("القسم") },
                    placeholder = { Text("مثلاً: تزكية") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("YouTube URL") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        if (section.isNotBlank() && title.isNotBlank() && url.isNotBlank()) {
                            onSave(scholar, section, title, url)
                        }
                    },
                    enabled = section.isNotBlank() && title.isNotBlank() && url.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ilmColors.gold,
                        contentColor = ilmColors.onGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("حفظ", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun ImportYouTubeSubPage(
    scholars: List<String>,
    isImporting: Boolean,
    statusText: String,
    onBack: () -> Unit,
    onImport: (scholar: String, section: String, lines: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    var scholar by remember { mutableStateOf(scholars.firstOrNull().orEmpty()) }
    var section by remember { mutableStateOf("") }
    var lines by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SubPageHeader(title = "استيراد من يوتيوب", onBack = onBack)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScholarDropdown(
                    scholars = scholars,
                    selectedScholar = scholar,
                    onSelect = { scholar = it }
                )

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("اسم القسم") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "رابط قائمة تشغيل، أو روابط فيديوهات (كل رابط في سطر — أو: العنوان | الرابط)",
                    style = MaterialTheme.typography.bodySmall,
                    color = ilmColors.mutedText
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = lines,
                        onValueChange = { lines = it },
                        minLines = 5,
                        maxLines = 8,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { onImport(scholar, section, lines) },
                    enabled = !isImporting && section.isNotBlank() && lines.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ilmColors.gold,
                        contentColor = ilmColors.onGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ilmColors.onGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text("استيراد", style = MaterialTheme.typography.titleSmall)
                }

                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ilmColors.mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun ExportDataSubPage(
    exportText: String,
    onBack: () -> Unit,
    onShareText: (String) -> Unit,
    onCopied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val clipboardManager = LocalClipboardManager.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SubPageHeader(title = "مشاركة المحتوى", onBack = onBack)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "انسخ النص والصقه مكان محتوى data.js على GitHub ليظهر نفس المحتوى للجميع.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ilmColors.mutedText
                )

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = exportText,
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        minLines = 10,
                        maxLines = 12,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportText))
                            onCopied()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ilmColors.gold,
                            contentColor = ilmColors.onGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ")
                    }

                    OutlinedButton(
                        onClick = { onShareText(exportText) },
                        border = BorderStroke(1.dp, ilmColors.gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = null,
                            tint = ilmColors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة", color = ilmColors.gold)
                    }
                }
            }
        }
    }
}
