package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class Creator(
    val id: String,
    val name: String,
    val badgeEmoji: String,
    val country: String,
    val countryFlag: String,
    val viewerCount: Int,
    val online: Boolean,
    @DrawableRes val avatarRes: Int,
    val bio: String,
    val languages: List<String>,
    val diamondsPerMinute: Int,
    val tagline: String,
    val rating: Double
)

object CreatorCatalog {
    val primaryCountries = listOf("All", "India", "USA", "UK")
    val moreCountries = listOf("Pakistan", "Bangladesh", "Nepal", "Canada")

    val creators: List<Creator> = listOf(
        Creator(
            id = "priya",
            name = "Priya",
            badgeEmoji = "\uD83D\uDC97",
            country = "India",
            countryFlag = "\uD83C\uDDEE\uD83C\uDDF3",
            viewerCount = 42,
            online = true,
            avatarRes = R.drawable.img_creator_priya,
            bio = "Music lover, travel vlogger, and always up for bright conversations and acoustic songs.",
            languages = listOf("Hindi", "English"),
            diamondsPerMinute = 60,
            tagline = "Live acoustic & travel stories",
            rating = 4.9
        ),
        Creator(
            id = "anaya",
            name = "Anaya",
            badgeEmoji = "\uD83D\uDC51",
            country = "Pakistan",
            countryFlag = "\uD83C\uDDF5\uD83C\uDDF0",
            viewerCount = 18,
            online = true,
            avatarRes = R.drawable.img_creator_anaya,
            bio = "Design student who loves late-night coffee chats, cinema recommendations, and art.",
            languages = listOf("Urdu", "English"),
            diamondsPerMinute = 80,
            tagline = "Coffee & cinema chat",
            rating = 4.8
        ),
        Creator(
            id = "tina",
            name = "Tina",
            badgeEmoji = "\u2B50",
            country = "Bangladesh",
            countryFlag = "\uD83C\uDDE7\uD83C\uDDE9",
            viewerCount = 91,
            online = true,
            avatarRes = R.drawable.img_creator_tina,
            bio = "Dance enthusiast & foodie exploring street markets and sharing positive vibes.",
            languages = listOf("Bengali", "English"),
            diamondsPerMinute = 50,
            tagline = "Weekend foodie hangout",
            rating = 4.9
        ),
        Creator(
            id = "rohit",
            name = "Rohit",
            badgeEmoji = "",
            country = "India",
            countryFlag = "\uD83C\uDDEE\uD83C\uDDF3",
            viewerCount = 267,
            online = true,
            avatarRes = R.drawable.img_creator_rohit,
            bio = "Fitness coach, motorcycle rider, and sunset photographer. Let's talk gear and travel!",
            languages = listOf("Hindi", "English", "Punjabi"),
            diamondsPerMinute = 70,
            tagline = "Golden hour rides & fitness",
            rating = 4.95
        ),
        Creator(
            id = "soniya",
            name = "Soniya",
            badgeEmoji = "\uD83C\uDF38",
            country = "Nepal",
            countryFlag = "\uD83C\uDDF3\uD83C\uDDF5",
            viewerCount = 123,
            online = true,
            avatarRes = R.drawable.img_creator_soniya,
            bio = "Mountain trekker from Kathmandu sharing peaceful Himalayan trails and tea culture.",
            languages = listOf("Nepali", "Hindi", "English"),
            diamondsPerMinute = 60,
            tagline = "Himalayan stories & tea",
            rating = 4.85
        ),
        Creator(
            id = "neha",
            name = "Neha",
            badgeEmoji = "\u2764\uFE0F",
            country = "USA",
            countryFlag = "\uD83C\uDDFA\uD83C\uDDF8",
            viewerCount = 56,
            online = true,
            avatarRes = R.drawable.img_creator_neha,
            bio = "California UI designer and indie podcast host who loves meeting creative minds worldwide.",
            languages = listOf("English", "Spanish"),
            diamondsPerMinute = 90,
            tagline = "Design, podcasts & chill",
            rating = 4.92
        ),
        Creator(
            id = "arjun",
            name = "Arjun",
            badgeEmoji = "\uD83C\uDFA7",
            country = "Canada",
            countryFlag = "\uD83C\uDDE8\uD83C\uDDE6",
            viewerCount = 144,
            online = true,
            avatarRes = R.drawable.img_creator_arjun,
            bio = "Lo-fi beatmaker in Toronto. Drop by for music production tips or casual late-night chat.",
            languages = listOf("English", "French"),
            diamondsPerMinute = 75,
            tagline = "Lo-fi beats & cafe talk",
            rating = 4.87
        ),
        Creator(
            id = "amara",
            name = "Amara",
            badgeEmoji = "\u2728",
            country = "UK",
            countryFlag = "\uD83C\uDDEC\uD83C\uDDE7",
            viewerCount = 88,
            online = true,
            avatarRes = R.drawable.img_creator_amara,
            bio = "London theater performer and bookworm. Always happy to practice English or share laughs.",
            languages = listOf("English"),
            diamondsPerMinute = 85,
            tagline = "London life & theater",
            rating = 4.91
        )
    )

    fun findById(id: String): Creator? = creators.firstOrNull { it.id == id }

    fun countryFlagFor(country: String): String = when (country) {
        "India" -> "\uD83C\uDDEE\uD83C\uDDF3"
        "USA" -> "\uD83C\uDDFA\uD83C\uDDF8"
        "UK" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "Pakistan" -> "\uD83C\uDDF5\uD83C\uDDF0"
        "Bangladesh" -> "\uD83C\uDDE7\uD83C\uDDE9"
        "Nepal" -> "\uD83C\uDDF3\uD83C\uDDF5"
        "Canada" -> "\uD83C\uDDE8\uD83C\uDDE6"
        else -> "\uD83C\uDF10"
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val country: String = "India",
    val bio: String? = null,
    val diamonds: Long = 10000L,
    val lastRewardClaimedAt: Timestamp? = null,
    val beautyFilterEnabled: Boolean = true,
    val hdVideoEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorCountry: String = "",
    val lastMessage: String = "",
    val unreadCount: Long = 0L,
    val favorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creatorId: String = "",
    val sender: String = "USER",
    val text: String = "",
    val giftDiamonds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CallRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val creatorCountry: String = "",
    val callType: String = "VIDEO",
    val status: String = "CONNECTED",
    val durationSeconds: Long = 0L,
    val diamondsSpent: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class GiftOption(
    val id: String,
    val emoji: String,
    val label: String,
    val diamonds: Long
)

object GiftCatalog {
    val gifts = listOf(
        GiftOption("rose", "\uD83C\uDF39", "Rose", 50L),
        GiftOption("heart", "\uD83D\uDC96", "Heart", 100L),
        GiftOption("crown", "\uD83D\uDC51", "Crown", 250L),
        GiftOption("rocket", "\uD83D\uDE80", "Super Boost", 500L)
    )
}
