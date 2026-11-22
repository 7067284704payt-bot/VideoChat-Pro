package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shop
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.distribution.AppDistributionState
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.GoldEnd
import com.example.ui.theme.GoldStart
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary

@Composable
fun DownloadAppFeatureCard(
    state: AppDistributionState,
    onDownloadAppClick: () -> Unit,
    onUpdateAppClick: () -> Unit,
    onShareAppClick: () -> Unit,
    onOpenSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(
            1.2.dp,
            if (state.isUpdateAvailable) GoldStart.copy(alpha = 0.65f) else SkyCyan.copy(alpha = 0.4f)
        ),
        shadowElevation = 10.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Branded Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, VioletGlow.copy(alpha = 0.6f)),
                    color = CardSurface,
                    modifier = Modifier.size(54.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = "Official App",
                            tint = SkyCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Installed: v${state.installedVersionName} (Build ${state.installedVersionCode})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (state.isUpdateAvailable) GoldEnd.copy(alpha = 0.2f) else OnlineGreen.copy(alpha = 0.16f),
                    border = BorderStroke(
                        1.dp,
                        if (state.isUpdateAvailable) GoldStart else OnlineGreen.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isUpdateAvailable) Icons.Rounded.NewReleases else Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (state.isUpdateAvailable) GoldStart else OnlineGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isUpdateAvailable) "v${state.latestVersionName} New" else "Up to date",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isUpdateAvailable) GoldStart else OnlineGreen
                        )
                    }
                }
            }

            // Latest Version & Update Details Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Latest Version: v${state.latestVersionName} (Build ${state.latestVersionCode})",
                            style = MaterialTheme.typography.labelLarge,
                            color = SkyCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = when {
                                state.hasPlayStoreUrl -> "Google Play"
                                state.hasApkDownloadUrl -> "Direct APK"
                                else -> "Setup Required"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = state.releaseNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Update App CTA when a newer version is available
            if (state.isUpdateAvailable) {
                Button(
                    onClick = onUpdateAppClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldEnd),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("update_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        tint = GoldDarkText,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update App to v${state.latestVersionName}",
                        color = GoldDarkText,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Download App & Share App Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDownloadAppClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("download_app_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(VioletPrimary, ElectricBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Download App",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onShareAppClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SkyCyan.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_app_link_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = SkyCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (onOpenSettingsClick != null) {
                OutlinedButton(
                    onClick = onOpenSettingsClick,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("profile_configure_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = VioletGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "App Settings & Release URLs",
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadOptionsModalSheet(
    state: AppDistributionState,
    fallbackReason: String?,
    onOpenPlayStore: () -> Unit,
    onDownloadOfficialApk: () -> Unit,
    onShareLink: () -> Unit,
    onOpenSettings: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, VioletGlow.copy(alpha = 0.6f)),
                    color = CardSurface,
                    modifier = Modifier.size(48.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Download ${stringResource(id = R.string.app_name)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Installed v${state.installedVersionName} • Latest v${state.latestVersionName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SkyCyan
                    )
                }
            }

            if (!fallbackReason.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = GoldEnd.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, GoldStart.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = fallbackReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldStart,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Google Play Store Option
            Button(
                onClick = onOpenPlayStore,
                enabled = state.hasPlayStoreUrl,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VioletPrimary,
                    disabledContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sheet_open_play_store_button")
            ) {
                Icon(Icons.Rounded.Shop, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.hasPlayStoreUrl) "Open Google Play Store" else "Google Play Link Not Configured",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Official APK Download Fallback Option
            Button(
                onClick = onDownloadOfficialApk,
                enabled = state.hasApkDownloadUrl,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = CardSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sheet_download_apk_button")
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.hasApkDownloadUrl) "Download Official APK (HTTPS)" else "Official APK URL Not Configured",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShareLink,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SkyCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, tint = SkyCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Link", color = TextPrimary)
                }
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("sheet_open_settings_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(VioletPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = VioletGlow, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settings", color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
