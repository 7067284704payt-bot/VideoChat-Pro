package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.components.CreatorCard
import com.example.ui.components.DailyRewardBanner
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldStart
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RewardTimerState

@Composable
fun HomeScreen(
    selectedCountry: String,
    searchQuery: String,
    rewardTimer: RewardTimerState,
    conversations: List<Conversation>,
    onSelectCountry: (String) -> Unit,
    onClaimDailyReward: () -> Unit,
    onStartVideoCall: (Creator) -> Unit,
    onOpenChat: (Creator) -> Unit,
    onToggleFavorite: (Creator) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMoreCountries by remember { mutableStateOf(false) }
    var inspectedCreator by remember { mutableStateOf<Creator?>(null) }

    val filteredCreators = remember(selectedCountry, searchQuery) {
        CreatorCatalog.creators.filter { creator ->
            val countryMatch = selectedCountry == "All" || creator.country.equals(selectedCountry, ignoreCase = true)
            val query = searchQuery.trim()
            val queryMatch = query.isEmpty() ||
                creator.name.contains(query, ignoreCase = true) ||
                creator.country.contains(query, ignoreCase = true) ||
                creator.languages.any { it.contains(query, ignoreCase = true) }
            countryMatch && queryMatch
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CountryFilterRow(
                        selectedCountry = selectedCountry,
                        showMoreCountries = showMoreCountries,
                        onSelectCountry = onSelectCountry,
                        onToggleMore = { showMoreCountries = !showMoreCountries }
                    )

                    AnimatedVisibility(visible = showMoreCountries) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CreatorCatalog.moreCountries.forEach { country ->
                                CountryChip(
                                    label = country,
                                    flag = CreatorCatalog.countryFlagFor(country),
                                    selected = selectedCountry == country,
                                    onClick = { onSelectCountry(country) }
                                )
                            }
                        }
                    }

                    DailyRewardBanner(
                        rewardTimer = rewardTimer,
                        onClaimClick = onClaimDailyReward
                    )
                }
            }

            if (filteredCreators.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = VioletGlow,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No creators match your filter",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try selecting All countries or clearing your search.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                val firstBatchCount = columns * 2
                val firstBatch = filteredCreators.take(firstBatchCount)
                val remainingBatch = filteredCreators.drop(firstBatchCount)

                itemsIndexed(firstBatch, key = { _, creator -> creator.id }) { index, creator ->
                    val startPad = if (index % columns == 0) 16.dp else 0.dp
                    val endPad = if (index % columns == columns - 1) 16.dp else 0.dp
                    CreatorCard(
                        creator = creator,
                        onCardClick = { inspectedCreator = creator },
                        onVideoCallClick = { onStartVideoCall(creator) },
                        onChatClick = { onOpenChat(creator) },
                        modifier = Modifier.padding(start = startPad, end = endPad)
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "home_ad_300x250") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.Banner300x250,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (remainingBatch.isNotEmpty()) {
                    itemsIndexed(remainingBatch, key = { _, creator -> creator.id }) { index, creator ->
                        val startPad = if (index % columns == 0) 16.dp else 0.dp
                        val endPad = if (index % columns == columns - 1) 16.dp else 0.dp
                        CreatorCard(
                            creator = creator,
                            onCardClick = { inspectedCreator = creator },
                            onVideoCallClick = { onStartVideoCall(creator) },
                            onChatClick = { onOpenChat(creator) },
                            modifier = Modifier.padding(start = startPad, end = endPad)
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "home_ad_468x60_and_inline") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.Banner468x60,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.InlineScript,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    inspectedCreator?.let { creator ->
        val isFav = conversations.firstOrNull { it.creatorId == creator.id }?.favorite == true
        CreatorDetailSheet(
            creator = creator,
            isFavorite = isFav,
            onToggleFavorite = { onToggleFavorite(creator) },
            onStartVideoCall = {
                inspectedCreator = null
                onStartVideoCall(creator)
            },
            onOpenChat = {
                inspectedCreator = null
                onOpenChat(creator)
            },
            onDismiss = { inspectedCreator = null }
        )
    }
}

@Composable
private fun CountryFilterRow(
    selectedCountry: String,
    showMoreCountries: Boolean,
    onSelectCountry: (String) -> Unit,
    onToggleMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CreatorCatalog.primaryCountries.forEach { country ->
            CountryChip(
                label = country,
                flag = if (country == "All") null else CreatorCatalog.countryFlagFor(country),
                selected = selectedCountry == country,
                onClick = { onSelectCountry(country) }
            )
        }

        // More dropdown chip
        Surface(
            shape = RoundedCornerShape(50),
            color = if (CreatorCatalog.moreCountries.contains(selectedCountry)) VioletPrimary else CardSurface,
            border = BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(50))
                .clickable(onClick = onToggleMore)
                .testTag("country_filter_more")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "More",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showMoreCountries) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "More countries",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CountryChip(
    label: String,
    flag: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (selected) VioletGlow else GlassBorder
        ),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(VioletGlow, VioletPrimary))
                } else {
                    Brush.horizontalGradient(listOf(CardSurface, CardSurface))
                }
            )
            .clickable(onClick = onClick)
            .testTag("country_chip_${label.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            if (flag == null) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(text = flag, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDetailSheet(
    creator: Creator,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onStartVideoCall: () -> Unit,
    onOpenChat: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MidnightIndigo,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = creator.avatarRes),
                    contentDescription = creator.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${creator.countryFlag} ${creator.name} ${creator.badgeEmoji}",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = creator.tagline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = VioletGlow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(OnlineGreen)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online", style = MaterialTheme.typography.labelSmall, color = OnlineGreen)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Visibility, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${creator.viewerCount}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = GoldStart, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${creator.rating}", style = MaterialTheme.typography.labelSmall, color = GoldStart)
                        }
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite creator",
                        tint = if (isFavorite) BadgeRed else TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = creator.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        creator.languages.forEach { lang ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = CardSurface
                            ) {
                                Text(
                                    text = lang,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.Diamond, null, tint = SkyCyan, modifier = Modifier.size(15.dp))
                        Text(
                            text = "${creator.diamondsPerMinute}/min",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldStart
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenChat,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ElectricBlue),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Rounded.ChatBubble, null, tint = SkyCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message", color = TextPrimary)
                }
                Button(
                    onClick = onStartVideoCall,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Rounded.Videocam, null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Video Call", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
