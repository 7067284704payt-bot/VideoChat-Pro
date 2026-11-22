package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Hd
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.distribution.AppDistributionState
import com.example.data.model.UserProfile
import com.example.ui.components.DownloadAppFeatureCard
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DeepSpaceBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.UiState

@Composable
fun SettingsScreen(
    distributionState: AppDistributionState,
    userProfileState: UiState<UserProfile?>,
    onBack: () -> Unit,
    onDownloadAppClick: () -> Unit,
    onUpdateAppClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onSaveDistributionConfig: (
        playStoreUrl: String,
        apkDownloadUrl: String,
        latestVersionName: String,
        latestVersionCode: Int,
        releaseNotes: String
    ) -> Unit,
    onResetDistributionConfig: () -> Unit,
    onTogglePreferences: (beauty: Boolean, hd: Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    BackHandler(onBack = onBack)

    var playStoreInput by remember(distributionState.playStoreUrl) {
        mutableStateOf(distributionState.playStoreUrl)
    }
    var apkUrlInput by remember(distributionState.apkDownloadUrl) {
        mutableStateOf(distributionState.apkDownloadUrl)
    }
    var latestVersionNameInput by remember(distributionState.latestVersionName) {
        mutableStateOf(distributionState.latestVersionName)
    }
    var latestVersionCodeInput by remember(distributionState.latestVersionCode) {
        mutableStateOf(distributionState.latestVersionCode.toString())
    }
    var releaseNotesInput by remember(distributionState.releaseNotes) {
        mutableStateOf(distributionState.releaseNotes)
    }

    val profile = (userProfileState as? UiState.Success)?.data
    var beautyEnabled by remember(profile?.userId) {
        mutableStateOf(profile?.beautyFilterEnabled ?: true)
    }
    var hdEnabled by remember(profile?.userId) {
        mutableStateOf(profile?.hdVideoEnabled ?: true)
    }

    LaunchedEffect(profile) {
        if (profile != null) {
            beautyEnabled = profile.beautyFilterEnabled
            hdEnabled = profile.hdVideoEnabled
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepSpaceBg, MidnightIndigo)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                color = MidnightIndigo.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "App Download, Updates & Call Preferences",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Download App, Share & Latest Version Card
                item(key = "settings_download_card") {
                    DownloadAppFeatureCard(
                        state = distributionState,
                        onDownloadAppClick = onDownloadAppClick,
                        onUpdateAppClick = onUpdateAppClick,
                        onShareAppClick = onShareAppClick
                    )
                }

                // Configurable Play Store & Secure APK Download URLs
                item(key = "settings_distribution_config") {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Link, contentDescription = null, tint = SkyCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Distribution & Update Links",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "Configure your official Google Play Store listing or secure HTTPS APK download URL.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )

                            OutlinedTextField(
                                value = playStoreInput,
                                onValueChange = { playStoreInput = it },
                                label = { Text("Google Play Store URL") },
                                placeholder = { Text("https://play.google.com/store/apps/details?id=...") },
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
                                    .testTag("settings_play_store_url_input")
                            )

                            OutlinedTextField(
                                value = apkUrlInput,
                                onValueChange = { apkUrlInput = it },
                                label = { Text("Official APK Download URL (HTTPS)") },
                                placeholder = { Text("https://...") },
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
                                    .testTag("settings_apk_url_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = latestVersionNameInput,
                                    onValueChange = { if (it.length <= 20) latestVersionNameInput = it },
                                    label = { Text("Latest Version") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VioletGlow,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_latest_version_name_input")
                                )

                                OutlinedTextField(
                                    value = latestVersionCodeInput,
                                    onValueChange = { input ->
                                        if (input.length <= 6 && input.all { it.isDigit() }) {
                                            latestVersionCodeInput = input
                                        }
                                    },
                                    label = { Text("Version Code") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VioletGlow,
                                        unfocusedBorderColor = GlassBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("settings_latest_version_code_input")
                                )
                            }

                            OutlinedTextField(
                                value = releaseNotesInput,
                                onValueChange = { if (it.length <= 240) releaseNotesInput = it },
                                label = { Text("Update Information") },
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
                                    .testTag("settings_release_notes_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onSaveDistributionConfig(
                                            playStoreInput,
                                            apkUrlInput,
                                            latestVersionNameInput,
                                            latestVersionCodeInput.toIntOrNull() ?: distributionState.installedVersionCode,
                                            releaseNotesInput
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(48.dp)
                                        .testTag("settings_save_config_button")
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Save Links", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onResetDistributionConfig,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, GlassBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("settings_reset_config_button")
                                ) {
                                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = TextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset", color = TextPrimary)
                                }
                            }
                        }
                    }
                }

                // Video Call Preferences synced to Profile
                item(key = "settings_call_preferences") {
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
                                    onCheckedChange = {
                                        beautyEnabled = it
                                        onTogglePreferences(it, hdEnabled)
                                    },
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
                                    onCheckedChange = {
                                        hdEnabled = it
                                        onTogglePreferences(beautyEnabled, it)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TextPrimary, checkedTrackColor = ElectricBlue)
                                )
                            }
                        }
                    }
                }

                // Sign Out
                item(key = "settings_sign_out") {
                    OutlinedButton(
                        onClick = onSignOut,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BadgeRed.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("settings_sign_out_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = BadgeRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", color = BadgeRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
