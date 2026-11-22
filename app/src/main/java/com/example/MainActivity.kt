package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.signOut
import com.example.data.repository.VideoChatRepository
import com.example.ui.components.AdsterraAdUnit
import com.example.ui.components.AdsterraBannerCard
import com.example.ui.components.AdsterraStickyBannerBar
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NotificationsSheet
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CallsScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VideoCallScreen
import com.example.ui.theme.BadgeRed
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.DeepSpaceBg
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VideoChatProTheme
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.VioletPrimary
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.UiState
import com.example.ui.viewmodel.VideoChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            VideoChatProTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        key(user.uid) {
            MainAppShell(currentUserId = user.uid)
        }
    }
}

@Composable
fun MainAppShell(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: VideoChatViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                VideoChatViewModel(VideoChatRepository(db), currentUserId)
            }
        }
    )

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchVisible by viewModel.isSearchVisible.collectAsStateWithLifecycle()
    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val conversationsState by viewModel.conversationsState.collectAsStateWithLifecycle()
    val callsState by viewModel.callsState.collectAsStateWithLifecycle()
    val activeMessagesState by viewModel.activeMessagesState.collectAsStateWithLifecycle()
    val activeChatCreator by viewModel.activeChatCreator.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val rewardTimer by viewModel.rewardTimer.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        val message = statusBanner
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearStatusBanner()
        }
    }

    val userDiamonds = (userProfileState as? UiState.Success)?.data?.diamonds ?: 10000L
    val conversations = (conversationsState as? UiState.Success)?.data ?: emptyList()
    val unreadCount = conversations.sumOf { it.unreadCount }.toInt()

    // Navigate back to Home tab if on a secondary tab when pressing Back
    BackHandler(enabled = selectedTab != MainTab.HOME && activeChatCreator == null && activeCall == null) {
        viewModel.selectTab(MainTab.HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepSpaceBg, MidnightIndigo, Color(0xFF140D34))
                )
            )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 840.dp

            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = CardSurfaceElevated,
                            contentColor = TextPrimary
                        )
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AdsterraStickyBannerBar(unit = AdsterraAdUnit.Banner320x50)
                            BottomNavBar(
                                selectedTab = selectedTab,
                                unreadChatCount = unreadCount,
                                onSelectTab = viewModel::selectTab
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MidnightIndigo.copy(alpha = 0.96f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            MainTab.entries.forEach { tab ->
                                val isSelected = tab == selectedTab
                                val icon = when (tab) {
                                    MainTab.HOME -> if (isSelected) Icons.Rounded.Home else Icons.Outlined.Home
                                    MainTab.DISCOVER -> if (isSelected) Icons.Rounded.Explore else Icons.Outlined.Explore
                                    MainTab.CHAT -> if (isSelected) Icons.Rounded.ChatBubble else Icons.Outlined.ChatBubbleOutline
                                    MainTab.CALLS -> if (isSelected) Icons.Rounded.Call else Icons.Outlined.Call
                                    MainTab.PROFILE -> if (isSelected) Icons.Rounded.Person else Icons.Outlined.Person
                                }
                                NavigationRailItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        if (tab == MainTab.CHAT && unreadCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = BadgeRed) {
                                                        Text(unreadCount.toString(), color = Color.White)
                                                    }
                                                }
                                            ) {
                                                Icon(icon, contentDescription = tab.label)
                                            }
                                        } else {
                                            Icon(icon, contentDescription = tab.label)
                                        }
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = TextPrimary,
                                        selectedTextColor = VioletGlow,
                                        indicatorColor = VioletPrimary,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        TopHeaderBar(
                            diamonds = userDiamonds,
                            unreadCount = unreadCount + (if (rewardTimer.canClaim) 1 else 0),
                            isSearchVisible = isSearchVisible,
                            searchQuery = searchQuery,
                            onNotificationsClick = { viewModel.openNotifications(true) },
                            onSearchToggle = viewModel::toggleSearch,
                            onSearchQueryChange = viewModel::updateSearchQuery
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTab) {
                                MainTab.HOME -> HomeScreen(
                                    selectedCountry = selectedCountry,
                                    searchQuery = searchQuery,
                                    rewardTimer = rewardTimer,
                                    conversations = conversations,
                                    onSelectCountry = viewModel::selectCountry,
                                    onClaimDailyReward = viewModel::claimDailyReward,
                                    onStartVideoCall = { creator -> viewModel.startVideoCall(creator) },
                                    onOpenChat = { creator -> viewModel.openChatWith(creator) },
                                    onToggleFavorite = { creator -> viewModel.toggleFavorite(creator) }
                                )

                                MainTab.DISCOVER -> DiscoverScreen(
                                    conversations = conversations,
                                    searchQuery = searchQuery,
                                    onStartVideoCall = { creator -> viewModel.startVideoCall(creator) },
                                    onOpenChat = { creator -> viewModel.openChatWith(creator) }
                                )

                                MainTab.CHAT -> ChatListScreen(
                                    conversationsState = conversationsState,
                                    searchQuery = searchQuery,
                                    onOpenChat = { creator -> viewModel.openChatWith(creator) },
                                    onStartVideoCall = { creator -> viewModel.startVideoCall(creator) }
                                )

                                MainTab.CALLS -> CallsScreen(
                                    callsState = callsState,
                                    onRedialCreator = { creator -> viewModel.startVideoCall(creator) },
                                    onDeleteCall = { callId -> viewModel.deleteCall(callId) }
                                )

                                MainTab.PROFILE -> ProfileScreen(
                                    userProfileState = userProfileState,
                                    rewardTimer = rewardTimer,
                                    onClaimDailyReward = viewModel::claimDailyReward,
                                    onSaveProfile = viewModel::updateProfile,
                                    onSignOut = {
                                        signOut(
                                            credentialManager = credentialManager,
                                            scope = scope
                                        )
                                    }
                                )
                            }
                        }

                        if (useRail) {
                            AdsterraStickyBannerBar(
                                unit = AdsterraAdUnit.Banner320x50,
                                modifier = Modifier.navigationBarsPadding()
                            )
                        }
                    }

                    if (useRail) {
                        Column(
                            modifier = Modifier
                                .width(192.dp)
                                .fillMaxHeight()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdsterraBannerCard(unit = AdsterraAdUnit.Banner160x600)
                        }
                    }
                }
            }
        }

        // Fullscreen Chat Thread Overlay
        activeChatCreator?.let { creator ->
            ChatThreadScreen(
                creator = creator,
                messagesState = activeMessagesState,
                userDiamonds = userDiamonds,
                onBack = viewModel::closeChatThread,
                onStartVideoCall = { viewModel.startVideoCall(creator) },
                onSendMessage = { text, gift -> viewModel.sendChatMessage(creator, text, gift) }
            )
        }

        // Fullscreen Live Video Call Overlay
        activeCall?.let { session ->
            VideoCallScreen(
                session = session,
                userDiamonds = userDiamonds,
                onToggleMic = viewModel::toggleCallMic,
                onToggleCamera = viewModel::toggleCallCamera,
                onFlipCamera = viewModel::flipCallCamera,
                onToggleBeauty = viewModel::toggleCallBeauty,
                onSendGift = viewModel::sendGiftInCall,
                onEndCall = viewModel::endActiveCall
            )
        }

        if (showNotifications) {
            NotificationsSheet(
                conversations = conversations,
                rewardTimer = rewardTimer,
                onClaimReward = viewModel::claimDailyReward,
                onOpenChat = viewModel::openChatWith,
                onDismiss = { viewModel.openNotifications(false) }
            )
        }
    }
}
