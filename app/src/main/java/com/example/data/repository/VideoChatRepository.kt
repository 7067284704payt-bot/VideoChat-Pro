package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.CallRecord
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.Creator
import com.example.data.model.CreatorCatalog
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class VideoChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "Signed-in user changed; refusing cross-user operation." }
        }
        return uid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeConversations(userId: String): Flow<List<Conversation>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/conversations"
        emitAll(
            db.collection("users").document(uid).collection("conversations")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMessages(userId: String, creatorId: String): Flow<List<ChatMessage>> = flow {
        val uid = requireUserId(userId)
        require(creatorId.isNotBlank()) { "Creator ID must not be blank" }
        val path = "users/$uid/conversations/$creatorId/messages"
        emitAll(
            db.collection("users").document(uid)
                .collection("conversations").document(creatorId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeCalls(userId: String): Flow<List<CallRecord>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/calls"
        emitAll(
            db.collection("users").document(uid).collection("calls")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(CallRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfileInitialized(
        userId: String,
        displayName: String,
        photoUrl: String?
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userDocRef = db.collection("users").document(uid)
            val existing = userDocRef.get().await()
            if (!existing.exists()) {
                val safeName = displayName.trim().take(50).ifBlank { "VideoChat User" }
                val safePhoto = photoUrl?.trim()?.take(500)?.takeIf { it.isNotEmpty() }
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "photoUrl" to safePhoto,
                    "country" to "India",
                    "bio" to "Connecting live on VideoChat Pro",
                    "diamonds" to 10000L,
                    "beautyFilterEnabled" to true,
                    "hdVideoEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ).filterValues { it != null }
                userDocRef.set(payload).await()
                seedStarterConversations(uid)
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    private suspend fun seedStarterConversations(uid: String) {
        val starters = listOf(
            "priya" to "Hey there! Just went live—want to hop on a quick video chat?",
            "anaya" to "Grabbing coffee right now, say hi anytime!",
            "neha" to "Welcome to VideoChat Pro! Love meeting new friends here."
        )
        val batch = db.batch()
        starters.forEach { (creatorId, greeting) ->
            val creator = CreatorCatalog.findById(creatorId) ?: return@forEach
            val convRef = db.collection("users").document(uid)
                .collection("conversations").document(creator.id)
            batch.set(
                convRef,
                mapOf(
                    "userId" to uid,
                    "creatorId" to creator.id,
                    "creatorName" to creator.name,
                    "creatorCountry" to creator.country,
                    "lastMessage" to greeting,
                    "unreadCount" to 1L,
                    "favorite" to (creator.id == "priya"),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val msgRef = convRef.collection("messages").document()
            batch.set(
                msgRef,
                mapOf(
                    "userId" to uid,
                    "creatorId" to creator.id,
                    "sender" to "CREATOR",
                    "text" to greeting,
                    "giftDiamonds" to 0L,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
        }
        batch.commit().await()
    }

    suspend fun claimDailyReward(userId: String, rewardAmount: Long = 10000L): Result<Long> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val newBalance = db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val currentDiamonds = snap.getLong("diamonds") ?: 0L
                val lastClaim = snap.getTimestamp("lastRewardClaimedAt")
                val nowSeconds = Timestamp.now().seconds
                if (lastClaim != null && (nowSeconds - lastClaim.seconds) < 86400L) {
                    throw IllegalStateException("Daily reward already claimed. Come back after cooldown.")
                }
                val updated = (currentDiamonds + rewardAmount).coerceAtMost(100_000_000L)
                tx.update(
                    userRef,
                    mapOf(
                        "diamonds" to updated,
                        "lastRewardClaimedAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                updated
            }.await()
            Result.success(newBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileSettings(
        userId: String,
        displayName: String,
        country: String,
        bio: String?,
        beautyFilterEnabled: Boolean,
        hdVideoEnabled: Boolean
    ): Result<Unit> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanName = displayName.trim().take(50)
            val cleanCountry = country.trim().take(40)
            require(cleanName.isNotEmpty()) { "Display name cannot be empty." }
            require(cleanCountry.isNotEmpty()) { "Country cannot be empty." }
            val cleanBio = bio?.trim()?.take(200)
            val updateMap = mutableMapOf<String, Any>(
                "displayName" to cleanName,
                "country" to cleanCountry,
                "beautyFilterEnabled" to beautyFilterEnabled,
                "hdVideoEnabled" to hdVideoEnabled,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanBio != null) {
                updateMap["bio"] = cleanBio
            }
            db.collection("users").document(uid).update(updateMap).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun openOrCreateConversation(userId: String, creator: Creator): Result<Unit> {
        val path = "users/$userId/conversations/${creator.id}"
        return try {
            val uid = requireUserId(userId)
            val convRef = db.collection("users").document(uid)
                .collection("conversations").document(creator.id)
            val snap = convRef.get().await()
            if (!snap.exists()) {
                convRef.set(
                    mapOf(
                        "userId" to uid,
                        "creatorId" to creator.id,
                        "creatorName" to creator.name.take(50),
                        "creatorCountry" to creator.country.take(40),
                        "lastMessage" to "Started a conversation with ${creator.name}",
                        "unreadCount" to 0L,
                        "favorite" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else if ((snap.getLong("unreadCount") ?: 0L) > 0L) {
                convRef.update(
                    mapOf(
                        "unreadCount" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun markConversationRead(userId: String, creatorId: String): Result<Unit> {
        val path = "users/$userId/conversations/$creatorId"
        return try {
            val uid = requireUserId(userId)
            db.collection("users").document(uid)
                .collection("conversations").document(creatorId)
                .update(
                    mapOf(
                        "unreadCount" to 0L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun toggleFavoriteCreator(userId: String, creator: Creator): Result<Boolean> {
        val path = "users/$userId/conversations/${creator.id}"
        return try {
            val uid = requireUserId(userId)
            val convRef = db.collection("users").document(uid)
                .collection("conversations").document(creator.id)
            val snap = convRef.get().await()
            val newFav = if (!snap.exists()) {
                convRef.set(
                    mapOf(
                        "userId" to uid,
                        "creatorId" to creator.id,
                        "creatorName" to creator.name.take(50),
                        "creatorCountry" to creator.country.take(40),
                        "lastMessage" to "Added ${creator.name} to favorites",
                        "unreadCount" to 0L,
                        "favorite" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                true
            } else {
                val current = snap.getBoolean("favorite") ?: false
                val next = !current
                convRef.update(
                    mapOf(
                        "favorite" to next,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                next
            }
            Result.success(newFav)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        userId: String,
        creator: Creator,
        text: String,
        giftDiamonds: Long = 0L
    ): Result<Unit> {
        val path = "users/$userId/conversations/${creator.id}/messages"
        return try {
            val uid = requireUserId(userId)
            val cleanText = text.trim().take(1000)
            require(cleanText.isNotEmpty()) { "Message cannot be empty" }
            require(giftDiamonds in 0L..100_000L) { "Invalid gift amount" }

            val userRef = db.collection("users").document(uid)
            val convRef = userRef.collection("conversations").document(creator.id)
            val msgRef = convRef.collection("messages").document()

            db.runTransaction { tx ->
                val userSnap = tx.get(userRef)
                val convSnap = tx.get(convRef)
                if (giftDiamonds > 0L) {
                    val currentDiamonds = userSnap.getLong("diamonds") ?: 0L
                    if (currentDiamonds < giftDiamonds) {
                        throw IllegalStateException("Not enough diamonds! Claim your free Daily Reward every 24 hours.")
                    }
                    tx.update(
                        userRef,
                        mapOf(
                            "diamonds" to (currentDiamonds - giftDiamonds),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                if (!convSnap.exists()) {
                    tx.set(
                        convRef,
                        mapOf(
                            "userId" to uid,
                            "creatorId" to creator.id,
                            "creatorName" to creator.name.take(50),
                            "creatorCountry" to creator.country.take(40),
                            "lastMessage" to cleanText.take(500),
                            "unreadCount" to 0L,
                            "favorite" to false,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                } else {
                    tx.update(
                        convRef,
                        mapOf(
                            "lastMessage" to cleanText.take(500),
                            "unreadCount" to 0L,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }

                tx.set(
                    msgRef,
                    mapOf(
                        "userId" to uid,
                        "creatorId" to creator.id,
                        "sender" to "USER",
                        "text" to cleanText,
                        "giftDiamonds" to giftDiamonds,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun startCall(userId: String, creator: Creator, callType: String = "VIDEO"): Result<String> {
        val path = "users/$userId/calls"
        return try {
            val uid = requireUserId(userId)
            val safeType = if (callType == "AUDIO") "AUDIO" else "VIDEO"
            val callRef = db.collection("users").document(uid).collection("calls").document()
            callRef.set(
                mapOf(
                    "userId" to uid,
                    "creatorId" to creator.id,
                    "creatorName" to creator.name.take(50),
                    "creatorCountry" to creator.country.take(40),
                    "callType" to safeType,
                    "status" to "CONNECTED",
                    "durationSeconds" to 0L,
                    "diamondsSpent" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(callRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun spendDiamondsInCall(userId: String, cost: Long): Result<Long> {
        val path = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            require(cost in 1L..100_000L) { "Invalid diamond amount" }
            val userRef = db.collection("users").document(uid)
            val newBalance = db.runTransaction { tx ->
                val snap = tx.get(userRef)
                val current = snap.getLong("diamonds") ?: 0L
                if (current < cost) {
                    throw IllegalStateException("Not enough diamonds!")
                }
                val updated = current - cost
                tx.update(
                    userRef,
                    mapOf(
                        "diamonds" to updated,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                updated
            }.await()
            Result.success(newBalance)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun endCall(
        userId: String,
        callId: String,
        durationSeconds: Long,
        diamondsSpent: Long
    ): Result<Unit> {
        val path = "users/$userId/calls/$callId"
        return try {
            val uid = requireUserId(userId)
            require(callId.isNotBlank()) { "Call ID cannot be blank" }
            val safeDuration = durationSeconds.coerceIn(0L, 86400L)
            val safeSpent = diamondsSpent.coerceIn(0L, 1_000_000L)
            db.collection("users").document(uid).collection("calls").document(callId)
                .update(
                    mapOf(
                        "status" to "COMPLETED",
                        "durationSeconds" to safeDuration,
                        "diamondsSpent" to safeSpent,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCallRecord(userId: String, callId: String): Result<Unit> {
        val path = "users/$userId/calls/$callId"
        return try {
            val uid = requireUserId(userId)
            require(callId.isNotBlank()) { "Call ID cannot be blank" }
            db.collection("users").document(uid).collection("calls").document(callId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }
}
