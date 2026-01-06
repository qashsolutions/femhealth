package com.maa.health.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maa.health.data.model.Region
import com.maa.health.data.model.SupportedCountry
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Bottom sheet for selecting a country
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPickerBottomSheet(
    sheetState: SheetState,
    selectedCountry: SupportedCountry,
    onCountrySelected: (SupportedCountry) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            // Show popular countries first, then all others
            val popular = SupportedCountry.getPopular()
            val others = SupportedCountry.getAll().filter { it !in popular }
            popular + others
        } else {
            SupportedCountry.getAll().filter { country ->
                country.name.contains(searchQuery, ignoreCase = true) ||
                        country.dialCode.contains(searchQuery)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaaColors.surface,
        contentColor = MaaColors.textPrimary,
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaaColors.border)
                )
                Spacer(modifier = Modifier.height(MaaSpacing.medium))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaaSpacing.large)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Country",
                    style = MaaTypography.headlineSmall,
                    color = MaaColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaaColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Search field
            MaaTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search country or code",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(MaaSpacing.medium))

            // Country list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                // Show "Popular" header if not searching
                if (searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "Popular",
                            style = MaaTypography.labelMedium,
                            color = MaaColors.textTertiary,
                            modifier = Modifier.padding(vertical = MaaSpacing.small)
                        )
                    }
                }

                items(filteredCountries) { country ->
                    val isSelected = country == selectedCountry
                    val isPopular = country in SupportedCountry.getPopular()
                    val showDivider = searchQuery.isBlank() &&
                            isPopular &&
                            country == SupportedCountry.getPopular().last()

                    CountryItem(
                        country = country,
                        isSelected = isSelected,
                        onClick = {
                            onCountrySelected(country)
                            onDismiss()
                        }
                    )

                    if (showDivider) {
                        Column {
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            HorizontalDivider(color = MaaColors.border)
                            Spacer(modifier = Modifier.height(MaaSpacing.small))
                            Text(
                                text = "All Countries",
                                style = MaaTypography.labelMedium,
                                color = MaaColors.textTertiary,
                                modifier = Modifier.padding(vertical = MaaSpacing.small)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaaSpacing.large))
        }
    }
}

@Composable
private fun CountryItem(
    country: SupportedCountry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaaShapes.small)
            .clickable { onClick() }
            .background(
                if (isSelected) MaaColors.accent.copy(alpha = 0.1f)
                else MaaColors.surface
            )
            .padding(MaaSpacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag
        Text(
            text = country.flag,
            style = MaaTypography.headlineSmall
        )

        Spacer(modifier = Modifier.width(MaaSpacing.medium))

        // Country name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = country.name,
                style = MaaTypography.bodyMedium,
                color = MaaColors.textPrimary
            )
            Text(
                text = country.region.displayName,
                style = MaaTypography.labelSmall,
                color = MaaColors.textTertiary
            )
        }

        // Dial code
        Text(
            text = country.dialCode,
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary
        )

        // Check mark for selected
        if (isSelected) {
            Spacer(modifier = Modifier.width(MaaSpacing.small))
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaaColors.accent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
