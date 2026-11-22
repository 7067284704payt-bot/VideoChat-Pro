package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.GiftCatalog
import com.example.data.model.GiftOption
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DeepSpaceBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldStart
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.ActiveCallSession
import com.example.ui.viewmodel.VideoChatViewModel

@Composable
fun VideoCallScreen(
    session: ActiveCallSession,
    userDiamonds: Long,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onFlipCamera: () -> Unit,
    onToggleBeauty: () -> Unit,
    onSendGift: (GiftOption) -> Unit,
    onEndCall: () -> Unit
) {
    BackHandler(onBack = onEndCall)

    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showGiftBar by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasCameraPermission = results[Manifest.permission.CAMERA] == true
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBg)
            .testTag("active_video_call_screen")
    ) {
        // Remote Host Video Feed
        Image(
            painter = painterResource(id = session.creator.avatarRes),
            contentDescription = "${session.creator.name} live video",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle Beauty Warm Glow Overlay + Top/Bottom Scrims
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xAA07081C),
                            if (session.beautyEnabled) Color(0x18A855F7) else Color.Transparent,
                            Color(0xCC07081C)
                        )
                    )
                )
        )

        // Top Call Info Bar + Self PiP Camera Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0x990B0D24),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${session.creator.countryFlag} ${session.creator.name} ${session.creator.badgeEmoji}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "LIVE • ${VideoChatViewModel.formatDuration(session.elapsedSeconds)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldStart
                        )
                    }
                }
            }

            // Local Self-Camera PiP Window
            Surface(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, VioletGlow),
                color = CardSurface,
                shadowElevation = 12.dp,
                modifier = Modifier.size(width = 104.dp, height = 144.dp)
            ) {
                if (session.cameraEnabled && hasCameraPermission) {
                    LocalCameraPreview(useFrontCamera = session.frontCamera)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Camera off",
                                tint = TextSecondary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (!hasCameraPermission) "No Cam" else "Cam Off",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls + Gift Tray
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            session.lastGiftSent?.let { gift ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = VioletPrimary.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, GoldStart)
                ) {
                    Text(
                        text = "${gift.emoji} Sent ${gift.label} (-${gift.diamonds} 💎)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            AnimatedVisibility(visible = showGiftBar) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurfaceElevated.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, GoldStart.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Send Live Gift", style = MaterialTheme.typography.labelLarge, color = GoldStart)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Diamond, null, tint = SkyCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(VideoChatViewModel.formatNumber(userDiamonds), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GiftCatalog.gifts.forEach { gift ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = CardSurface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSendGift(gift) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(gift.emoji, fontSize = 22.sp)
                                        Text("${gift.diamonds} 💎", style = MaterialTheme.typography.labelSmall, color = GoldStart)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Control Bar
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xCC121436),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallControlIcon(
                        icon = if (session.micMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        description = "Toggle Microphone",
                        active = !session.micMuted,
                        onClick = onToggleMic
                    )
                    CallControlIcon(
                        icon = if (session.cameraEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                        description = "Toggle Camera",
                        active = session.cameraEnabled,
                        onClick = onToggleCamera
                    )
                    CallControlIcon(
                        icon = Icons.Rounded.Cameraswitch,
                        description = "Flip Camera",
                        active = true,
                        onClick = onFlipCamera
                    )
                    CallControlIcon(
                        icon = Icons.Rounded.AutoAwesome,
                        description = "Beauty Filter",
                        active = session.beautyEnabled,
                        tintOverride = if (session.beautyEnabled) GoldStart else TextSecondary,
                        onClick = onToggleBeauty
                    )
                    CallControlIcon(
                        icon = Icons.Rounded.CardGiftcard,
                        description = "Gift Diamonds",
                        active = showGiftBar,
                        tintOverride = GoldStart,
                        onClick = { showGiftBar = !showGiftBar }
                    )

                    IconButton(
                        onClick = onEndCall,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BadgeRed)
                            .testTag("end_video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallControlIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    active: Boolean,
    tintOverride: Color? = null,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (active) CardSurfaceElevated else BadgeRed.copy(alpha = 0.25f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tintOverride ?: if (active) TextPrimary else BadgeRed,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun LocalCameraPreview(useFrontCamera: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(useFrontCamera, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        var provider: ProcessCameraProvider? = null

        cameraProviderFuture.addListener({
            try {
                val camProvider = cameraProviderFuture.get()
                provider = camProvider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val selector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                camProvider.unbindAll()
                camProvider.bindToLifecycle(lifecycleOwner, selector, preview)
            } catch (e: Exception) {
                Log.w("VideoCallCamera", "Camera preview unavailable on device/emulator: ${e.message}")
            }
        }, executor)

        onDispose {
            try {
                provider?.unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}
