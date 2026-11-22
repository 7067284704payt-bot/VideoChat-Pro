package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Creator
import com.example.ui.theme.CardSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary

@Composable
fun CreatorCard(
    creator: Creator,
    onCardClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GlassBorder),
        color = CardSurface,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.92f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onCardClick)
            .testTag("creator_card_${creator.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = creator.avatarRes),
                contentDescription = "${creator.name} portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Bottom gradient scrim for readable name & action pills
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x9907081C),
                                Color(0xEE07081C)
                            )
                        )
                    )
                    .padding(top = 36.dp, bottom = 8.dp, start = 10.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = creator.countryFlag,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (creator.badgeEmoji.isNotEmpty()) "${creator.name} ${creator.badgeEmoji}" else creator.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Video call circular pill
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(VioletGlow, VioletPrimary))
                                )
                                .clickable(onClick = onVideoCallClick)
                                .testTag("call_creator_${creator.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Videocam,
                                contentDescription = "Video call ${creator.name}",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Chat circular pill
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(SkyCyan, ElectricBlue))
                                )
                                .clickable(onClick = onChatClick)
                                .testTag("chat_creator_${creator.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ChatBubble,
                                contentDescription = "Chat with ${creator.name}",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Top-left viewer counter pill
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0x8A0B0D24),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = creator.viewerCount.toString(),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Top-right green online dot
            if (creator.online) {
                Surface(
                    shape = CircleShape,
                    color = OnlineGreen,
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(14.dp)
                ) {}
            }
        }
    }
}
