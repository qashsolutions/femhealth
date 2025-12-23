package com.maa.health.ui.screens.you

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Workspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.ui.components.FloatingVoiceFAB
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * You Hub Screen - Profile, Settings, and Data Management
 *
 * Features:
 * - Profile management
 * - Language & voice settings
 * - Push notification preferences
 * - Health monitoring settings
 * - Data export (JSON/PDF)
 * - Cloud sync (premium)
 * - Account deletion
 * - Privacy & security
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouHubScreen(
    userName: String = "User",
    userPhone: String = "+91 98xxx xxxxx",
    isPremium: Boolean = false,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToVoiceSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHealthMonitoring: () -> Unit,
    onExportData: () -> Unit,
    onCloudSync: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onLogout: () -> Unit,
    voiceService: VoiceService? = null,
    onVoiceSearch: ((String) -> Unit)? = null
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var cloudSyncEnabled by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        TopAppBar(
            title = {
                Text(
                    text = "You",
                    style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaaColors.textPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
        ) {
            // Profile card
            item {
                ProfileCard(
                    userName = userName,
                    userPhone = userPhone,
                    isPremium = isPremium,
                    onEdit = onNavigateToEditProfile,
                    onUpgrade = onNavigateToPremium
                )
            }

            // Premium banner (if not premium)
            if (!isPremium) {
                item {
                    PremiumBanner(onClick = onNavigateToPremium)
                }
            }

            // Language & Voice section
            item {
                SettingsSection(title = "Language & Voice") {
                    SettingsItem(
                        icon = Icons.Default.Language,
                        iconColor = MaaColors.info,
                        title = "Language",
                        subtitle = "हिन्दी (Hindi)",
                        onClick = onNavigateToLanguage
                    )
                    SettingsItem(
                        icon = Icons.Default.RecordVoiceOver,
                        iconColor = MaaColors.accent,
                        title = "Voice Settings",
                        subtitle = "Voice type, speed & pitch",
                        onClick = onNavigateToVoiceSettings
                    )
                }
            }

            // Notifications section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleItem(
                        icon = Icons.Default.Notifications,
                        iconColor = MaaColors.warning,
                        title = "Push Notifications",
                        subtitle = "Reminders & health alerts",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    SettingsItem(
                        icon = Icons.Default.MonitorHeart,
                        iconColor = MaaColors.error,
                        title = "Health Monitoring",
                        subtitle = "Common issues to track",
                        onClick = onNavigateToHealthMonitoring
                    )
                }
            }

            // Data Management section
            item {
                SettingsSection(title = "Data Management") {
                    SettingsItem(
                        icon = Icons.Default.Download,
                        iconColor = MaaColors.success,
                        title = "Export Data",
                        subtitle = "Download as JSON or PDF",
                        onClick = onExportData
                    )
                    SettingsToggleItem(
                        icon = Icons.Default.CloudSync,
                        iconColor = MaaColors.info,
                        title = "Cloud Sync",
                        subtitle = if (isPremium) "Auto-backup enabled" else "Premium feature",
                        checked = cloudSyncEnabled && isPremium,
                        onCheckedChange = {
                            if (isPremium) {
                                cloudSyncEnabled = it
                            } else {
                                onNavigateToPremium()
                            }
                        },
                        enabled = isPremium,
                        showPremiumBadge = !isPremium
                    )
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        iconColor = MaaColors.textTertiary,
                        title = "Local Storage",
                        subtitle = "12.4 MB used",
                        onClick = {}
                    )
                }
            }

            // Security section
            item {
                SettingsSection(title = "Security") {
                    SettingsToggleItem(
                        icon = Icons.Default.Fingerprint,
                        iconColor = MaaColors.accent,
                        title = "Biometric Lock",
                        subtitle = "Use fingerprint to unlock",
                        checked = biometricEnabled,
                        onCheckedChange = { biometricEnabled = it }
                    )
                    SettingsItem(
                        icon = Icons.Default.Security,
                        iconColor = MaaColors.info,
                        title = "Privacy Settings",
                        subtitle = "Data sharing & permissions",
                        onClick = onNavigateToPrivacy
                    )
                }
            }

            // Support section
            item {
                SettingsSection(title = "Support") {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconColor = MaaColors.info,
                        title = "Help & FAQ",
                        subtitle = "Get help with the app",
                        onClick = onNavigateToHelp
                    )
                    SettingsItem(
                        icon = Icons.Default.Share,
                        iconColor = MaaColors.success,
                        title = "Share App",
                        subtitle = "Invite friends & family",
                        onClick = {}
                    )
                }
            }

            // Danger zone
            item {
                SettingsSection(
                    title = "Account",
                    titleColor = MaaColors.error
                ) {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconColor = MaaColors.warning,
                        title = "Log Out",
                        subtitle = "Sign out of your account",
                        onClick = onLogout
                    )
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        iconColor = MaaColors.error,
                        title = "Delete Account",
                        subtitle = "Permanently delete all data",
                        onClick = onDeleteAccount,
                        isDanger = true
                    )
                }
            }

            // Version info
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaaSpacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Maa - Women's Health Companion",
                        style = MaaTypography.labelMedium,
                        color = MaaColors.textTertiary
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textTertiary
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }

        // Voice FAB for search
        voiceService?.let { service ->
            FloatingVoiceFAB(
                voiceService = service,
                voiceAction = VoiceAction.SEARCH,
                onTranscriptionConfirmed = { text ->
                    onVoiceSearch?.invoke(text)
                },
                onEditRequested = { text ->
                    onVoiceSearch?.invoke(text)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaaSpacing.large)
            )
        }
    }
}

@Composable
private fun ProfileCard(
    userName: String,
    userPhone: String,
    isPremium: Boolean,
    onEdit: () -> Unit,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaaColors.accent,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        style = MaaTypography.titleMedium,
                        color = MaaColors.textPrimary
                    )
                    if (isPremium) {
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Premium",
                            tint = MaaColors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = userPhone,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
                if (isPremium) {
                    Text(
                        text = "Premium Member",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.accent
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile",
                    tint = MaaColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PremiumBanner(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.accent.copy(alpha = 0.1f)
        ),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaaColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Upgrade to Premium",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "Cloud sync, AI insights & more",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = MaaColors.accent
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    titleColor: Color = MaaColors.textPrimary,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaaTypography.titleSmall,
            color = titleColor,
            modifier = Modifier.padding(bottom = MaaSpacing.small)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaaSpacing.small)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isDanger) {
                MaaColors.error.copy(alpha = 0.05f)
            } else {
                Color.Transparent
            }
        ),
        shape = MaaShapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaaTypography.labelLarge,
                    color = if (isDanger) MaaColors.error else MaaColors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    showPremiumBadge: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaaTypography.labelLarge,
                    color = MaaColors.textPrimary
                )
                if (showPremiumBadge) {
                    Spacer(modifier = Modifier.width(MaaSpacing.small))
                    Box(
                        modifier = Modifier
                            .clip(MaaShapes.small)
                            .background(MaaColors.accent.copy(alpha = 0.1f))
                            .padding(horizontal = MaaSpacing.small, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Premium",
                            style = MaaTypography.labelSmall,
                            color = MaaColors.accent
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaaColors.surface,
                checkedTrackColor = MaaColors.accent,
                uncheckedThumbColor = MaaColors.surface,
                uncheckedTrackColor = MaaColors.textTertiary
            )
        )
    }
}
