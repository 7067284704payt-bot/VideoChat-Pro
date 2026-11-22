package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainTab

@Composable
fun BottomNavBar(
    selectedTab: MainTab,
    unreadChatCount: Int,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MidnightIndigo.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, GlassBorder),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val icon = when (tab) {
                    MainTab.HOME -> if (isSelected) Icons.Rounded.Home else Icons.Outlined.Home
                    MainTab.DISCOVER -> if (isSelected) Icons.Rounded.Explore else Icons.Outlined.Explore
                    MainTab.CHAT -> if (isSelected) Icons.Rounded.ChatBubble else Icons.Outlined.ChatBubbleOutline
                    MainTab.CALLS -> if (isSelected) Icons.Rounded.Call else Icons.Outlined.Call
                    MainTab.PROFILE -> if (isSelected) Icons.Rounded.Person else Icons.Outlined.Person
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(24.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        listOf(VioletGlow, VioletPrimary)
                                    )
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .testTag("nav_tab_${tab.route}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) TextPrimary else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (tab == MainTab.CHAT && unreadChatCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 8.dp, y = (-5).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(BadgeRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadChatCount > 9) "9+" else unreadChatCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
