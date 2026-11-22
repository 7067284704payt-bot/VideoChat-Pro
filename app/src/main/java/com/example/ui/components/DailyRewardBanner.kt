package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldDarkText
import com.example.ui.theme.GoldEnd
import com.example.ui.theme.GoldStart
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.RewardTimerState

@Composable
fun DailyRewardBanner(
    rewardTimer: RewardTimerState,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.2.dp, SkyCyan.copy(alpha = 0.55f)),
        shadowElevation = 10.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(126.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_daily_reward_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle dark radial/vertical overlay for crisp text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xCC0A0D35),
                                Color(0x9912134A),
                                Color(0x881A104B)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Crowned Diamond Artwork
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, GoldStart.copy(alpha = 0.65f)),
                    color = Color(0xFF12103A),
                    modifier = Modifier.size(84.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_diamond_crown),
                        contentDescription = "Daily Reward Diamond",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Center: Daily Reward Title + 10,000 Diamonds + Timer Pill
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Daily Reward",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = GoldStart, fontWeight = FontWeight.ExtraBold)) {
                                append("10,000 ")
                            }
                            withStyle(SpanStyle(color = TextPrimary, fontWeight = FontWeight.Bold)) {
                                append("Diamonds")
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 18.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = GoldEnd.copy(alpha = 0.92f),
                        modifier = Modifier.clip(RoundedCornerShape(50))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = GoldDarkText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (rewardTimer.canClaim) "Ready: 10,000 💎" else "Next in: ${rewardTimer.formattedCountdown}",
                                color = GoldDarkText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Golden Get Now > Button
                Surface(
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.2.dp, Color(0xFFFFF3B0)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = rewardTimer.canClaim, onClick = onClaimClick)
                        .testTag("claim_daily_reward_button")
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (rewardTimer.canClaim) {
                                    Brush.verticalGradient(listOf(GoldStart, GoldEnd))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            OnlineGreen.copy(alpha = 0.25f),
                                            OnlineGreen.copy(alpha = 0.35f)
                                        )
                                    )
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (rewardTimer.canClaim) Icons.Rounded.CardGiftcard else Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (rewardTimer.canClaim) Color(0xFFB91C1C) else OnlineGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (rewardTimer.canClaim) "Get Now" else "Claimed",
                                color = if (rewardTimer.canClaim) GoldDarkText else TextPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (rewardTimer.canClaim) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = GoldDarkText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
