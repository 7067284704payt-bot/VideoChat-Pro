package com.example.data.distribution

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.distributionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "videochat_app_distribution"
)

data class AppDistributionState(
    val installedVersionName: String = BuildConfig.VERSION_NAME,
    val installedVersionCode: Int = BuildConfig.VERSION_CODE,
    val playStoreUrl: String = BuildConfig.PLAY_STORE_URL,
    val apkDownloadUrl: String = BuildConfig.APK_DOWNLOAD_URL,
    val latestVersionName: String = BuildConfig.LATEST_VERSION_NAME,
    val latestVersionCode: Int = BuildConfig.LATEST_VERSION_CODE,
    val releaseNotes: String = BuildConfig.UPDATE_RELEASE_NOTES
) {
    val isUpdateAvailable: Boolean
        get() = latestVersionCode > installedVersionCode

    val hasPlayStoreUrl: Boolean
        get() = AppDistributionManager.isValidPlayStoreUrl(playStoreUrl)

    val hasApkDownloadUrl: Boolean
        get() = AppDistributionManager.isValidSecureApkUrl(apkDownloadUrl)

    val preferredShareUrl: String?
        get() = when {
            hasPlayStoreUrl -> playStoreUrl.trim()
            hasApkDownloadUrl -> apkDownloadUrl.trim()
            else -> null
        }
}

class AppDistributionStore(private val context: Context) {

    private object Keys {
        val PLAY_STORE_URL = stringPreferencesKey("play_store_url")
        val APK_DOWNLOAD_URL = stringPreferencesKey("apk_download_url")
        val LATEST_VERSION_NAME = stringPreferencesKey("latest_version_name")
        val LATEST_VERSION_CODE = intPreferencesKey("latest_version_code")
        val RELEASE_NOTES = stringPreferencesKey("release_notes")
    }

    val state: Flow<AppDistributionState> = context.distributionDataStore.data.map { prefs ->
        AppDistributionState(
            installedVersionName = BuildConfig.VERSION_NAME,
            installedVersionCode = BuildConfig.VERSION_CODE,
            playStoreUrl = prefs[Keys.PLAY_STORE_URL] ?: BuildConfig.PLAY_STORE_URL,
            apkDownloadUrl = prefs[Keys.APK_DOWNLOAD_URL] ?: BuildConfig.APK_DOWNLOAD_URL,
            latestVersionName = prefs[Keys.LATEST_VERSION_NAME] ?: BuildConfig.LATEST_VERSION_NAME,
            latestVersionCode = prefs[Keys.LATEST_VERSION_CODE] ?: BuildConfig.LATEST_VERSION_CODE,
            releaseNotes = prefs[Keys.RELEASE_NOTES] ?: BuildConfig.UPDATE_RELEASE_NOTES
        )
    }

    suspend fun saveConfig(
        playStoreUrl: String,
        apkDownloadUrl: String,
        latestVersionName: String,
        latestVersionCode: Int,
        releaseNotes: String
    ): Result<Unit> {
        val cleanPlay = playStoreUrl.trim()
        val cleanApk = apkDownloadUrl.trim()
        val cleanVersionName = latestVersionName.trim().ifBlank { BuildConfig.VERSION_NAME }
        val cleanVersionCode = latestVersionCode.coerceAtLeast(1)
        val cleanNotes = releaseNotes.trim().ifBlank { BuildConfig.UPDATE_RELEASE_NOTES }

        if (cleanPlay.isNotEmpty() && !AppDistributionManager.isValidPlayStoreUrl(cleanPlay)) {
            return Result.failure(
                IllegalArgumentException("Play Store URL must start with https://play.google.com/ or market://details?id=")
            )
        }
        if (cleanApk.isNotEmpty() && !AppDistributionManager.isValidSecureApkUrl(cleanApk)) {
            return Result.failure(
                IllegalArgumentException("APK Download URL must be a valid https:// link.")
            )
        }

        context.distributionDataStore.edit { prefs ->
            prefs[Keys.PLAY_STORE_URL] = cleanPlay
            prefs[Keys.APK_DOWNLOAD_URL] = cleanApk
            prefs[Keys.LATEST_VERSION_NAME] = cleanVersionName
            prefs[Keys.LATEST_VERSION_CODE] = cleanVersionCode
            prefs[Keys.RELEASE_NOTES] = cleanNotes
        }
        return Result.success(Unit)
    }

    suspend fun resetConfig() {
        context.distributionDataStore.edit { it.clear() }
    }
}

sealed interface LaunchActionResult {
    data class Opened(val channel: String) : LaunchActionResult
    data class Unavailable(val reason: String) : LaunchActionResult
}

object AppDistributionManager {

    fun isValidPlayStoreUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase() ?: return false
        return when (scheme) {
            "market" -> uri.host?.lowercase() == "details" && !uri.getQueryParameter("id").isNullOrBlank()
            "https" -> {
                val host = uri.host?.lowercase() ?: ""
                host == "play.google.com" && !uri.path.isNullOrBlank()
            }
            else -> false
        }
    }

    fun isValidSecureApkUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        val uri = runCatching { Uri.parse(trimmed) }.getOrNull() ?: return false
        return uri.scheme?.lowercase() == "https" && !uri.host.isNullOrBlank()
    }

    fun openGooglePlayStore(context: Context, playStoreUrl: String): LaunchActionResult {
        val cleanUrl = playStoreUrl.trim()
        if (!isValidPlayStoreUrl(cleanUrl)) {
            return LaunchActionResult.Unavailable("Official Google Play listing is not available yet.")
        }
        val uri = Uri.parse(cleanUrl)

        // Prefer opening directly inside the official Google Play Store app if installed.
        val playIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(playIntent)
            return LaunchActionResult.Opened("Google Play Store")
        } catch (_: ActivityNotFoundException) {
            // Fall through to browser if Play Store app is unavailable and URL is HTTPS.
        }

        if (uri.scheme?.lowercase() == "https") {
            val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(webIntent)
                return LaunchActionResult.Opened("Google Play Web")
            } catch (_: ActivityNotFoundException) {
                return LaunchActionResult.Unavailable("No Google Play app or web browser found on this device.")
            }
        }

        return LaunchActionResult.Unavailable("Google Play Store is not available on this device.")
    }

    fun downloadOfficialApk(context: Context, apkDownloadUrl: String): LaunchActionResult {
        val cleanUrl = apkDownloadUrl.trim()
        if (!isValidSecureApkUrl(cleanUrl)) {
            return LaunchActionResult.Unavailable("No secure HTTPS APK download URL configured yet.")
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            LaunchActionResult.Opened("Official APK Download")
        } catch (_: ActivityNotFoundException) {
            LaunchActionResult.Unavailable("No browser or downloader app found on this device.")
        }
    }

    fun shareAppDownloadLink(context: Context, state: AppDistributionState): LaunchActionResult {
        val shareUrl = state.preferredShareUrl
            ?: return LaunchActionResult.Unavailable(
                "Configure a Play Store or HTTPS APK download URL in Settings before sharing."
            )

        val message = buildString {
            append("Join me on VideoChat Pro (v${state.latestVersionName}) for HD live video calls and chat!\n\n")
            append("Download the official app here:\n")
            append(shareUrl)
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Download VideoChat Pro")
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(sendIntent, "Share VideoChat Pro").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(chooser)
            LaunchActionResult.Opened("Share Sheet")
        } catch (_: ActivityNotFoundException) {
            LaunchActionResult.Unavailable("Unable to open Android Share sheet on this device.")
        }
    }
}
