package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallRecord
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.data.model.GiftOption
import com.example.data.model.UserProfile
import com.example.data.repository.VideoChatRepository
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class MainTab(val route: String, val label: String) {
    HOME("home", "Home"),
    DISCOVER("discover", "Discover"),
    CHAT("chat", "Chat"),
    CALLS("calls", "Calls"),
    PROFILE("profile", "Profile")
}

data class ActiveCallSession(
    val callId: String,
    val creator: Creator,
    val callType: String,
    val elapsedSeconds: Long = 0L,
    val diamondsSpent: Long = 0L,
    val micMuted: Boolean = false,
    val cameraEnabled: Boolean = true,
    val frontCamera: Boolean = true,
    val beautyEnabled: Boolean = true,
    val lastGiftSent: GiftOption? = null
)

data class RewardTimerState(
    val canClaim: Boolean = true,
    val formattedCountdown: String = "Ready!"
)

class VideoChatViewModel(
    private val repository: VideoChatRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _selectedCountry = MutableStateFlow("All")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _activeChatCreator = MutableStateFlow<Creator?>(null)
    val activeChatCreator: StateFlow<Creator?> = _activeChatCreator.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCallSession?>(null)
    val activeCall: StateFlow<ActiveCallSession?> = _activeCall.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _rewardTimer = MutableStateFlow(RewardTimerState())
    val rewardTimer: StateFlow<RewardTimerState> = _rewardTimer.asStateFlow()

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing user profile", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load profile"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val conversationsState: StateFlow<UiState<List<Conversation>>> =
        repository.observeConversations(currentUserId)
            .map<List<Conversation>, UiState<List<Conversation>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing conversations", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load conversations"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val callsState: StateFlow<UiState<List<CallRecord>>> =
        repository.observeCalls(currentUserId)
            .map<List<CallRecord>, UiState<List<CallRecord>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing call history", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load call history"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeMessagesState: StateFlow<UiState<List<ChatMessage>>> =
        _activeChatCreator
            .flatMapLatest { creator ->
                if (creator == null) {
                    flowOf<UiState<List<ChatMessage>>>(UiState.Success(emptyList()))
                } else {
                    repository.observeMessages(currentUserId, creator.id)
                        .map<List<ChatMessage>, UiState<List<ChatMessage>>> { UiState.Success(it) }
                        .catch { error ->
                            val firestoreError = generateSequence(error) { it.cause }
                                .filterIsInstance<FirebaseFirestoreException>()
                                .firstOrNull()
                            if (firestoreError == null && error is CancellationException) throw error
                            val rootError = firestoreError ?: error
                            Log.w(TAG, "Error observing messages", rootError)
                            emit(UiState.Error(rootError.message ?: "Failed to load chat"))
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Success(emptyList())
            )

    init {
        initializeProfileIfNeeded()
        startRewardCountdownTicker()
        startCallTicker()
    }

    private fun initializeProfileIfNeeded() {
        viewModelScope.launch {
            val user = Firebase.auth.currentUser
            val name = user?.displayName ?: "VideoChat User"
            val photo = user?.photoUrl?.toString()
            repository.ensureUserProfileInitialized(currentUserId, name, photo)
        }
    }

    private fun startRewardCountdownTicker() {
        viewModelScope.launch {
            while (isActive) {
                val profile = (userProfileState.value as? UiState.Success)?.data
                val lastClaim = profile?.lastRewardClaimedAt
                if (lastClaim == null) {
                    _rewardTimer.value = RewardTimerState(canClaim = true, formattedCountdown = "23:59:47")
                } else {
                    val remaining = (86400L - (Timestamp.now().seconds - lastClaim.seconds)).coerceAtLeast(0L)
                    if (remaining == 0L) {
                        _rewardTimer.value = RewardTimerState(canClaim = true, formattedCountdown = "Ready Now!")
                    } else {
                        val h = remaining / 3600
                        val m = (remaining % 3600) / 60
                        val s = remaining % 60
                        _rewardTimer.value = RewardTimerState(
                            canClaim = false,
                            formattedCountdown = String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
                        )
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun startCallTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val current = _activeCall.value
                if (current != null) {
                    _activeCall.update { session ->
                        session?.copy(elapsedSeconds = session.elapsedSeconds + 1L)
                    }
                }
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _selectedTab.value = tab
    }

    fun selectCountry(country: String) {
        _selectedCountry.value = country
    }

    fun toggleSearch() {
        val next = !_isSearchVisible.value
        _isSearchVisible.value = next
        if (!next) _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openNotifications(open: Boolean) {
        _showNotifications.value = open
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val result = repository.claimDailyReward(currentUserId, 10000L)
            result.onSuccess { newBalance ->
                _statusBanner.value = "+10,000 Daily Diamonds claimed! Balance: ${formatNumber(newBalance)}"
            }.onFailure { err ->
                _statusBanner.value = err.message ?: "Unable to claim daily reward right now"
            }
        }
    }

    fun openChatWith(creator: Creator) {
        _activeChatCreator.value = creator
        viewModelScope.launch {
            repository.openOrCreateConversation(currentUserId, creator)
        }
    }

    fun closeChatThread() {
        _activeChatCreator.value = null
    }

    fun sendChatMessage(creator: Creator, text: String, gift: GiftOption? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && gift == null) return
        val messageBody = if (gift != null) {
            if (trimmed.isEmpty()) "Sent ${gift.emoji} ${gift.label} (${gift.diamonds} Diamonds)"
            else "${gift.emoji} $trimmed"
        } else {
            trimmed
        }
        viewModelScope.launch {
            val result = repository.sendMessage(
                userId = currentUserId,
                creator = creator,
                text = messageBody,
                giftDiamonds = gift?.diamonds ?: 0L
            )
            result.onFailure { err ->
                _statusBanner.value = err.message ?: "Could not send message"
            }
        }
    }

    fun toggleFavorite(creator: Creator) {
        viewModelScope.launch {
            repository.toggleFavoriteCreator(currentUserId, creator).onSuccess { isFav ->
                _statusBanner.value = if (isFav) "${creator.name} added to Favorites" else "${creator.name} removed from Favorites"
            }
        }
    }

    fun startVideoCall(creator: Creator, callType: String = "VIDEO") {
        val profile = (userProfileState.value as? UiState.Success)?.data
        viewModelScope.launch {
            val result = repository.startCall(currentUserId, creator, callType)
            result.onSuccess { callId ->
                _activeCall.value = ActiveCallSession(
                    callId = callId,
                    creator = creator,
                    callType = callType,
                    beautyEnabled = profile?.beautyFilterEnabled ?: true
                )
            }.onFailure { err ->
                _statusBanner.value = err.message ?: "Unable to start call"
            }
        }
    }

    fun toggleCallMic() {
        _activeCall.update { it?.copy(micMuted = !it.micMuted) }
    }

    fun toggleCallCamera() {
        _activeCall.update { it?.copy(cameraEnabled = !it.cameraEnabled) }
    }

    fun flipCallCamera() {
        _activeCall.update { it?.copy(frontCamera = !it.frontCamera) }
    }

    fun toggleCallBeauty() {
        _activeCall.update { it?.copy(beautyEnabled = !it.beautyEnabled) }
    }

    fun sendGiftInCall(gift: GiftOption) {
        val session = _activeCall.value ?: return
        viewModelScope.launch {
            val result = repository.spendDiamondsInCall(currentUserId, gift.diamonds)
            result.onSuccess {
                _activeCall.update {
                    it?.copy(
                        diamondsSpent = it.diamondsSpent + gift.diamonds,
                        lastGiftSent = gift
                    )
                }
                _statusBanner.value = "Sent ${gift.emoji} ${gift.label} to ${session.creator.name}!"
            }.onFailure { err ->
                _statusBanner.value = err.message ?: "Not enough diamonds!"
            }
        }
    }

    fun endActiveCall() {
        val session = _activeCall.value ?: return
        _activeCall.value = null
        viewModelScope.launch {
            repository.endCall(
                userId = currentUserId,
                callId = session.callId,
                durationSeconds = session.elapsedSeconds,
                diamondsSpent = session.diamondsSpent
            )
            _statusBanner.value = "Call with ${session.creator.name} ended (${formatDuration(session.elapsedSeconds)})"
        }
    }

    fun deleteCall(callId: String) {
        viewModelScope.launch {
            repository.deleteCallRecord(currentUserId, callId)
        }
    }

    fun updateProfile(
        displayName: String,
        country: String,
        bio: String,
        beautyFilterEnabled: Boolean,
        hdVideoEnabled: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.updateProfileSettings(
                userId = currentUserId,
                displayName = displayName,
                country = country,
                bio = bio,
                beautyFilterEnabled = beautyFilterEnabled,
                hdVideoEnabled = hdVideoEnabled
            )
            result.onSuccess {
                _statusBanner.value = "Profile saved"
            }.onFailure { err ->
                _statusBanner.value = err.message ?: "Failed to save profile"
            }
        }
    }

    companion object {
        private const val TAG = "VideoChatVM"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun formatNumber(value: Long): String = String.format(Locale.US, "%,d", value)

        fun formatDuration(seconds: Long): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }
}
