package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.GoldEnd
import com.example.ui.theme.GoldStart
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletDeep
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary

@Composable
fun DiscoverScreen(
    conversations: List<Conversation>,
    searchQuery: String,
    onStartVideoCall: (Creator) -> Unit,
    onOpenChat: (Creator) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = remember { listOf("Trending", "Favorites", "English", "Hindi", "Low Cost") }
    var selectedFilter by remember { mutableStateOf("Trending") }
    val favoriteIds = remember(conversations) {
        conversations.filter { it.favorite }.map { it.creatorId }.toSet()
    }

    val filteredCreators = remember(selectedFilter, searchQuery, favoriteIds) {
        val base = when (selectedFilter) {
            "Favorites" -> CreatorCatalog.creators.filter { favoriteIds.contains(it.id) }
            "English" -> CreatorCatalog.creators.filter { it.languages.contains("English") }
            "Hindi" -> CreatorCatalog.creators.filter { it.languages.contains("Hindi") }
            "Low Cost" -> CreatorCatalog.creators.sortedBy { it.diamondsPerMinute }
            else -> CreatorCatalog.creators.sortedByDescending { it.viewerCount }
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) base else base.filter {
            it.name.contains(q, ignoreCase = true) || it.country.contains(q, ignoreCase = true)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Instant Random Video Match Spotlight
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.2.dp, VioletGlow.copy(alpha = 0.6f)),
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                listOf(VioletDeep, ElectricBlue.copy(alpha = 0.85f), CardSurfaceElevated)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldStart,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "INSTANT VIDEO MATCH",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldStart,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Meet Someone New Live",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "${CreatorCatalog.creators.size} verified hosts online now",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val pick = CreatorCatalog.creators.random()
                                onStartVideoCall(pick)
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldEnd),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("instant_match_button")
                        ) {
                            Icon(Icons.Rounded.Shuffle, null, tint = GoldDarkText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Match", color = GoldDarkText, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // Top Rated Spotlight Carousel
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Top Rated Hosts",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(CreatorCatalog.creators.sortedByDescending { it.rating }, key = { "top_${it.id}" }) { creator ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .width(146.dp)
                                .clickable { onStartVideoCall(creator) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box {
                                    Image(
                                        painter = painterResource(id = creator.avatarRes),
                                        contentDescription = creator.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0x9907081C),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Rounded.Star, null, tint = GoldStart, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "${creator.rating}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${creator.countryFlag} ${creator.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = creator.tagline,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Vibe / Language filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { label ->
                    val selected = label == selectedFilter
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) VioletPrimary else CardSurface,
                        border = BorderStroke(1.dp, if (selected) VioletGlow else GlassBorder),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable { selectedFilter = label }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            if (label == "Favorites") {
                                Icon(Icons.Rounded.Favorite, null, tint = BadgeRed, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Adsterra Native Banner
        item(key = "discover_native_ad") {
            AdsterraBannerCard(
                unit = AdsterraAdUnit.NativeBanner,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filteredCreators.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (selectedFilter == "Favorites") "Tap the heart on any creator profile to save favorites here." else "No creators found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(filteredCreators, key = { "discover_${it.id}" }) { creator ->
                DiscoverListCard(
                    creator = creator,
                    onVideoCall = { onStartVideoCall(creator) },
                    onChat = { onOpenChat(creator) }
                )
            }
        }

        // Featured Partners Strip (160x300 Skyscraper + 300x250 Rectangle)
        item(key = "discover_partner_banners") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Featured Sponsors",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    AdsterraBannerCard(
                        unit = AdsterraAdUnit.Banner160x300,
                        modifier = Modifier.width(184.dp)
                    )
                    AdsterraBannerCard(
                        unit = AdsterraAdUnit.Banner300x250,
                        modifier = Modifier.width(324.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiscoverListCard(
    creator: Creator,
    onVideoCall: () -> Unit,
    onChat: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(id = creator.avatarRes),
                    contentDescription = creator.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(OnlineGreen)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${creator.countryFlag} ${creator.name} ${creator.badgeEmoji}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = creator.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = VioletGlow,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Visibility, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("${creator.viewerCount}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Text(
                        text = creator.languages.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyCyan,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue)
                        .clickable(onClick = onChat),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.ChatBubble, "Chat", tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VioletPrimary)
                        .clickable(onClick = onVideoCall),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Videocam, "Video Call", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
