package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.LocalIlmColors
import com.example.ui.theme.ScholarGradients
import kotlinx.coroutines.delay

@Composable
fun IslamicStarPatternBackground(modifier: Modifier = Modifier) {
    val ilmColors = LocalIlmColors.current
    val strokeColor = ilmColors.gold.copy(alpha = if (ilmColors.isDark) 0.065f else 0.08f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val tileSize = 84.dp.toPx()
        val cols = (size.width / tileSize).toInt() + 2
        val rows = (size.height / tileSize).toInt() + 2
        val stroke = Stroke(width = 1.dp.toPx())

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val ox = c * tileSize
                val oy = r * tileSize
                val cx = ox + tileSize / 2f
                val cy = oy + tileSize / 2f
                val outer = tileSize * 0.44f
                val inner = tileSize * 0.16f

                val starPath = Path().apply {
                    moveTo(cx, cy - outer)
                    lineTo(cx + inner, cy - inner)
                    lineTo(cx + outer, cy)
                    lineTo(cx + inner, cy + inner)
                    lineTo(cx, cy + outer)
                    lineTo(cx - inner, cy + inner)
                    lineTo(cx - outer, cy)
                    lineTo(cx - inner, cy - inner)
                    close()
                }
                drawPath(path = starPath, color = strokeColor, style = stroke)
                drawCircle(
                    color = strokeColor,
                    radius = tileSize * 0.135f,
                    center = Offset(cx, cy),
                    style = stroke
                )
                drawLine(
                    color = strokeColor.copy(alpha = strokeColor.alpha * 0.45f),
                    start = Offset(cx, oy),
                    end = Offset(cx, oy + tileSize),
                    strokeWidth = 0.8.dp.toPx()
                )
                drawLine(
                    color = strokeColor.copy(alpha = strokeColor.alpha * 0.45f),
                    start = Offset(ox, cy),
                    end = Offset(ox + tileSize, cy),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun IlmTopBar(
    watchedCount: Int,
    totalCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearchBar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val progress by animateFloatAsState(
        targetValue = if (totalCount > 0) watchedCount.toFloat() / totalCount.toFloat() else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "top_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.NightsStay,
                    contentDescription = null,
                    tint = ilmColors.gold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    fontFamily = AmiriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = ilmColors.gold
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, ilmColors.hairline)
            ) {
                AnimatedContent(
                    targetState = watchedCount,
                    transitionSpec = {
                        (fadeIn(tween(220)) + slideInVertically { -it / 2 })
                            .togetherWith(fadeOut(tween(160)) + slideOutVertically { it / 2 })
                    },
                    label = "watched_counter_anim"
                ) { animatedWatched ->
                    Text(
                        text = stringResource(R.string.watched_counter_format, animatedWatched, totalCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = ilmColors.mutedText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (showSearchBar) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = ilmColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = ilmColors.gold
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.testTag("top_search_clear")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.clear_search_cd),
                                tint = ilmColors.mutedText
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = ilmColors.gold,
                    unfocusedBorderColor = ilmColors.hairline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 10.dp)
                    .testTag("top_search_bar")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(ilmColors.hairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ilmColors.gold, ilmColors.watched)
                        )
                    )
            )
        }
    }
}

@Composable
fun ScholarAvatar(
    initial: String,
    scholarIndex: Int,
    hasUnwatched: Boolean,
    isSelected: Boolean,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val gradientPair = ScholarGradients[scholarIndex.mod(ScholarGradients.size)]

    val ringBrush = when {
        isSelected -> Brush.linearGradient(listOf(ilmColors.gold, ilmColors.watched))
        hasUnwatched -> Brush.linearGradient(
            listOf(ilmColors.gold, Color(0xFF47C79E), ilmColors.goldDeep)
        )
        else -> Brush.linearGradient(listOf(ilmColors.hairline, ilmColors.hairline))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ringBrush)
            .padding(if (isSelected) 3.dp else 2.5.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.background)
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(gradientPair.first, gradientPair.second))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

@Composable
fun VideoThumbnailBox(
    video: VideoItem,
    useMaxRes: Boolean = false,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (video.kind == VideoKind.PLAYLIST) {
            Image(
                painter = painterResource(id = R.drawable.img_playlist_cover),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, ilmColors.gold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlaylistPlay,
                        contentDescription = null,
                        tint = ilmColors.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.playlist_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        } else {
            val primaryUrl = if (useMaxRes) video.maxResThumbnailUrl else video.hqThumbnailUrl
            var currentUrl by remember(video.key, useMaxRes) { mutableStateOf(primaryUrl) }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ilmColors.cardElevated.copy(alpha = shimmerAlpha))
                    )
                },
                error = {
                    if (currentUrl != video.hqThumbnailUrl && video.hqThumbnailUrl.isNotEmpty()) {
                        LaunchedEffect(video.key) {
                            currentUrl = video.hqThumbnailUrl
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_playlist_cover),
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )
        }

        // Dim overlay if already watched
        if (video.isWatched) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        // Center play pill
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.48f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(R.string.play_cd),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Short clip badge
        if (video.isShort) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.72f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = ilmColors.gold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.short_clip),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }

        // Watched badge
        if (video.isWatched) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ilmColors.watched,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = ilmColors.onWatched,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = stringResource(R.string.watched_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ilmColors.onWatched
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPostCard(
    video: VideoItem,
    onPlay: () -> Unit,
    onToggleSave: () -> Unit,
    onToggleWatched: () -> Unit,
    onShare: () -> Unit,
    onScholarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    var heartPopTrigger by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (heartPopTrigger) 1.35f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 450f),
        label = "heart_pop"
    )
    LaunchedEffect(heartPopTrigger) {
        if (heartPopTrigger) {
            delay(220)
            heartPopTrigger = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (video.isWatched) 0.85f else 1f)
            .testTag("video_post_${video.youtubeId}")
    ) {
        // Post header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onScholarClick != null) {
                        Modifier.clickable(onClick = onScholarClick)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScholarAvatar(
                initial = video.scholarInitial,
                scholarIndex = video.scholarIndex,
                hasUnwatched = false,
                isSelected = false,
                size = 42.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.scholar,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = video.categoryIcon,
                        contentDescription = null,
                        tint = ilmColors.gold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = video.topic,
                        style = MaterialTheme.typography.bodySmall,
                        color = ilmColors.gold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Video thumbnail clickable
        VideoThumbnailBox(
            video = video,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ilmColors.hairline, RoundedCornerShape(16.dp))
                .clickable(onClick = onPlay)
                .testTag("play_thumb_${video.youtubeId}")
        )

        // Action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (!video.isSaved) heartPopTrigger = true
                    onToggleSave()
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("save_btn_${video.youtubeId}")
            ) {
                Icon(
                    imageVector = if (video.isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(R.string.save_cd),
                    tint = if (video.isSaved) ilmColors.heart else MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(heartScale)
                )
            }

            var watchedPopTrigger by remember { mutableStateOf(false) }
            val watchedScale by animateFloatAsState(
                targetValue = if (watchedPopTrigger) 1.32f else 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = 450f),
                label = "watched_pop"
            )
            LaunchedEffect(watchedPopTrigger) {
                if (watchedPopTrigger) {
                    delay(220)
                    watchedPopTrigger = false
                }
            }

            IconButton(
                onClick = {
                    if (!video.isWatched) watchedPopTrigger = true
                    onToggleWatched()
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("watched_btn_${video.youtubeId}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(R.string.watched_cd),
                    tint = if (video.isWatched) ilmColors.watched else ilmColors.mutedText,
                    modifier = Modifier
                        .size(23.dp)
                        .scale(watchedScale)
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("share_btn_${video.youtubeId}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.share_cd),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        // Video title
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlay)
                .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
        )

        HorizontalDivider(
            color = ilmColors.hairline.copy(alpha = 0.7f),
            thickness = 1.dp
        )
    }
}

@Composable
fun Modifier.pressableBounce(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.965f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 520f),
        label = "press_bounce"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun MotivationalBannerCard(
    userName: String,
    messages: List<String>,
    onEditNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (messages.isEmpty()) return
    val ilmColors = LocalIlmColors.current
    var messageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(messages.size) {
        while (true) {
            delay(7500)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    val shimmerTransition = rememberInfiniteTransition(label = "motivational_shimmer")
    val shimmerAlpha by shimmerTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_shimmer"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val safeIndex = messageIndex.mod(messages.size)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    ilmColors.gold.copy(alpha = shimmerAlpha),
                    ilmColors.watched.copy(alpha = shimmerAlpha * 0.85f),
                    ilmColors.goldDeep.copy(alpha = shimmerAlpha)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .pressableBounce(interactionSource)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { messageIndex = (messageIndex + 1) % messages.size }
            )
            .testTag("motivational_banner")
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            ilmColors.cardElevated,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = ilmColors.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (userName.isBlank()) {
                            stringResource(R.string.motivational_badge)
                        } else {
                            "السلام عليكم يا $userName"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = ilmColors.gold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ilmColors.gold.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, ilmColors.gold.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onEditNameClick)
                            .testTag("edit_username_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = ilmColors.gold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(
                                    if (userName.isBlank()) R.string.user_name_add else R.string.user_name_edit
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ilmColors.gold
                            )
                        }
                    }

                    IconButton(
                        onClick = { messageIndex = (messageIndex + 1) % messages.size },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.next_message_cd),
                            tint = ilmColors.mutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedContent(
                targetState = messages[safeIndex],
                transitionSpec = {
                    (fadeIn(tween(340)) + slideInVertically(tween(340)) { it / 3 })
                        .togetherWith(fadeOut(tween(220)) + slideOutVertically(tween(220)) { -it / 3 })
                },
                label = "motivational_quote_anim"
            ) { quote ->
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun UserNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val ilmColors = LocalIlmColors.current
    var draftName by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.user_name_dialog_title),
                style = MaterialTheme.typography.titleLarge,
                color = ilmColors.gold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.user_name_dialog_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ilmColors.mutedText
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.user_name_placeholder)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ilmColors.gold,
                        unfocusedBorderColor = ilmColors.hairline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(draftName) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ilmColors.gold,
                    contentColor = ilmColors.onGold
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("username_save_btn")
            ) {
                Text(stringResource(R.string.save_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
