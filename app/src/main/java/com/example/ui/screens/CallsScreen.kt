package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CallRecord
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldStart
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.VideoChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CallsScreen(
    callsState: UiState<List<CallRecord>>,
    onRedialCreator: (Creator) -> Unit,
    onDeleteCall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }

    when (callsState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VioletGlow)
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(callsState.message, color = BadgeRed, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is UiState.Success -> {
            val calls = callsState.data
            if (calls.isEmpty()) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AdsterraBannerCard(
                        unit = AdsterraAdUnit.Banner468x60,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = null,
                            tint = VioletGlow,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No call history yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start a live video call from Home or Discover to see your history here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = modifier.fillMaxSize()
                ) {
                    item(key = "calls_ad_468x60") {
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.Banner468x60,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    items(calls, key = { it.id.ifEmpty { it.hashCode().toString() } }) { call ->
                        val creator = CreatorCatalog.findById(call.creatorId)
                        val timeLabel = call.createdAt?.toDate()?.let { dateFormatter.format(it) } ?: "Just now"

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardSurface,
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("call_history_item_${call.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (creator != null) {
                                    Image(
                                        painter = painterResource(id = creator.avatarRes),
                                        contentDescription = call.creatorName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(VioletPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.Call, null, tint = TextPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${CreatorCatalog.countryFlagFor(call.creatorCountry)} ${call.creatorName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${call.callType} • ${VideoChatViewModel.formatDuration(call.durationSeconds)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnlineGreen
                                        )
                                        Text(
                                            text = timeLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    if (call.diamondsSpent > 0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.Diamond, null, tint = SkyCyan, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${call.diamondsSpent} gifted",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldStart
                                            )
                                        }
                                    }
                                }

                                if (call.id.isNotBlank()) {
                                    IconButton(onClick = { onDeleteCall(call.id) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete call record",
                                            tint = TextSecondary
                                        )
                                    }
                                }

                                if (creator != null) {
                                    Box(
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(VioletPrimary)
                                            .clickable { onRedialCreator(creator) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Videocam,
                                            contentDescription = "Video call ${call.creatorName}",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
