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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldStart
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.RewardTimerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    conversations: List<Conversation>,
    rewardTimer: RewardTimerState,
    onClaimReward: () -> Unit,
    onOpenChat: (Creator) -> Unit,
    onDismiss: () -> Unit
) {
    val unreadThreads = conversations.filter { it.unreadCount > 0L }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MidnightIndigo,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = VioletGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            if (rewardTimer.canClaim) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GoldStart.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClaimReward()
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CardGiftcard,
                            contentDescription = null,
                            tint = GoldStart,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Reward Ready!",
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldStart
                            )
                            Text(
                                text = "Tap to claim 10,000 bonus Diamonds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            if (unreadThreads.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You're all caught up on messages!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                unreadThreads.forEach { conv ->
                    val creator = CreatorCatalog.findById(conv.creatorId)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (creator != null) {
                                    onDismiss()
                                    onOpenChat(creator)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (creator != null) {
                                Image(
                                    painter = painterResource(id = creator.avatarRes),
                                    contentDescription = creator.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${conv.creatorName} sent a message",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = conv.lastMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Brush.horizontalGradient(listOf(VioletPrimary, ElectricBlue)))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Reply",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
