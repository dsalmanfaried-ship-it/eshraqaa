package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.CategoryMapper
import com.example.model.VideoItem
import com.example.model.VideoKind
import com.example.ui.CategoryShelf
import com.example.ui.IlmUiState
import com.example.ui.ScholarStory
import com.example.ui.components.MotivationalBannerCard
import com.example.ui.components.ScholarAvatar
import com.example.ui.components.VideoPostCard
import com.example.ui.components.VideoThumbnailBox
import com.example.ui.components.pressableBounce
import com.example.ui.theme.LocalIlmColors
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(
    uiState: IlmUiState,
    onSelectScholar: (String?) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onOpenPlayer: (VideoItem, List<VideoItem>) -> Unit,
    onToggleSave: (VideoItem) -> Unit,
    onToggleWatched: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    onOpenFavoritesTab: () -> Unit,
    onEditUserName: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.selectedScholar, uiState.selectedCategory, uiState.searchQuery) {
        listState.scrollToItem(0)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .testTag("home_feed_list"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Scholar Stories Row
            item(key = "stories_row") {
                ScholarStoriesRow(
                    scholars = uiState.scholars,
                    selectedScholar = uiState.selectedScholar,
                    onSelectScholar = onSelectScholar
                )
            }

            // 1.5 Personalized Motivational Banner
            if (uiState.motivationalMessages.isNotEmpty() && uiState.searchQuery.isBlank()) {
                item(key = "motivational_banner") {
                    MotivationalBannerCard(
                        userName = uiState.userName,
                        messages = uiState.motivationalMessages,
                        onEditNameClick = onEditUserName
                    )
                }
            }

            // 2. Featured Hero Carousel (only when no scholar/category filter active)
            if (uiState.heroVideos.isNotEmpty()) {
                item(key = "hero_carousel") {
                    FeaturedHeroCarousel(
                        videos = uiState.heroVideos,
                        isPlayerOpen = uiState.player.isOpen,
                        onPlay = { video -> onOpenPlayer(video, uiState.heroVideos) }
                    )
                }
            }

            // 3. Category Filter Chips
            item(key = "category_chips") {
                CategoryFilterChipsRow(
                    categories = uiState.availableCategories,
                    selectedCategory = uiState.selectedCategory,
                    onSelectCategory = onSelectCategory
                )
            }

            // 3.5. Quick Access Favorites Strip (whenever user has liked videos and not filtering)
            if (uiState.savedVideos.isNotEmpty() && uiState.selectedScholar == null && uiState.selectedCategory == null && uiState.searchQuery.isBlank()) {
                item(key = "quick_favorites_section") {
                    QuickFavoritesSection(
                        favorites = uiState.savedVideos.take(10),
                        onShowAll = onOpenFavoritesTab,
                        onPlay = { video -> onOpenPlayer(video, uiState.savedVideos) }
                    )
                }
            }

            // 4. Category Horizontal Shelves
            if (uiState.categoryShelves.isNotEmpty()) {
                items(
                    items = uiState.categoryShelves,
                    key = { "shelf_${it.category}" }
                ) { shelf ->
                    CategoryShelfSection(
                        shelf = shelf,
                        onShowAll = { onSelectCategory(shelf.category) },
                        onPlay = { video -> onOpenPlayer(video, shelf.videos) }
                    )
                }
            }

            // 5. All Videos Header
            item(key = "all_videos_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = ilmColors.gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                uiState.searchQuery.isNotBlank() -> stringResource(R.string.search_results_title)
                                uiState.selectedScholar != null -> uiState.selectedScholar
                                uiState.selectedCategory != null -> uiState.selectedCategory
                                else -> stringResource(R.string.all_videos)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "${uiState.homeFeed.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ilmColors.mutedText
                    )
                }
            }

            // 6. Main Feed
            if (uiState.homeFeed.isEmpty() && !uiState.isLoading) {
                item(key = "empty_feed") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_state),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) {
                                stringResource(R.string.no_results)
                            } else {
                                "لا توجد دروس في هذا التصنيف حالياً"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = ilmColors.mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(
                    items = uiState.homeFeed,
                    key = { "feed_${it.key}" }
                ) { video ->
                    VideoPostCard(
                        video = video,
                        onPlay = { onOpenPlayer(video, uiState.homeFeed) },
                        onToggleSave = { onToggleSave(video) },
                        onToggleWatched = { onToggleWatched(video) },
                        onShare = { onShare(video) },
                        onScholarClick = {
                            onSelectScholar(if (uiState.selectedScholar == video.scholar) null else video.scholar)
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun ScholarStoriesRow(
    scholars: List<ScholarStory>,
    selectedScholar: String?,
    onSelectScholar: (String?) -> Unit
) {
    val ilmColors = LocalIlmColors.current

    Column {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "scholar_all") {
                val isSelected = selectedScholar == null
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectScholar(null) }
                        .padding(vertical = 2.dp)
                        .testTag("scholar_all")
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(listOf(ilmColors.gold, ilmColors.watched))
                                } else {
                                    Brush.linearGradient(listOf(ilmColors.gold, ilmColors.goldDeep))
                                }
                            )
                            .padding(if (isSelected) 3.dp else 2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ilmColors.gold, Color(0xFF8A6A1F)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = stringResource(R.string.all_label),
                            tint = Color(0xFF17140A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.all_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) ilmColors.gold else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            items(items = scholars, key = { "story_${it.name}" }) { scholar ->
                val isSelected = selectedScholar == scholar.name
                val avatarScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 450f),
                    label = "scholar_scale"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(70.dp)
                        .graphicsLayer {
                            scaleX = avatarScale
                            scaleY = avatarScale
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectScholar(if (isSelected) null else scholar.name)
                        }
                        .padding(vertical = 2.dp)
                        .testTag("scholar_story_${scholar.index}")
                ) {
                    ScholarAvatar(
                        initial = scholar.initial,
                        scholarIndex = scholar.index,
                        hasUnwatched = scholar.hasUnwatched,
                        isSelected = isSelected,
                        size = 62.dp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scholar.shortName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ilmColors.gold else MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        HorizontalDivider(color = ilmColors.hairline.copy(alpha = 0.65f))
    }
}

@Composable
private fun FeaturedHeroCarousel(
    videos: List<VideoItem>,
    isPlayerOpen: Boolean,
    onPlay: (VideoItem) -> Unit
) {
    val ilmColors = LocalIlmColors.current
    val pagerState = rememberPagerState(pageCount = { videos.size })

    LaunchedEffect(videos.size, isPlayerOpen) {
        if (videos.size > 1 && !isPlayerOpen) {
            while (true) {
                delay(5000)
                if (!pagerState.isScrollInProgress) {
                    val next = (pagerState.currentPage + 1) % videos.size
                    pagerState.animateScrollToPage(next)
                }
            }
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "hero_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(modifier = Modifier.padding(top = 12.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val video = videos[page]
            val context = LocalContext.current
            var thumbUrl by remember(video.key) { mutableStateOf(video.maxResThumbnailUrl) }
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
            val cardScale = (1f - pageOffset.coerceIn(0f, 1f) * 0.07f)
            val cardAlpha = (1f - pageOffset.coerceIn(0f, 1f) * 0.25f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    }
                    .pressableBounce()
                    .aspectRatio(16f / 9.5f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, ilmColors.hairline, RoundedCornerShape(22.dp))
                    .clickable { onPlay(video) }
                    .testTag("hero_card_$page")
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(thumbUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    onError = {
                        if (thumbUrl != video.hqThumbnailUrl) {
                            thumbUrl = video.hqThumbnailUrl
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x66050C09),
                                    Color(0xF0050C09)
                                )
                            )
                        )
                )

                // Pulsing Play Orb
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(48.dp)
                        .scale(if (page == pagerState.currentPage) pulseScale else 1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.play_cd),
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Info overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ilmColors.gold
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = video.categoryIcon,
                                contentDescription = null,
                                tint = ilmColors.onGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = video.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ilmColors.onGold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = video.scholar,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD9E6DF)
                    )
                }
            }
        }

        // Indicator Dots
        if (videos.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(videos.size) { index ->
                    val active = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (active) 20.dp else 6.dp,
                        animationSpec = tween(250),
                        label = "dot_w"
                    )
                    val color by animateColorAsState(
                        targetValue = if (active) ilmColors.gold else ilmColors.hairline,
                        animationSpec = tween(250),
                        label = "dot_c"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterChipsRow(
    categories: List<String>,
    selectedCategory: String?,
    onSelectCategory: (String?) -> Unit
) {
    val ilmColors = LocalIlmColors.current

    LazyRow(
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "chip_all") {
            CategoryPill(
                label = stringResource(R.string.all_label),
                icon = null,
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                modifier = Modifier.testTag("cat_chip_all")
            )
        }
        items(items = categories, key = { "chip_$it" }) { category ->
            val selected = selectedCategory == category
            CategoryPill(
                label = category,
                icon = CategoryMapper.iconFor(category),
                selected = selected,
                onClick = { onSelectCategory(if (selected) null else category) },
                modifier = Modifier.testTag("cat_chip_$category")
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ilmColors = LocalIlmColors.current
    val containerColor by animateColorAsState(
        targetValue = if (selected) ilmColors.gold else MaterialTheme.colorScheme.surface,
        animationSpec = tween(220),
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) ilmColors.onGold else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(220),
        label = "chip_fg"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = if (selected) null else BorderStroke(1.dp, ilmColors.hairline),
        modifier = modifier
            .height(40.dp)
            .pressableBounce(pressedScale = 0.94f)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) ilmColors.onGold else ilmColors.gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

@Composable
private fun CategoryShelfSection(
    shelf: CategoryShelf,
    onShowAll: () -> Unit,
    onPlay: (VideoItem) -> Unit
) {
    val ilmColors = LocalIlmColors.current

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = CategoryMapper.iconFor(shelf.category),
                    contentDescription = null,
                    tint = ilmColors.gold,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = shelf.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onShowAll)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.show_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = ilmColors.gold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ilmColors.gold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = shelf.videos,
                key = { "shelf_${shelf.category}_${it.key}" }
            ) { video ->
                Column(
                    modifier = Modifier
                        .width(164.dp)
                        .alpha(if (video.isWatched) 0.72f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPlay(video) }
                        .padding(bottom = 4.dp)
                ) {
                    VideoThumbnailBox(
                        video = video,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ilmColors.hairline, RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )
                    Text(
                        text = video.scholar,
                        style = MaterialTheme.typography.labelSmall,
                        color = ilmColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFavoritesSection(
    favorites: List<VideoItem>,
    onShowAll: () -> Unit,
    onPlay: (VideoItem) -> Unit
) {
    val ilmColors = LocalIlmColors.current

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .testTag("quick_favorites_strip")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    tint = ilmColors.heart,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.quick_favorites),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onShowAll)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("quick_favorites_show_all")
            ) {
                Text(
                    text = stringResource(R.string.show_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = ilmColors.gold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ilmColors.gold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = favorites,
                key = { "qfav_${it.key}" }
            ) { video ->
                Column(
                    modifier = Modifier
                        .width(164.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPlay(video) }
                        .padding(bottom = 4.dp)
                ) {
                    VideoThumbnailBox(
                        video = video,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, ilmColors.heart.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )
                    Text(
                        text = video.scholar,
                        style = MaterialTheme.typography.labelSmall,
                        color = ilmColors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
