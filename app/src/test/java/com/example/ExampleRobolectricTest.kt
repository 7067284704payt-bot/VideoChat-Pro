package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.distribution.AppDistributionManager
import com.example.data.distribution.AppDistributionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VideoChat Pro", appName)
  }

  @Test
  fun `validate play store and secure apk urls`() {
    assertTrue(
      AppDistributionManager.isValidPlayStoreUrl(
        "https://play.google.com/store/apps/details?id=com.aistudio.videochatpro.vrkmpq"
      )
    )
    assertTrue(
      AppDistributionManager.isValidPlayStoreUrl(
        "market://details?id=com.aistudio.videochatpro.vrkmpq"
      )
    )
    assertFalse(AppDistributionManager.isValidPlayStoreUrl("https://evil.example.com/fake"))
    assertFalse(AppDistributionManager.isValidPlayStoreUrl(""))

    assertTrue(AppDistributionManager.isValidSecureApkUrl("https://releases.example.com/videochat-pro.apk"))
    assertFalse(AppDistributionManager.isValidSecureApkUrl("http://insecure.example.com/app.apk"))
    assertFalse(AppDistributionManager.isValidSecureApkUrl(""))
  }

  @Test
  fun `update detection and preferred share link fallback`() {
    val unconfigured = AppDistributionState(
      installedVersionCode = 1,
      latestVersionCode = 1,
      playStoreUrl = "",
      apkDownloadUrl = ""
    )
    assertFalse(unconfigured.isUpdateAvailable)
    assertNull(unconfigured.preferredShareUrl)

    val apkOnlyUpdate = AppDistributionState(
      installedVersionCode = 1,
      latestVersionCode = 2,
      latestVersionName = "1.1.0",
      playStoreUrl = "",
      apkDownloadUrl = "https://cdn.example.com/VideoChatPro-v1.1.0.apk"
    )
    assertTrue(apkOnlyUpdate.isUpdateAvailable)
    assertEquals("https://cdn.example.com/VideoChatPro-v1.1.0.apk", apkOnlyUpdate.preferredShareUrl)
  }
}
