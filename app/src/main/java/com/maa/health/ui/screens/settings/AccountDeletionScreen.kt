package com.maa.health.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.data.repository.AccountDeletionManager
import com.maa.health.data.repository.DataDeletionSummary
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonSize
import com.maa.health.ui.components.MaaButtonVariant
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography
import kotlinx.coroutines.launch

/**
 * Account Deletion Screen
 *
 * Provides a comprehensive account deletion flow as required by Google Play Store.
 * Shows what data will be deleted and requires explicit confirmation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDeletionScreen(
    accountDeletionManager: AccountDeletionManager,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var dataSummary by remember { mutableStateOf<DataDeletionSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    var confirmChecked by remember { mutableStateOf(false) }
    var understandChecked by remember { mutableStateOf(false) }
    var showFinalConfirmation by remember { mutableStateOf(false) }
    var deletionError by remember { mutableStateOf<String?>(null) }

    // Load data summary
    LaunchedEffect(Unit) {
        dataSummary = accountDeletionManager.getDataDeletionSummary()
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Delete Account",
                    style = MaaTypography.titleLarge,
                    color = MaaColors.error
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
                .padding(MaaSpacing.large)
        ) {
            // Warning icon
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally),
                tint = MaaColors.error
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Title
            Text(
                text = "Delete Your Account?",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "This action is permanent and cannot be undone.",
                style = MaaTypography.bodyMedium,
                color = MaaColors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Data that will be deleted
            Text(
                text = "The following data will be permanently deleted:",
                style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaaShapes.medium)
                    .background(MaaColors.error.copy(alpha = 0.1f))
                    .border(1.dp, MaaColors.error.copy(alpha = 0.3f), MaaShapes.medium)
                    .padding(MaaSpacing.medium)
            ) {
                DeletionItem("Your profile information (name, phone, age)")
                DeletionItem("All health records and symptom logs")
                DeletionItem("Cycle tracking history")
                DeletionItem("Pregnancy and postpartum data")
                DeletionItem("Children's profiles and vaccination records")
                DeletionItem("Mental health logs and screening results")
                DeletionItem("All saved preferences and settings")
                DeletionItem("Cached data and downloaded content")

                if (dataSummary != null) {
                    Spacer(modifier = Modifier.height(MaaSpacing.medium))
                    Text(
                        text = "Total: ${dataSummary!!.totalRecordsCount} records, ${dataSummary!!.cachedDataSizeMB}",
                        style = MaaTypography.labelMedium,
                        color = MaaColors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Data retention notice
            Text(
                text = "Data retained for legal compliance:",
                style = MaaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "• Deletion request logs (30 days, for fraud prevention)\n• Anonymous, aggregated analytics (non-identifiable)",
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Confirmation checkboxes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaaShapes.small)
                    .background(MaaColors.surfaceVariant)
                    .padding(MaaSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmChecked,
                    onCheckedChange = { confirmChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaaColors.error,
                        uncheckedColor = MaaColors.textSecondary
                    )
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "I understand that all my data will be permanently deleted",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaaShapes.small)
                    .background(MaaColors.surfaceVariant)
                    .padding(MaaSpacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = understandChecked,
                    onCheckedChange = { understandChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaaColors.error,
                        uncheckedColor = MaaColors.textSecondary
                    )
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "I understand this action cannot be undone",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Error message
            deletionError?.let { error ->
                Text(
                    text = error,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
            }

            // Delete button
            MaaButton(
                text = "Delete My Account",
                onClick = { showFinalConfirmation = true },
                enabled = confirmChecked && understandChecked && !isDeleting,
                loading = isDeleting,
                leadingIcon = Icons.Default.DeleteForever,
                variant = MaaButtonVariant.DANGER,
                size = MaaButtonSize.LARGE,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Web deletion option (required by Google Play)
            MaaButton(
                text = "Delete via Web",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AccountDeletionManager.ACCOUNT_DELETION_WEB_URL))
                    context.startActivity(intent)
                },
                leadingIcon = Icons.Default.OpenInNew,
                variant = MaaButtonVariant.SECONDARY,
                size = MaaButtonSize.MEDIUM,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "You can also delete your account at:\n${AccountDeletionManager.ACCOUNT_DELETION_WEB_URL}",
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Cancel button
            MaaButton(
                text = "Cancel",
                onClick = onBack,
                variant = MaaButtonVariant.TERTIARY,
                fullWidth = true
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }

    // Final confirmation dialog
    if (showFinalConfirmation) {
        AlertDialog(
            onDismissRequest = { showFinalConfirmation = false },
            containerColor = MaaColors.surface,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaaColors.error,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Final Confirmation",
                    style = MaaTypography.headlineSmall,
                    color = MaaColors.error
                )
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to delete your account and all associated data?\n\nThis cannot be undone.",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.textPrimary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinalConfirmation = false
                        isDeleting = true
                        scope.launch {
                            val result = accountDeletionManager.deleteAllUserData()
                            result.fold(
                                onSuccess = {
                                    onAccountDeleted()
                                },
                                onFailure = { error ->
                                    isDeleting = false
                                    deletionError = "Failed to delete account: ${error.message}"
                                }
                            )
                        }
                    }
                ) {
                    Text(
                        text = "DELETE EVERYTHING",
                        color = MaaColors.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalConfirmation = false }) {
                    Text(
                        text = "Cancel",
                        color = MaaColors.textSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun DeletionItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = MaaSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaaColors.error
        )
        Spacer(modifier = Modifier.width(MaaSpacing.small))
        Text(
            text = text,
            style = MaaTypography.bodySmall,
            color = MaaColors.textPrimary
        )
    }
}
