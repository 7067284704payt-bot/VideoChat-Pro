package com.example.ui.components

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletGlow
import kotlin.math.min
import kotlin.math.roundToInt

private const val PUBLISHER_BASE_URL = "https://videochatpro.app/"

sealed class AdsterraAdUnit(
    val id: String,
    val label: String,
    val widthDp: Int?,
    val heightDp: Int,
    val isDynamicHeight: Boolean = false,
    val collapseUntilVisibleContent: Boolean = false
) {
    /** Ad 1: Inline / Social Bar Script */
    data object InlineScript : AdsterraAdUnit(
        id = "ad_1_inline_script",
        label = "Sponsored",
        widthDp = null,
        heightDp = 1,
        isDynamicHeight = true,
        collapseUntilVisibleContent = true
    )

    /** Ad 2: 300x250 Medium Rectangle */
    data object Banner300x250 : AdsterraAdUnit(
        id = "ad_2_300x250",
        label = "Sponsored",
        widthDp = 300,
        heightDp = 250
    )

    /** Ad 3: 320x50 Mobile Leaderboard */
    data object Banner320x50 : AdsterraAdUnit(
        id = "ad_3_320x50",
        label = "Ad",
        widthDp = 320,
        heightDp = 50
    )

    /** Ad 4: 160x300 Skyscraper */
    data object Banner160x300 : AdsterraAdUnit(
        id = "ad_4_160x300",
        label = "Ad",
        widthDp = 160,
        heightDp = 300
    )

    /** Ad 5: 160x600 Wide Skyscraper */
    data object Banner160x600 : AdsterraAdUnit(
        id = "ad_5_160x600",
        label = "Ad",
        widthDp = 160,
        heightDp = 600
    )

    /** Ad 6: 468x60 Full Banner */
    data object Banner468x60 : AdsterraAdUnit(
        id = "ad_6_468x60",
        label = "Sponsored",
        widthDp = 468,
        heightDp = 60
    )

    /** Ad 7: Native Banner */
    data object NativeBanner : AdsterraAdUnit(
        id = "ad_7_native",
        label = "Sponsored Picks",
        widthDp = null,
        heightDp = 160,
        isDynamicHeight = true
    )

    fun buildHtml(fitViewportWidth: Int? = null): String {
        val viewportMeta = if (fitViewportWidth != null) {
            "width=$fitViewportWidth, user-scalable=no"
        } else {
            "width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"
        }

        val creativeObserverScript = """
            <script>
              (function() {
                var notifiedReady = false;
                function inspectDom() {
                  if (!document.body) return;
                  var hasFrame = document.querySelector('iframe, img, ins, a, video, canvas');
                  var root = document.getElementById('container-b3de1b91c22f6a0b40b5945a9fe3ed8e') ||
                             document.getElementById('inline-ad-slot') ||
                             document.body;
                  var h = root ? Math.max(root.scrollHeight || 0, document.body.scrollHeight || 0) : 0;
                  if (window.AdBridge) {
                    if (h > 12) {
                      window.AdBridge.onHeightChanged(Math.round(h));
                    }
                    if (!notifiedReady && (hasFrame || h > 20)) {
                      notifiedReady = true;
                      window.AdBridge.onCreativeReady();
                    }
                  }
                }
                if (typeof MutationObserver !== 'undefined' && document.body) {
                  new MutationObserver(inspectDom).observe(document.body, {
                    childList: true,
                    subtree: true,
                    attributes: true
                  });
                }
                if (typeof ResizeObserver !== 'undefined' && document.documentElement) {
                  new ResizeObserver(inspectDom).observe(document.documentElement);
                }
                window.addEventListener('load', inspectDom);
                setTimeout(inspectDom, 400);
                setTimeout(inspectDom, 1200);
                setTimeout(inspectDom, 2500);
                setTimeout(inspectDom, 4500);
              })();
            </script>
        """.trimIndent()

        val snippet = when (this) {
            InlineScript -> """
                <div id="inline-ad-slot" style="width:100%;min-height:1px;text-align:center;">
                  <script data-cfasync="false"
                          src="https://bauval.org/14/f124a532fbfd154e431b3bc39685936e"
                          onerror="window.AdBridge && window.AdBridge.onScriptError()"></script>
                </div>
                $creativeObserverScript
            """.trimIndent()

            Banner300x250 -> iframeSnippet(
                key = "70ad69496fdca744cd01abc904dc63fa",
                width = 300,
                height = 250,
                scriptUrl = "https://bauval.org/22/70ad69496fdca744cd01abc904dc63fa",
                observer = creativeObserverScript
            )

            Banner320x50 -> iframeSnippet(
                key = "9e54172d1d0747e0130707a952b75dcf",
                width = 320,
                height = 50,
                scriptUrl = "https://bauval.org/22/9e54172d1d0747e0130707a952b75dcf",
                observer = creativeObserverScript
            )

            Banner160x300 -> iframeSnippet(
                key = "e669f8452643a7e08de3b6002e9c7fa3",
                width = 160,
                height = 300,
                scriptUrl = "https://bauval.org/22/e669f8452643a7e08de3b6002e9c7fa3",
                observer = creativeObserverScript
            )

            Banner160x600 -> iframeSnippet(
                key = "3f569b433b55b7547576ed4527a495c1",
                width = 160,
                height = 600,
                scriptUrl = "https://bauval.org/22/3f569b433b55b7547576ed4527a495c1",
                observer = creativeObserverScript
            )

            Banner468x60 -> iframeSnippet(
                key = "9d4add14cf0ee9836bd85d2d1cf06564",
                width = 468,
                height = 60,
                scriptUrl = "https://bauval.org/22/9d4add14cf0ee9836bd85d2d1cf06564",
                observer = creativeObserverScript
            )

            NativeBanner -> """
                <div style="width:100%;color:#E2E8F0;font-family:sans-serif;text-align:center;">
                  <script async="async"
                          data-cfasync="false"
                          src="https://bauval.org/21/b3de1b91c22f6a0b40b5945a9fe3ed8e"
                          onerror="window.AdBridge && window.AdBridge.onScriptError()"></script>
                  <div id="container-b3de1b91c22f6a0b40b5945a9fe3ed8e"></div>
                </div>
                $creativeObserverScript
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html>
              <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="$viewportMeta" />
                <style>
                  * { box-sizing: border-box; }
                  html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    background: transparent;
                    overflow: hidden;
                    text-align: center;
                  }
                </style>
              </head>
              <body>
                $snippet
              </body>
            </html>
        """.trimIndent()
    }

    private fun iframeSnippet(
        key: String,
        width: Int,
        height: Int,
        scriptUrl: String,
        observer: String
    ): String {
        return """
            <div style="width:${width}px;height:${height}px;margin:0 auto;overflow:hidden;text-align:center;">
              <script type="text/javascript">
                window.atOptions = {
                  'key' : '$key',
                  'format' : 'iframe',
                  'height' : $height,
                  'width' : $width,
                  'params' : {}
                };
              </script>
              <script type="text/javascript"
                      src="$scriptUrl"
                      onerror="window.AdBridge && window.AdBridge.onScriptError()"></script>
            </div>
            $observer
        """.trimIndent()
    }
}

@Composable
fun AdsterraBannerCard(
    unit: AdsterraAdUnit,
    modifier: Modifier = Modifier,
    showHeaderBadge: Boolean = true,
    compactPadding: Boolean = false
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val isOnline = remember(context) { isNetworkAvailable(context) }
    var hasLoadError by remember(unit.id) { mutableStateOf(false) }
    var isCreativeReady by remember(unit.id) { mutableStateOf(false) }

    if (!isPreview && (!isOnline || hasLoadError)) {
        return
    }

    if (unit.collapseUntilVisibleContent && !isCreativeReady && !isPreview) {
        // Keep the WebView mounted discreetly at 1.dp so script 14 can execute without rendering a blank card.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        ) {
            AdsterraWebViewSlot(
                unit = unit,
                isCreativeReady = isCreativeReady,
                onCreativeReady = { isCreativeReady = true },
                onLoadError = { hasLoadError = true }
            )
        }
        return
    }

    Surface(
        shape = RoundedCornerShape(if (compactPadding) 12.dp else 18.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .animateContentSize()
            .testTag("adsterra_${unit.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(
                    horizontal = if (compactPadding) 8.dp else 10.dp,
                    vertical = if (compactPadding) 4.dp else 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showHeaderBadge) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (compactPadding) 2.dp else 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = unit.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = CardSurfaceElevated,
                        border = BorderStroke(0.8.dp, VioletGlow.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "AD",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VioletGlow,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            AdsterraWebViewSlot(
                unit = unit,
                isCreativeReady = isCreativeReady,
                onCreativeReady = { isCreativeReady = true },
                onLoadError = { hasLoadError = true }
            )
        }
    }
}

@Composable
fun AdsterraStickyBannerBar(
    unit: AdsterraAdUnit = AdsterraAdUnit.Banner320x50,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val isOnline = remember(context) { isNetworkAvailable(context) }
    var hasLoadError by remember(unit.id) { mutableStateOf(false) }
    var isCreativeReady by remember(unit.id) { mutableStateOf(false) }

    if (!isPreview && (!isOnline || hasLoadError)) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CardSurface.copy(alpha = 0.96f))
            .testTag("adsterra_sticky_${unit.id}")
    ) {
        HorizontalDivider(thickness = 1.dp, color = GlassBorder)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(0.8.dp, VioletGlow.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "AD",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = VioletGlow,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                AdsterraWebViewSlot(
                    unit = unit,
                    isCreativeReady = isCreativeReady,
                    onCreativeReady = { isCreativeReady = true },
                    onLoadError = { hasLoadError = true }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AdsterraWebViewSlot(
    unit: AdsterraAdUnit,
    isCreativeReady: Boolean,
    onCreativeReady: () -> Unit,
    onLoadError: () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    var dynamicHeightDp by remember(unit.id) { mutableIntStateOf(unit.heightDp) }
    var loadingTimedOut by remember(unit.id) { mutableStateOf(false) }

    LaunchedEffect(unit.id, isCreativeReady) {
        if (!isCreativeReady) {
            delay(6000L)
            loadingTimedOut = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidthDp = if (constraints.hasBoundedWidth) {
            maxWidth.value
        } else {
            unit.widthDp?.toFloat() ?: 320f
        }
        val targetWidth = unit.widthDp
        val needsShrinkToFit = targetWidth != null && availableWidthDp > 0f && targetWidth > availableWidthDp
        val scale = if (needsShrinkToFit && targetWidth != null) {
            min(1f, availableWidthDp / targetWidth.toFloat())
        } else {
            1f
        }

        val slotHeight: Dp = if (unit.isDynamicHeight) {
            if (unit.collapseUntilVisibleContent && !isCreativeReady) {
                1.dp
            } else {
                dynamicHeightDp.coerceIn(48, 320).dp
            }
        } else {
            (unit.heightDp * scale).roundToInt().coerceAtLeast(36).dp
        }

        val slotModifier = if (targetWidth != null) {
            val scaledWidth = (targetWidth * scale).roundToInt().dp
            Modifier
                .width(scaledWidth)
                .height(slotHeight)
        } else {
            Modifier
                .fillMaxWidth()
                .height(slotHeight)
        }

        if (isPreview) {
            Box(
                modifier = slotModifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F4D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Adsterra ${unit.widthDp ?: "Responsive"}x${unit.heightDp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        } else {
            val fitViewportWidth = if (needsShrinkToFit) targetWidth else null
            val html = remember(unit.id, fitViewportWidth) { unit.buildHtml(fitViewportWidth) }

            Box(
                modifier = slotModifier.clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!isCreativeReady && !loadingTimedOut && !unit.collapseUntilVisibleContent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardSurfaceElevated.copy(alpha = 0.55f))
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = VioletGlow
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading Ad...",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                key(unit.id) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                setBackgroundColor(AndroidColor.TRANSPARENT)
                                overScrollMode = View.OVER_SCROLL_NEVER
                                isVerticalScrollBarEnabled = false
                                isHorizontalScrollBarEnabled = false
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadsImagesAutomatically = true
                                    blockNetworkImage = false
                                    blockNetworkLoads = false
                                    mediaPlaybackRequiresUserGesture = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = fitViewportWidth != null
                                    setSupportZoom(false)
                                    builtInZoomControls = false
                                    displayZoomControls = false
                                    setSupportMultipleWindows(true)
                                    javaScriptCanOpenWindowsAutomatically = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    allowFileAccess = false
                                    allowContentAccess = false
                                    // Remove "; wv" WebView marker so Adsterra serves creatives just like Mobile Chrome.
                                    val defaultUa = WebSettings.getDefaultUserAgent(ctx)
                                    userAgentString = defaultUa
                                        .replace("; wv", "")
                                        .replace("Version/4.0 ", "")
                                }

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                addJavascriptInterface(
                                    AdBridge(
                                        onHeight = { reportedPx ->
                                            if (unit.isDynamicHeight && reportedPx in 24..340) {
                                                post { dynamicHeightDp = reportedPx }
                                            }
                                        },
                                        onReady = {
                                            post { onCreativeReady() }
                                        },
                                        onError = {
                                            post { onLoadError() }
                                        }
                                    ),
                                    "AdBridge"
                                )

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val uri = request?.url ?: return false
                                        if (request.hasGesture()) {
                                            openAdInExternalBrowser(ctx, uri)
                                            return true
                                        }
                                        return false
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            onLoadError()
                                        }
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?
                                    ): Boolean {
                                        if (!isUserGesture || resultMsg == null) return false
                                        val tempWebView = WebView(ctx).apply {
                                            webViewClient = object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(
                                                    v: WebView?,
                                                    req: WebResourceRequest?
                                                ): Boolean {
                                                    req?.url?.let { openAdInExternalBrowser(ctx, it) }
                                                    v?.destroy()
                                                    return true
                                                }
                                            }
                                        }
                                        val transport = resultMsg.obj as? WebView.WebViewTransport
                                        transport?.webView = tempWebView
                                        resultMsg.sendToTarget()
                                        return true
                                    }
                                }

                                loadDataWithBaseURL(
                                    PUBLISHER_BASE_URL,
                                    html,
                                    "text/html",
                                    "UTF-8",
                                    PUBLISHER_BASE_URL
                                )
                            }
                        },
                        onRelease = { webView ->
                            webView.removeJavascriptInterface("AdBridge")
                            webView.stopLoading()
                            webView.loadUrl("about:blank")
                            webView.clearHistory()
                            webView.removeAllViews()
                            webView.destroy()
                        }
                    )
                }
            }
        }
    }
}

private class AdBridge(
    private val onHeight: (Int) -> Unit,
    private val onReady: () -> Unit,
    private val onError: () -> Unit
) {
    @JavascriptInterface
    fun onHeightChanged(heightCssPx: Int) {
        onHeight(heightCssPx)
    }

    @JavascriptInterface
    fun onCreativeReady() {
        onReady()
    }

    @JavascriptInterface
    fun onScriptError() {
        onError()
    }
}

private fun openAdInExternalBrowser(context: Context, uri: Uri) {
    val scheme = uri.scheme?.lowercase()
    if (scheme != "https" && scheme != "http") return
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // Ignore if no browser is installed
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
