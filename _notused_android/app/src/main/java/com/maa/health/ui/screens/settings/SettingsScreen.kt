package com.maa.health.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Settings Screen
 *
 * Features:
 * - Profile management
 * - Language selection
 * - Notification settings
 * - Privacy & security (biometrics, PIN)
 * - Data management (export, delete)
 * - App info
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onLanguageSettings: () -> Unit,
    onNotificationSettings: () -> Unit,
    onPrivacySettings: () -> Unit,
    onDataExport: () -> Unit,
    onAbout: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
                onLogout()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaaTypography.titleLarge,
                    color = MaaColors.textPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaaColors.textPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaaSpacing.medium)
        ) {
            // Profile section
            ProfileCard(
                userName = uiState.userName,
                userPhone = uiState.userPhone,
                onEditProfile = onEditProfile
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Account settings
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Edit Profile",
                    subtitle = "Name, age, location",
                    onClick = onEditProfile
                )
                SettingsItem(
                    icon = Icons.Default.Face,
                    title = "Lifecycle Stages",
                    subtitle = uiState.lifecycleStages.joinToString(", "),
                    onClick = { /* Navigate to stage selection */ }
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // App settings
            SettingsSection(title = "App Settings") {
                SettingsItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Language",
                    subtitle = uiState.currentLanguage,
                    onClick = onLanguageSettings
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = if (uiState.notificationsEnabled) "On" else "Off",
                    onClick = onNotificationSettings
                )
                SettingsToggleItem(
                    icon = Icons.Default.Star,
                    title = "Offline Mode",
                    subtitle = "Download content for offline use",
                    isEnabled = uiState.offlineModeEnabled,
                    onToggle = { viewModel.toggleOfflineMode() }
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Privacy & Security
            SettingsSection(title = "Privacy & Security") {
                SettingsToggleItem(
                    icon = Icons.Default.Lock,
                    title = "Biometric Lock",
                    subtitle = "Use fingerprint or face to unlock",
                    isEnabled = uiState.biometricEnabled,
                    onToggle = { viewModel.toggleBiometric() }
                )
                SettingsToggleItem(
                    icon = Icons.Default.Lock,
                    title = "PIN Lock",
                    subtitle = "Set a 4-digit PIN",
                    isEnabled = uiState.pinEnabled,
                    onToggle = { viewModel.togglePin() }
                )
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Data Sharing",
                    subtitle = "Control what data is shared",
                    onClick = onPrivacySettings
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Data management
            SettingsSection(title = "Data") {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Export My Data",
                    subtitle = "Download your health records",
                    onClick = onDataExport
                )
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Account",
                    subtitle = "Permanently delete all data",
                    onClick = { showDeleteDialog = true },
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // About
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Maa",
                    subtitle = "Version ${uiState.appVersion}",
                    onClick = onAbout
                )
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate the App",
                    subtitle = "Help us improve",
                    onClick = { viewModel.rateApp() }
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Logout button
            MaaButton(
                text = "Log Out",
                onClick = { showLogoutDialog = true },
                style = MaaButtonStyle.OUTLINE,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun ProfileCard(
    userName: String?,
    userPhone: String?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditProfile),
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName?.firstOrNull()?.uppercase() ?: "👤",
                    style = MaaTypography.headlineSmall,
                    color = MaaColors.accent
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName ?: "Set up your profile",
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )
                if (userPhone != null) {
                    Text(
                        text = userPhone,
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaaColors.textTertiary
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaaTypography.labelMedium,
            color = MaaColors.textTertiary,
            modifier = Modifier.padding(bottom = MaaSpacing.small)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
            shape = MaaShapes.medium
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(MaaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaaColors.error else MaaColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaaTypography.bodyLarge,
                color = if (isDestructive) MaaColors.error else MaaColors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textTertiary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaaColors.textTertiary
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(MaaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaaColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaaTypography.bodyLarge,
                color = MaaColors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textTertiary
                )
            }
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaaColors.accent,
                uncheckedThumbColor = MaaColors.textTertiary,
                uncheckedTrackColor = MaaColors.surfaceVariant
            )
        )
    }
}

@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Out?",
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary
            )
        },
        text = {
            Text(
                text = "Your data is stored locally and will remain on this device. You can log back in anytime.",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Log Out",
                    color = MaaColors.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaaColors.textSecondary
                )
            }
        },
        containerColor = MaaColors.surface,
        shape = MaaShapes.medium
    )
}

@Composable
private fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Account?",
                style = MaaTypography.titleMedium,
                color = MaaColors.error
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your health data, including pregnancy records, cycle history, and symptom logs. This action cannot be undone.",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete Everything",
                    color = MaaColors.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = MaaColors.textSecondary
                )
            }
        },
        containerColor = MaaColors.surface,
        shape = MaaShapes.medium
    )
}
