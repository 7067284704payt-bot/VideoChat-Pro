package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.distribution.AppDistributionState
import com.example.data.model.CreatorCatalog
import com.example.data.model.UserProfile
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.components.DownloadAppFeatureCard
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.GoldEnd
import com.example.ui.theme.GoldStart
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RewardTimerState
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.VideoChatViewModel

@Composable
fun ProfileScreen(
    userProfileState: UiState<UserProfile?>,
    rewardTimer: RewardTimerState,
    distributionState: AppDistributionState,
    onClaimDailyReward: () -> Unit,
    onSaveProfile: (displayName: String, country: String, bio: String, beauty: Boolean, hd: Boolean) -> Unit,
    onDownloadAppClick: () -> Unit,
    onUpdateAppClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countries = remember {
        listOf("India", "USA", "UK", "Pakistan", "Bangladesh", "Nepal", "Canada")
    }

    when (userProfileState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VioletGlow)
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(userProfileState.message, color = BadgeRed, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is UiState.Success -> {
            val profile = userProfileState.data
            var name by remember(profile?.userId) { mutableStateOf(profile?.displayName ?: "") }
            var country by remember(profile?.userId) { mutableStateOf(profile?.country ?: "India") }
            var bio by remember(profile?.userId) { mutableStateOf(profile?.bio ?: "") }
            var beautyEnabled by remember(profile?.userId) { mutableStateOf(profile?.beautyFilterEnabled ?: true) }
            var hdEnabled by remember(profile?.userId) { mutableStateOf(profile?.hdVideoEnabled ?: true) }

            LaunchedEffect(profile) {
                if (profile != null && name.isEmpty()) {
                    name = profile.displayName
                    country = profile.country
                    bio = profile.bio ?: ""
                    beautyEnabled = profile.beautyFilterEnabled
                    hdEnabled = profile.hdVideoEnabled
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = modifier.fillMaxSize()
            ) {
                // Avatar & Wallet Hero Card
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .testTag("open_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Open Settings",
                                    tint = TextPrimary
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (!profile?.photoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = profile.photoUrl,
                                        contentDescription = "Profile avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(82.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(VioletPrimary, ElectricBlue))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Person, null, tint = TextPrimary, modifier = Modifier.size(42.dp))
                                    }
                                }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = profile?.displayName?.ifBlank { "VideoChat User" } ?: "VideoChat User",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "${CreatorCatalog.countryFlagFor(country)} $country",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardSurface,
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Diamond, null, tint = SkyCyan, modifier = Modifier.size(26.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("Diamond Balance", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                            Text(
                                                text = VideoChatViewModel.formatNumber(profile?.diamonds ?: 0L),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = GoldStart
                                            )
                                        }
                                    }
                                    if (rewardTimer.canClaim) {
                                        Button(
                                            onClick = onClaimDailyReward,
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldEnd),
                                            modifier = Modifier.testTag("profile_claim_daily_button")
                                        ) {
                                            Text("Claim 10,000", color = GoldDarkText, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = CardSurfaceElevated,
                                            border = BorderStroke(1.dp, GlassBorder)
                                        ) {
                                            Text(
                                                text = "Free in ${rewardTimer.formattedCountdown}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = SkyCyan,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

                // Download App, Share Link & Version Update Card
                item(key = "profile_download_app_card") {
                    DownloadAppFeatureCard(
                        state = distributionState,
                        onDownloadAppClick = onDownloadAppClick,
                        onUpdateAppClick = onUpdateAppClick,
                        onShareAppClick = onShareAppClick,
                        onOpenSettingsClick = onOpenSettings
                    )
                }

                // Editable Profile Fields
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Account Details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)

                            OutlinedTextField(
                                value = name,
                                onValueChange = { if (it.length <= 50) name = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletGlow,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_name_input")
                            )

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 200) bio = it },
                                label = { Text("Bio") },
                                maxLines = 3,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletGlow,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_bio_input")
                            )

                            Text("Region", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                countries.forEach { c ->
                                    val selected = country == c
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (selected) VioletPrimary else CardSurfaceElevated,
                                        border = BorderStroke(1.dp, if (selected) VioletGlow else GlassBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { country = c }
                                    ) {
                                        Text(
                                            text = "${CreatorCatalog.countryFlagFor(c)} $c",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Call Preferences
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Video Call Preferences", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.AutoAwesome, null, tint = VioletGlow)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Beauty Glow Filter", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                        Text("Warm lighting enhancement on video", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                                Switch(
                                    checked = beautyEnabled,
                                    onCheckedChange = { beautyEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = VioletPrimary)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Hd, null, tint = SkyCyan)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("HD 1080p Streaming", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                                        Text("Crisp camera preview & high bitrate", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                }
                                Switch(
                                    checked = hdEnabled,
                                    onCheckedChange = { hdEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = ElectricBlue)
                                )
                            }
                        }
                    }
                }

                // Save & Sign Out Buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                onSaveProfile(
                                    name.ifBlank { "VideoChat User" },
                                    country,
                                    bio,
                                    beautyEnabled,
                                    hdEnabled
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_profile_button")
                        ) {
                            Icon(Icons.Rounded.Check, null, tint = TextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Changes", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BadgeRed.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = BadgeRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out", color = BadgeRed, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Sponsored Native & Wide Skyscraper Section
                item(key = "profile_native_ad") {
                    AdsterraBannerCard(
                        unit = AdsterraAdUnit.NativeBanner,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item(key = "profile_skyscraper_ad") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.Banner160x600,
                            modifier = Modifier.width(184.dp)
                        )
                    }
                }
            }
        }
    }
}
