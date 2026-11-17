package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.ui.components.VideoThumbnailBox
import com.example.ui.theme.LocalIlmColors

@Composable
fun VideoPlayerOverlay(
    isOpen: Boolean,
    currentVideo: VideoItem?,
    upNextVideos: List<VideoItem>,
    onClose: () -> Unit,
    onSelectVideo: (VideoItem) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onToggleWatched: (VideoItem) -> Unit,
    onAutoWatched: (String) -> Unit,
    onVideoEnded: () -> Unit,
    onShare: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isOpen && currentVideo != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        if (currentVideo != null) {
            BackHandler(onBack = onClose)
            PlayerSheetContent(
                video = currentVideo,
                upNextVideos = upNextVideos,
                onClose = onClose,
                onSelectVideo = onSelectVideo,
                onToggleSave = { onToggleSave(currentVideo) },
                onToggleWatched = { onToggleWatched(currentVideo) },
                onAutoWatched = { onAutoWatched(currentVideo.key) },
                onVideoEnded = onVideoEnded,
                onShare = { onShare(currentVideo) }
            )
        }
    }
}

@Composable
private fun PlayerSheetContent(
    video: VideoItem,
    upNextVideos: List<VideoItem>,
    onClose: () -> Unit,
    onSelectVideo: (VideoItem) -> Unit,
    onToggleSave: () -> Unit,
    onToggleWatched: () -> Unit,
    onAutoWatched: () -> Unit,
    onVideoEnded: () -> Unit,
    onShare: () -> Unit
) {
    val ilmColors = LocalIlmColors.current
    val context = LocalContext.current
    var hasEmbedError by remember(video.key) { mutableStateOf(false) }
    var isPlayerLoading by remember(video.key) { mutableStateOf(true) }
    var fullscreenCustomView by remember { mutableStateOf<View?>(null) }

    val openExternalYouTube = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.youtubeUrl))
        context.startActivity(intent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("video_player_overlay")
    ) {
        // Ambient blurred backdrop
        if (video.kind == VideoKind.VIDEO && video.hqThumbnailUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.hqThumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.28f)
                    .then(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Modifier.blur(42.dp)
                        } else {
                            Modifier
                        }
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.65f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sticky Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("player_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.close_cd),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = video.scholar,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = openExternalYouTube) {
                    Text(
                        text = stringResource(R.string.open_in_youtube),
                        style = MaterialTheme.typography.labelLarge,
                        color = ilmColors.gold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
            ) {
                // Embedded YouTube Player Box (16:9)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                ) {
                    YouTubeEmbedWebView(
                        video = video,
                        onReady = { isPlayerLoading = false },
                        onProgressWatched = onAutoWatched,
                        onEnded = onVideoEnded,
                        onError = {
                            isPlayerLoading = false
                            hasEmbedError = true
                        },
                        onShowCustomView = { view -> fullscreenCustomView = view },
                        onHideCustomView = { fullscreenCustomView = null }
                    )

                    if (isPlayerLoading && !hasEmbedError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.72f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ilmColors.gold)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = stringResource(R.string.player_loading),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (hasEmbedError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xE607100D))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.player_embed_error),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = openExternalYouTube,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ilmColors.gold,
                                            contentColor = ilmColors.onGold
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.open_in_youtube_full))
                                    }
                                    if (upNextVideos.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = onVideoEnded,
                                            border = BorderStroke(1.dp, ilmColors.gold),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.SkipNext,
                                                contentDescription = null,
                                                tint = ilmColors.gold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = stringResource(R.string.up_next),
                                                color = ilmColors.gold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Info + Action Bar + Up Next Queue
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item(key = "player_meta") {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = video.categoryIcon,
                                    contentDescription = null,
                                    tint = ilmColors.gold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = buildString {
                                        append(video.topic)
                                        if (video.isShort) append(" · مقطع قصير")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ilmColors.gold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onToggleSave,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = if (video.isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = stringResource(R.string.save_cd),
                                    tint = if (video.isSaved) ilmColors.heart else MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = onToggleWatched,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = stringResource(R.string.watched_cd),
                                    tint = if (video.isWatched) ilmColors.watched else ilmColors.mutedText,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.share_cd),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = openExternalYouTube) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = ilmColors.mutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.open_in_youtube_full),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ilmColors.mutedText
                                )
                            }
                        }

                        HorizontalDivider(color = ilmColors.hairline.copy(alpha = 0.6f))
                    }

                    if (upNextVideos.isNotEmpty()) {
                        item(key = "up_next_header") {
                            Text(
                                text = stringResource(R.string.up_next),
                                style = MaterialTheme.typography.titleMedium,
                                color = ilmColors.gold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        items(
                            items = upNextVideos,
                            key = { "next_${it.key}" }
                        ) { nextVideo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectVideo(nextVideo) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(128.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, ilmColors.hairline, RoundedCornerShape(10.dp))
                                ) {
                                    VideoThumbnailBox(video = nextVideo)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = nextVideo.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = nextVideo.scholar,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ilmColors.mutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (nextVideo.isWatched) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = ilmColors.watched,
                                                modifier = Modifier.size(14.dp)
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

        // Fullscreen custom view overlay when user taps fullscreen inside YouTube player
        val fsView = fullscreenCustomView
        if (fsView != null) {
            BackHandler {
                fullscreenCustomView = null
            }
            AndroidView(
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        (fsView.parent as? ViewGroup)?.removeView(fsView)
                        addView(
                            fsView,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
    }
}

private class IlmPlayerJsBridge(
    private val onReadyCallback: () -> Unit,
    private val onWatchedCallback: () -> Unit,
    private val onEndedCallback: () -> Unit,
    private val onErrorCallback: () -> Unit
) {
    @JavascriptInterface
    fun onReady() {
        onReadyCallback()
    }

    @JavascriptInterface
    fun onWatched() {
        onWatchedCallback()
    }

    @JavascriptInterface
    fun onEnded() {
        onEndedCallback()
    }

    @JavascriptInterface
    fun onError() {
        onErrorCallback()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeEmbedWebView(
    video: VideoItem,
    onReady: () -> Unit,
    onProgressWatched: () -> Unit,
    onEnded: () -> Unit,
    onError: () -> Unit,
    onShowCustomView: (View) -> Unit,
    onHideCustomView: () -> Unit
) {
    val context = LocalContext.current
    val currentOnReady by rememberUpdatedState(onReady)
    val currentOnWatched by rememberUpdatedState(onProgressWatched)
    val currentOnEnded by rememberUpdatedState(onEnded)
    val currentOnError by rememberUpdatedState(onError)

    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                loadsImagesAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (view != null) onShowCustomView(view)
                }

                override fun onHideCustomView() {
                    onHideCustomView()
                }
            }
            addJavascriptInterface(
                IlmPlayerJsBridge(
                    onReadyCallback = { post { currentOnReady() } },
                    onWatchedCallback = { post { currentOnWatched() } },
                    onEndedCallback = { post { currentOnEnded() } },
                    onErrorCallback = { post { currentOnError() } }
                ),
                "IlmBridge"
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    val html = remember(video.key) { buildYouTubePlayerHtml(video) }

    AndroidView(
        factory = { webView },
        update = { view ->
            if (view.tag != video.key) {
                view.tag = video.key
                view.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun buildYouTubePlayerHtml(video: VideoItem): String {
    val playerVars = if (video.kind == VideoKind.VIDEO) {
        "rel: 0, playsinline: 1, autoplay: 1, modestbranding: 1, origin: 'https://www.youtube.com'"
    } else {
        "rel: 0, playsinline: 1, autoplay: 1, listType: 'playlist', list: '${video.youtubeId}', origin: 'https://www.youtube.com'"
    }
    val videoIdLine = if (video.kind == VideoKind.VIDEO) "videoId: '${video.youtubeId}'," else ""

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            html, body { margin:0; padding:0; width:100%; height:100%; background:#000; overflow:hidden; }
            #player { width:100%; height:100%; }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script src="https://www.youtube.com/iframe_api"></script>
          <script>
            var ytPlayer = null;
            var watchTimer = null;
            var marked = false;
            function onYouTubeIframeAPIReady() {
              ytPlayer = new YT.Player('player', {
                width: '100%',
                height: '100%',
                $videoIdLine
                playerVars: { $playerVars },
                events: {
                  onReady: function() {
                    if (window.IlmBridge) IlmBridge.onReady();
                    watchTimer = setInterval(function() {
                      try {
                        if (!marked && ytPlayer && ytPlayer.getDuration) {
                          var d = ytPlayer.getDuration();
                          if (d > 0 && ytPlayer.getCurrentTime() / d >= 0.9) {
                            marked = true;
                            if (window.IlmBridge) IlmBridge.onWatched();
                          }
                        }
                      } catch (e) {}
                    }, 4000);
                  },
                  onStateChange: function(e) {
                    if (e.data === 0) {
                      if (!marked && window.IlmBridge) {
                        marked = true;
                        IlmBridge.onWatched();
                      }
                      if (window.IlmBridge) IlmBridge.onEnded();
                    }
                  },
                  onError: function() {
                    if (window.IlmBridge) IlmBridge.onError();
                  }
                }
              });
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}
