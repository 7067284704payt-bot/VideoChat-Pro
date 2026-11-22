package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.data.model.GiftCatalog
import com.example.data.model.GiftOption
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DeepSpaceBg
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldStart
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.UiState

@Composable
fun ChatListScreen(
    conversationsState: UiState<List<Conversation>>,
    searchQuery: String,
    onOpenChat: (Creator) -> Unit,
    onStartVideoCall: (Creator) -> Unit,
    modifier: Modifier = Modifier
) {
    when (conversationsState) {
        is UiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VioletGlow)
            }
        }
        is UiState.Error -> {
            Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(conversationsState.message, color = BadgeRed, style = MaterialTheme.typography.bodyLarge)
            }
        }
        is UiState.Success -> {
            val q = searchQuery.trim()
            val conversations = remember(conversationsState.data, q) {
                if (q.isEmpty()) conversationsState.data
                else conversationsState.data.filter {
                    it.creatorName.contains(q, ignoreCase = true) ||
                        it.lastMessage.contains(q, ignoreCase = true)
                }
            }

            if (conversations.isEmpty()) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = null,
                            tint = VioletGlow,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No conversations yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the blue chat icon on any creator card to start messaging.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = modifier.fillMaxSize()
                ) {
                    items(conversations, key = { it.creatorId }) { conv ->
                        val creator = CreatorCatalog.findById(conv.creatorId)
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardSurface,
                            border = BorderStroke(
                                1.dp,
                                if (conv.unreadCount > 0) VioletGlow.copy(alpha = 0.55f) else GlassBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    if (creator != null) onOpenChat(creator)
                                }
                                .testTag("conversation_item_${conv.creatorId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box {
                                    if (creator != null) {
                                        Image(
                                            painter = painterResource(id = creator.avatarRes),
                                            contentDescription = conv.creatorName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(VioletPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(conv.creatorName.take(1), color = TextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(13.dp)
                                            .clip(CircleShape)
                                            .background(OnlineGreen)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${CreatorCatalog.countryFlagFor(conv.creatorCountry)} ${conv.creatorName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimary
                                        )
                                        if (conv.favorite) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(Icons.Rounded.Favorite, null, tint = BadgeRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = conv.lastMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (conv.unreadCount > 0) TextPrimary else TextSecondary,
                                        fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                if (conv.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(BadgeRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = conv.unreadCount.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                if (creator != null) {
                                    Box(
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(VioletPrimary.copy(alpha = 0.25f))
                                            .clickable { onStartVideoCall(creator) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Videocam,
                                            contentDescription = "Call ${conv.creatorName}",
                                            tint = VioletGlow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key = "chat_list_ad_inline") {
                        AdsterraBannerCard(
                            unit = AdsterraAdUnit.InlineScript,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatThreadScreen(
    creator: Creator,
    messagesState: UiState<List<ChatMessage>>,
    userDiamonds: Long,
    onBack: () -> Unit,
    onStartVideoCall: () -> Unit,
    onSendMessage: (String, GiftOption?) -> Unit
) {
    BackHandler(onBack = onBack)

    var draft by remember { mutableStateOf("") }
    var showGiftTray by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val quickPrompts = remember(creator.id) {
        listOf("Hey ${creator.name}! 👋", "Want to video call?", "Where are you streaming from?")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepSpaceBg, MidnightIndigo)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Thread Header
            Surface(
                color = MidnightIndigo.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Image(
                        painter = painterResource(id = creator.avatarRes),
                        contentDescription = creator.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${creator.countryFlag} ${creator.name} ${creator.badgeEmoji}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Online • ${creator.tagline}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnlineGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = VioletPrimary,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onStartVideoCall)
                            .testTag("chat_header_video_call")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Videocam, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                        }
                    }
                }
            }

            // Message List
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (messagesState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(color = VioletGlow, modifier = Modifier.align(Alignment.Center))
                    }
                    is UiState.Error -> {
                        Text(
                            text = messagesState.message,
                            color = BadgeRed,
                            modifier = Modifier.align(Alignment.Center).padding(16.dp)
                        )
                    }
                    is UiState.Success -> {
                        val messages = messagesState.data
                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                        }

                        if (messages.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Say hello to ${creator.name}!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Send a message or diamond gift to break the ice.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(messages, key = { it.id.ifEmpty { it.hashCode().toString() } }) { msg ->
                                    val isUser = msg.sender == "USER"
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(
                                                topStart = 18.dp,
                                                topEnd = 18.dp,
                                                bottomStart = if (isUser) 18.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 18.dp
                                            ),
                                            color = Color.Transparent,
                                            border = if (msg.giftDiamonds > 0) BorderStroke(1.dp, GoldStart) else null,
                                            modifier = Modifier.widthIn(max = 280.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isUser) {
                                                            Brush.horizontalGradient(listOf(VioletPrimary, ElectricBlue))
                                                        } else {
                                                            Brush.horizontalGradient(listOf(CardSurfaceElevated, CardSurfaceElevated))
                                                        }
                                                    )
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = msg.text,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = TextPrimary
                                                    )
                                                    if (msg.giftDiamonds > 0) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Rounded.Diamond, null, tint = SkyCyan, modifier = Modifier.size(14.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "${msg.giftDiamonds} Diamonds Gift",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = GoldStart
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
                }
            }

            // Quick Icebreakers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardSurface,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSendMessage(prompt, null) }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Gift Tray
            AnimatedVisibility(visible = showGiftTray) {
                Surface(
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Send Diamond Gift", style = MaterialTheme.typography.labelLarge, color = GoldStart)
                            Text("Balance: $userDiamonds 💎", style = MaterialTheme.typography.labelSmall, color = SkyCyan)
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
                                    border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            onSendMessage("", gift)
                                            showGiftTray = false
                                        }
                                        .testTag("gift_option_${gift.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(gift.emoji, fontSize = 22.sp)
                                        Text(gift.label, style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                                        Text("${gift.diamonds} 💎", style = MaterialTheme.typography.labelSmall, color = GoldStart)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightIndigo)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showGiftTray = !showGiftTray },
                    modifier = Modifier.testTag("toggle_gift_tray_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CardGiftcard,
                        contentDescription = "Send Gift",
                        tint = if (showGiftTray) GoldStart else VioletGlow
                    )
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Type a message...", color = TextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VioletGlow,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = CardSurface,
                        unfocusedContainerColor = CardSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_message_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (draft.isNotBlank()) {
                            onSendMessage(draft, null)
                            draft = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(VioletPrimary, ElectricBlue)))
                        .testTag("send_chat_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}
