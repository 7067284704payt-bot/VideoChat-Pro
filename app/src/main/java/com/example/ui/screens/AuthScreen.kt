package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GoldStart
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary

private val AuroraBgTop = Color(0xFF060B24)
private val AuroraBgMid = Color(0xFF0D183D)
private val AuroraBgBottom = Color(0xFF19123B)
private val RoseAccent = Color(0xFFEC4899)
private val TealGlow = Color(0xFF14B8A6)
private val FeatureCardStart = Color(0xE613224A)
private val FeatureCardEnd = Color(0xE61E184A)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var isCheckingSession by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                isCheckingSession = false
                onAuthSuccess()
            },
            onUnauthenticated = {
                isCheckingSession = false
            },
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AuroraBgTop, AuroraBgMid, AuroraBgBottom)
                )
            )
    ) {
        // Soft ambient aurora background glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SkyCyan.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(size.width * 0.22f, size.height * 0.22f),
                    radius = size.width * 0.65f
                ),
                center = Offset(size.width * 0.22f, size.height * 0.22f),
                radius = size.width * 0.65f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(RoseAccent.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.42f),
                    radius = size.width * 0.60f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.42f),
                radius = size.width * 0.60f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(TealGlow.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.45f, size.height * 0.82f),
                    radius = size.width * 0.70f
                ),
                center = Offset(size.width * 0.45f, size.height * 0.82f),
                radius = size.width * 0.70f
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illuminated App Emblem
                Surface(
                    shape = RoundedCornerShape(32.dp),
                    border = BorderStroke(
                        2.dp,
                        Brush.linearGradient(listOf(SkyCyan, VioletGlow, RoseAccent))
                    ),
                    shadowElevation = 24.dp,
                    color = AuroraBgMid,
                    modifier = Modifier.size(110.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "VideoChat Pro logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.ExtraBold)) {
                            append("VideoChat ")
                        }
                        withStyle(
                            SpanStyle(
                                brush = Brush.horizontalGradient(listOf(SkyCyan, VioletGlow, RoseAccent)),
                                fontWeight = FontWeight.ExtraBold
                            )
                        ) {
                            append("Pro")
                        }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.07f),
                    border = BorderStroke(1.dp, SkyCyan.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "Connect • Chat • Video Call",
                        style = MaterialTheme.typography.labelLarge,
                        color = SkyCyan,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Styled HD Live Video Calls Showcase Card
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent,
                    border = BorderStroke(
                        1.2.dp,
                        Brush.linearGradient(
                            listOf(SkyCyan.copy(alpha = 0.55f), RoseAccent.copy(alpha = 0.45f))
                        )
                    ),
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(FeatureCardStart, FeatureCardEnd))
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(SkyCyan.copy(alpha = 0.28f), VioletPrimary.copy(alpha = 0.36f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Videocam,
                                contentDescription = null,
                                tint = SkyCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HD Live Video Calls",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Instant face-to-face connections with beauty lighting",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BadgeRed.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, BadgeRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    enabled = !isLoading && !isCheckingSession,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = AuroraBgMid
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(SkyCyan.copy(alpha = 0.75f), RoseAccent.copy(alpha = 0.65f))
                        )
                    ),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("google_sign_in_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(ElectricBlue, VioletPrimary, RoseAccent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading || isCheckingSession) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isCheckingSession) "Checking session..." else "Signing in...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = GoldStart,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Sign in with Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
