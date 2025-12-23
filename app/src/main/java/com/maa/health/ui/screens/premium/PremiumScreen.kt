package com.maa.health.ui.screens.premium

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Support
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maa.health.data.remote.payment.PaymentState
import com.maa.health.data.remote.payment.SubscriptionPlan
import com.maa.health.data.remote.payment.UpiApp
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Premium Subscription Screen with UPI Payment
 *
 * Features:
 * - Subscription plan selection
 * - UPI app selection (GPay, PhonePe, Paytm, BHIM)
 * - Payment processing
 * - Success/failure handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val scrollState = rememberScrollState()

    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaaColors.accent.copy(alpha = 0.1f),
                        MaaColors.background
                    )
                )
            )
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Upgrade to Premium",
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
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = MaaSpacing.medium)
        ) {
            // Premium badge
            PremiumHeader()

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Features list
            PremiumFeatures()

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Subscription plans
            Text(
                text = "Choose Your Plan",
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            uiState.subscriptionPlans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    isSelected = selectedPlan?.id == plan.id,
                    onClick = { selectedPlan = plan }
                )
                Spacer(modifier = Modifier.height(MaaSpacing.small))
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))

            // Payment section
            if (selectedPlan != null) {
                PaymentSection(
                    selectedPlan = selectedPlan!!,
                    installedUpiApps = uiState.installedUpiApps,
                    paymentState = paymentState,
                    onPayWithUpi = { app ->
                        viewModel.initiatePayment(selectedPlan!!, app)
                    }
                )
            }

            // Payment state messages
            when (paymentState) {
                is PaymentState.Success -> {
                    Spacer(modifier = Modifier.height(MaaSpacing.medium))
                    PaymentSuccessCard(onContinue = onPaymentSuccess)
                }
                is PaymentState.Failed -> {
                    Spacer(modifier = Modifier.height(MaaSpacing.medium))
                    PaymentFailedCard(
                        message = (paymentState as PaymentState.Failed).message,
                        onRetry = { selectedPlan?.let { viewModel.resetPayment() } }
                    )
                }
                is PaymentState.Pending -> {
                    Spacer(modifier = Modifier.height(MaaSpacing.medium))
                    PaymentPendingCard()
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(MaaSpacing.extraLarge))

            // Terms
            Text(
                text = "By subscribing, you agree to our Terms of Service and Privacy Policy. Subscriptions auto-renew unless cancelled.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun PremiumHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.accent
        ),
        shape = MaaShapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Star badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Text(
                text = "माँ Premium",
                style = MaaTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            Text(
                text = "Unlock all features for complete health care",
                style = MaaTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PremiumFeatures() {
    val features = listOf(
        PremiumFeature(
            icon = Icons.Default.Cloud,
            title = "Cloud Sync",
            titleHindi = "क्लाउड सिंक",
            description = "Backup your data securely"
        ),
        PremiumFeature(
            icon = Icons.Default.Psychology,
            title = "AI Insights",
            titleHindi = "AI अंतर्दृष्टि",
            description = "Personalized health analysis"
        ),
        PremiumFeature(
            icon = Icons.Default.RecordVoiceOver,
            title = "Unlimited Voice",
            titleHindi = "असीमित वॉयस",
            description = "No daily voice query limits"
        ),
        PremiumFeature(
            icon = Icons.Default.Support,
            title = "Priority Support",
            titleHindi = "प्राथमिकता सहायता",
            description = "24/7 dedicated support"
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            features.forEach { feature ->
                FeatureRow(feature = feature)
                if (feature != features.last()) {
                    Spacer(modifier = Modifier.height(MaaSpacing.medium))
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(feature: PremiumFeature) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaaColors.accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = MaaColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                style = MaaTypography.labelLarge,
                color = MaaColors.textPrimary
            )
            Text(
                text = feature.description,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )
        }

        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaaColors.safe,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaaColors.accent else MaaColors.border,
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = MaaShapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaaColors.accent.copy(alpha = 0.1f)
            } else {
                MaaColors.surface
            }
        ),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaaColors.accent else Color.Transparent)
                    .border(2.dp, if (isSelected) MaaColors.accent else MaaColors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = plan.name,
                        style = MaaTypography.titleSmall,
                        color = MaaColors.textPrimary
                    )
                    if (plan.isBestValue) {
                        Spacer(modifier = Modifier.width(MaaSpacing.small))
                        Box(
                            modifier = Modifier
                                .clip(MaaShapes.extraSmall)
                                .background(MaaColors.safe)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Best Value",
                                style = MaaTypography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Text(
                    text = plan.duration,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹",
                        style = MaaTypography.titleSmall,
                        color = MaaColors.accent
                    )
                    Text(
                        text = plan.price.toString(),
                        style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.accent
                    )
                }
                if (plan.originalPrice != null) {
                    Text(
                        text = "₹${plan.originalPrice}",
                        style = MaaTypography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        color = MaaColors.textTertiary
                    )
                }
                if (plan.savings != null) {
                    Text(
                        text = plan.savings,
                        style = MaaTypography.labelSmall,
                        color = MaaColors.safe
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentSection(
    selectedPlan: SubscriptionPlan,
    installedUpiApps: List<UpiApp>,
    paymentState: PaymentState,
    onPayWithUpi: (UpiApp?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Text(
                text = "Pay with UPI",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "Secure payment via Google Pay, PhonePe, Paytm or any UPI app",
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            if (installedUpiApps.isNotEmpty()) {
                // Show installed UPI apps
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
                ) {
                    items(installedUpiApps) { app ->
                        UpiAppButton(
                            app = app,
                            onClick = { onPayWithUpi(app) },
                            isLoading = paymentState is PaymentState.Processing
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MaaSpacing.medium))

                // Or use any UPI app
                MaaButton(
                    text = "Pay ₹${selectedPlan.price} with any UPI app",
                    onClick = { onPayWithUpi(null) },
                    style = MaaButtonStyle.OUTLINE,
                    fullWidth = true,
                    enabled = paymentState !is PaymentState.Processing
                )
            } else {
                // No UPI apps installed
                Text(
                    text = "No UPI app found. Please install Google Pay, PhonePe, or any UPI app.",
                    style = MaaTypography.bodyMedium,
                    color = MaaColors.warning,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun UpiAppButton(
    app: UpiApp,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(MaaShapes.medium)
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(MaaSpacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaaColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaaColors.accent,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = app.name.take(1),
                    style = MaaTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaaColors.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

        Text(
            text = app.name,
            style = MaaTypography.labelSmall,
            color = MaaColors.textSecondary
        )
    }
}

@Composable
private fun PaymentSuccessCard(onContinue: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.safe.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaaColors.safe),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            Text(
                text = "Payment Successful!",
                style = MaaTypography.titleMedium,
                color = MaaColors.safe
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            Text(
                text = "Welcome to Maa Premium",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            MaaButton(
                text = "Continue",
                onClick = onContinue,
                fullWidth = true
            )
        }
    }
}

@Composable
private fun PaymentFailedCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.error.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Payment Failed",
                style = MaaTypography.titleSmall,
                color = MaaColors.error
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            Text(
                text = message,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            MaaButton(
                text = "Try Again",
                onClick = onRetry,
                style = MaaButtonStyle.OUTLINE
            )
        }
    }
}

@Composable
private fun PaymentPendingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.warning.copy(alpha = 0.15f)
        ),
        shape = MaaShapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaaColors.warning,
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column {
                Text(
                    text = "Payment Pending",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.warning
                )
                Text(
                    text = "We're verifying your payment. This may take a few minutes.",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }
        }
    }
}

private data class PremiumFeature(
    val icon: ImageVector,
    val title: String,
    val titleHindi: String,
    val description: String
)
