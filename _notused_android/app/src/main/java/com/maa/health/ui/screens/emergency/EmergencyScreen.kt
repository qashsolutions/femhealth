package com.maa.health.ui.screens.emergency

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maa.health.ui.components.MaaButton
import com.maa.health.ui.components.MaaButtonStyle
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Emergency and Crisis Support Screen
 *
 * Features:
 * - Emergency helpline numbers (India-specific)
 * - Ambulance/hospital finder
 * - Mental health crisis support
 * - Domestic violence helpline
 * - Quick call buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onBack: () -> Unit,
    onFindHospital: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val emergencyContacts = remember {
        listOf(
            EmergencyContact(
                name = "Emergency (Ambulance)",
                number = "108",
                description = "Free ambulance service across India",
                icon = Icons.Default.Call,
                color = MaaColors.error,
                isPrimary = true
            ),
            EmergencyContact(
                name = "Women Helpline",
                number = "181",
                description = "24/7 support for women in distress",
                icon = Icons.Default.Favorite,
                color = MaaColors.stageReproductive,
                isPrimary = true
            ),
            EmergencyContact(
                name = "Police",
                number = "100",
                description = "Emergency police assistance",
                icon = Icons.Default.Warning,
                color = MaaColors.warning,
                isPrimary = false
            ),
            EmergencyContact(
                name = "Child Helpline",
                number = "1098",
                description = "For children in need of care and protection",
                icon = Icons.Default.Favorite,
                color = MaaColors.stageChildCare,
                isPrimary = false
            ),
            EmergencyContact(
                name = "Mental Health Helpline",
                number = "08046110007",
                description = "iCall - Free counseling support",
                icon = Icons.Default.Favorite,
                color = MaaColors.stageMentalHealth,
                isPrimary = false
            ),
            EmergencyContact(
                name = "Vandrevala Foundation",
                number = "18602662345",
                description = "24/7 mental health support",
                icon = Icons.Default.Favorite,
                color = MaaColors.stageMentalHealth,
                isPrimary = false
            ),
            EmergencyContact(
                name = "Domestic Violence",
                number = "1091",
                description = "Women in distress helpline",
                icon = Icons.Default.Warning,
                color = MaaColors.error,
                isPrimary = false
            ),
            EmergencyContact(
                name = "Senior Citizen Helpline",
                number = "14567",
                description = "Support for elderly citizens",
                icon = Icons.Default.Favorite,
                color = MaaColors.stageElder,
                isPrimary = false
            )
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
                    text = "Emergency Help",
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
            // Emergency banner
            EmergencyBanner()

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Primary emergency contacts (large buttons)
            Text(
                text = "Emergency Numbers",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            emergencyContacts.filter { it.isPrimary }.forEach { contact ->
                PrimaryEmergencyCard(
                    contact = contact,
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${contact.number}")
                        }
                        context.startActivity(intent)
                    }
                )
                Spacer(modifier = Modifier.height(MaaSpacing.small))
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Find nearby hospital
            NearbyHospitalCard(onClick = onFindHospital)

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Other helplines
            Text(
                text = "Other Helplines",
                style = MaaTypography.titleSmall,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
                shape = MaaShapes.medium
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    emergencyContacts.filter { !it.isPrimary }.forEach { contact ->
                        HelplineItem(
                            contact = contact,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${contact.number}")
                                }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Safety tips
            SafetyTipsCard()

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Disclaimer
            Text(
                text = "If you are in immediate danger, please call emergency services immediately. This app provides information only and is not a substitute for professional medical or emergency assistance.",
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaaSpacing.medium)
            )

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun EmergencyBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaaColors.error),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = "In case of emergency",
                style = MaaTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = "Call 108 for ambulance or 100 for police",
                style = MaaTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrimaryEmergencyCard(
    contact: EmergencyContact,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCall),
        colors = CardDefaults.cardColors(
            containerColor = contact.color.copy(alpha = 0.15f)
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
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(contact.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = contact.description,
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Text(
                text = contact.number,
                style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = contact.color
            )
        }
    }
}

@Composable
private fun NearbyHospitalCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaaColors.surface),
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaaColors.accent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaaColors.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaaSpacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Find Nearby Hospital",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
                Text(
                    text = "Locate government and private hospitals near you",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary
                )
            }

            Text(
                text = "→",
                style = MaaTypography.titleLarge,
                color = MaaColors.accent
            )
        }
    }
}

@Composable
private fun HelplineItem(
    contact: EmergencyContact,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCall)
            .padding(MaaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(contact.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = contact.icon,
                contentDescription = null,
                tint = contact.color,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaaTypography.bodyLarge,
                color = MaaColors.textPrimary
            )
            Text(
                text = contact.description,
                style = MaaTypography.bodySmall,
                color = MaaColors.textTertiary
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = contact.number,
                style = MaaTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = contact.color
            )
            Text(
                text = "Tap to call",
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary
            )
        }
    }
}

@Composable
private fun SafetyTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.warning.copy(alpha = 0.1f)
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaaColors.warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = "Safety Tips",
                    style = MaaTypography.titleSmall,
                    color = MaaColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            val tips = listOf(
                "Keep emergency numbers saved in your phone",
                "Share your location with a trusted family member",
                "Know your nearest hospital location",
                "Keep important documents accessible",
                "Have a first aid kit at home"
            )

            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaaTypography.bodySmall,
                    color = MaaColors.textSecondary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

private data class EmergencyContact(
    val name: String,
    val number: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isPrimary: Boolean
)
