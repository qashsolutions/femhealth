package com.maa.health.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maa.health.service.VoiceAction
import com.maa.health.service.VoiceService
import com.maa.health.ui.components.FloatingVoiceFAB
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaShapes
import com.maa.health.ui.theme.MaaSpacing
import com.maa.health.ui.theme.MaaTypography

/**
 * Learn Hub Screen - Educational content and resources
 *
 * Features:
 * - Health articles by lifecycle stage
 * - Video content (offline available)
 * - Expert Q&A
 * - Searchable knowledge base
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnHubScreen(
    onNavigateToArticle: (String) -> Unit,
    onNavigateToVideos: () -> Unit,
    onNavigateToSearch: () -> Unit,
    voiceService: VoiceService? = null,
    onVoiceSearch: ((String) -> Unit)? = null
) {
    val categories = listOf(
        LearnCategory(
            title = "Pregnancy",
            titleHindi = "गर्भावस्था",
            icon = Icons.Default.PregnantWoman,
            color = MaaColors.stagePregnancy,
            articles = listOf(
                Article("1", "Danger Signs During Pregnancy", "गर्भावस्था में खतरे के संकेत", "5 min read"),
                Article("2", "Nutrition for Healthy Pregnancy", "स्वस्थ गर्भावस्था के लिए पोषण", "8 min read"),
                Article("3", "When to Visit the Doctor", "डॉक्टर से कब मिलें", "4 min read")
            )
        ),
        LearnCategory(
            title = "Child Care",
            titleHindi = "शिशु देखभाल",
            icon = Icons.Default.ChildCare,
            color = MaaColors.stageChildCare,
            articles = listOf(
                Article("4", "Breastfeeding Guide", "स्तनपान गाइड", "10 min read"),
                Article("5", "Vaccination Schedule", "टीकाकरण अनुसूची", "6 min read"),
                Article("6", "Developmental Milestones", "विकास के पड़ाव", "7 min read")
            )
        ),
        LearnCategory(
            title = "Menstrual Health",
            titleHindi = "मासिक धर्म स्वास्थ्य",
            icon = Icons.Default.Timeline,
            color = MaaColors.stageReproductive,
            articles = listOf(
                Article("7", "Understanding Your Cycle", "अपने चक्र को समझें", "6 min read"),
                Article("8", "Managing Period Pain", "पीरियड दर्द प्रबंधन", "5 min read"),
                Article("9", "PCOS Awareness", "पीसीओएस जागरूकता", "8 min read")
            )
        ),
        LearnCategory(
            title = "Mental Health",
            titleHindi = "मानसिक स्वास्थ्य",
            icon = Icons.Default.Psychology,
            color = MaaColors.stageMentalHealth,
            articles = listOf(
                Article("10", "Postpartum Depression Signs", "प्रसवोत्तर अवसाद के संकेत", "6 min read"),
                Article("11", "Stress Management", "तनाव प्रबंधन", "5 min read"),
                Article("12", "Self-Care for Mothers", "माताओं के लिए स्व-देखभाल", "4 min read")
            )
        )
    )

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
                Column {
                    Text(
                        text = "Learn",
                        style = MaaTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaaColors.textPrimary
                    )
                    Text(
                        text = "Health knowledge at your fingertips",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                }
            },
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search articles",
                        tint = MaaColors.textSecondary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaaColors.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = MaaSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaaSpacing.large)
        ) {
            // Featured video section
            item {
                FeaturedVideoSection(onNavigateToVideos = onNavigateToVideos)
            }

            // Categories with articles
            items(categories) { category ->
                CategorySection(
                    category = category,
                    onArticleClick = onNavigateToArticle
                )
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
private fun FeaturedVideoSection(onNavigateToVideos: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = MaaSpacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Video Lessons",
                style = MaaTypography.titleMedium,
                color = MaaColors.textPrimary
            )
            Text(
                text = "See all →",
                style = MaaTypography.labelMedium,
                color = MaaColors.accent,
                modifier = Modifier
                    .clip(MaaShapes.small)
                    .padding(MaaSpacing.extraSmall)
            )
        }

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        Card(
            onClick = onNavigateToVideos,
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
                Card(
                    modifier = Modifier.size(80.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaaColors.accent.copy(alpha = 0.2f)
                    ),
                    shape = MaaShapes.medium
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = MaaColors.accent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(MaaSpacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Essential Health Videos",
                        style = MaaTypography.titleSmall,
                        color = MaaColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                    Text(
                        text = "Watch offline • Available in your language",
                        style = MaaTypography.bodySmall,
                        color = MaaColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))
                    Text(
                        text = "12 videos • 45 min total",
                        style = MaaTypography.labelSmall,
                        color = MaaColors.textTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(
    category: LearnCategory,
    onArticleClick: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaaSpacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MaaSpacing.small))
                Text(
                    text = category.title,
                    style = MaaTypography.titleMedium,
                    color = MaaColors.textPrimary
                )
            }
            Text(
                text = "See all →",
                style = MaaTypography.labelMedium,
                color = MaaColors.accent,
                modifier = Modifier
                    .clip(MaaShapes.small)
                    .padding(MaaSpacing.extraSmall)
            )
        }

        Spacer(modifier = Modifier.height(MaaSpacing.small))

        LazyRow(
            contentPadding = PaddingValues(horizontal = MaaSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaaSpacing.medium)
        ) {
            items(category.articles) { article ->
                ArticleCard(
                    article = article,
                    categoryColor = category.color,
                    onClick = { onArticleClick(article.id) }
                )
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    categoryColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaaColors.surface
        ),
        shape = MaaShapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaaSpacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Article,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = article.readTime,
                    style = MaaTypography.labelSmall,
                    color = MaaColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(MaaSpacing.small))

            Text(
                text = article.title,
                style = MaaTypography.labelLarge,
                color = MaaColors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(MaaSpacing.extraSmall))

            Text(
                text = article.titleHindi,
                style = MaaTypography.bodySmall,
                color = MaaColors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class LearnCategory(
    val title: String,
    val titleHindi: String,
    val icon: ImageVector,
    val color: Color,
    val articles: List<Article>
)

private data class Article(
    val id: String,
    val title: String,
    val titleHindi: String,
    val readTime: String
)
