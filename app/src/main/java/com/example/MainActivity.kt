package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.VideoItem
import com.example.ui.IlmTab
import com.example.ui.IlmViewModel
import com.example.ui.components.IlmTopBar
import com.example.ui.components.IslamicStarPatternBackground
import com.example.ui.components.UserNameDialog
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.VideoPlayerOverlay
import com.example.ui.theme.IlmTheme
import com.example.ui.theme.LocalIlmColors
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: IlmViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                IlmTheme(darkTheme = uiState.isDarkTheme) {
                    IlmAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

private data class NavTabSpec(
    val tab: IlmTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

private val NavTabs = listOf(
    NavTabSpec(IlmTab.HOME, R.string.tab_home, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    NavTabSpec(IlmTab.EXPLORE, R.string.tab_explore, Icons.Rounded.Explore, Icons.Outlined.Explore, "nav_explore"),
    NavTabSpec(IlmTab.SAVED, R.string.tab_saved, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder, "nav_saved"),
    NavTabSpec(IlmTab.MORE, R.string.tab_more, Icons.Rounded.Settings, Icons.Outlined.Settings, "nav_more")
)

@Composable
fun IlmAppRoot(viewModel: IlmViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val ilmColors = LocalIlmColors.current
    var showUserNameDialog by remember { mutableStateOf(false) }

    val shareVideo: (VideoItem) -> Unit = { video ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, video.title)
            putExtra(Intent.EXTRA_TEXT, "${video.title}\n${video.scholar}\n${video.youtubeUrl}")
        }
        context.startActivity(Intent.createChooser(intent, video.title))
    }

    val shareText: (String) -> Unit = { text ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة المحتوى"))
    }

    LaunchedEffect(uiState.feedbackMessage) {
        if (uiState.feedbackMessage != null) {
            delay(2000)
            viewModel.consumeFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!useNavigationRail && uiState.moreSubPage == null) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                            tonalElevation = 0.dp
                        ) {
                            NavTabs.forEach { spec ->
                                val selected = uiState.selectedTab == spec.tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(spec.tab) },
                                    icon = {
                                        if (spec.tab == IlmTab.SAVED && uiState.savedCount > 0) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(
                                                        containerColor = ilmColors.heart,
                                                        contentColor = androidx.compose.ui.graphics.Color.White
                                                    ) {
                                                        Text("${uiState.savedCount}")
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                                    contentDescription = stringResource(spec.labelRes)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                                contentDescription = stringResource(spec.labelRes)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(spec.labelRes),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ilmColors.gold,
                                        selectedTextColor = ilmColors.gold,
                                        indicatorColor = ilmColors.gold.copy(alpha = 0.16f),
                                        unselectedIconColor = ilmColors.mutedText,
                                        unselectedTextColor = ilmColors.mutedText
                                    ),
                                    modifier = Modifier.testTag(spec.tag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    IslamicStarPatternBackground()

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                NavTabs.forEach { spec ->
                                    val selected = uiState.selectedTab == spec.tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(spec.tab) },
                                        icon = {
                                            if (spec.tab == IlmTab.SAVED && uiState.savedCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = ilmColors.heart,
                                                            contentColor = androidx.compose.ui.graphics.Color.White
                                                        ) {
                                                            Text("${uiState.savedCount}")
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                                        contentDescription = stringResource(spec.labelRes)
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                                    contentDescription = stringResource(spec.labelRes)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(spec.labelRes),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = ilmColors.gold,
                                            selectedTextColor = ilmColors.gold,
                                            indicatorColor = ilmColors.gold.copy(alpha = 0.16f),
                                            unselectedIconColor = ilmColors.mutedText,
                                            unselectedTextColor = ilmColors.mutedText
                                        ),
                                        modifier = Modifier.testTag(spec.tag)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.fillMaxSize()) {
                            if (uiState.moreSubPage == null) {
                                IlmTopBar(
                                    watchedCount = uiState.watchedCount,
                                    totalCount = uiState.allVideos.size,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = viewModel::updateSearchQuery,
                                    showSearchBar = uiState.selectedTab != IlmTab.MORE
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                AnimatedContent(
                                    targetState = uiState.selectedTab,
                                    transitionSpec = {
                                        (fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 32 })
                                            .togetherWith(fadeOut(tween(170)))
                                    },
                                    label = "tab_transition"
                                ) { currentTab ->
                                    when (currentTab) {
                                        IlmTab.HOME -> HomeScreen(
                                            uiState = uiState,
                                            onSelectScholar = viewModel::selectScholar,
                                            onSelectCategory = viewModel::selectCategory,
                                            onOpenPlayer = { video, queue -> viewModel.openPlayer(video.key, queue) },
                                            onToggleSave = viewModel::toggleSaved,
                                            onToggleWatched = viewModel::toggleWatched,
                                            onShare = shareVideo,
                                            onOpenFavoritesTab = { viewModel.selectTab(IlmTab.SAVED) },
                                            onEditUserName = { showUserNameDialog = true }
                                        )

                                        IlmTab.EXPLORE -> ExploreScreen(
                                            uiState = uiState,
                                            onClearSearch = { viewModel.updateSearchQuery("") },
                                            onOpenPlayer = { video, queue -> viewModel.openPlayer(video.key, queue) }
                                        )

                                        IlmTab.SAVED -> SavedScreen(
                                            uiState = uiState,
                                            onOpenPlayer = { video, queue -> viewModel.openPlayer(video.key, queue) },
                                            onToggleSave = viewModel::toggleSaved,
                                            onToggleWatched = viewModel::toggleWatched,
                                            onShare = shareVideo,
                                            onExploreClick = { viewModel.selectTab(IlmTab.EXPLORE) }
                                        )

                                        IlmTab.MORE -> MoreScreen(
                                            uiState = uiState,
                                            onOpenSubPage = viewModel::openMoreSubPage,
                                            onAddVideo = viewModel::addSingleVideo,
                                            onImportYouTube = viewModel::importFromYouTube,
                                            onSyncCatalog = viewModel::syncCatalogFromWebsite,
                                            onToggleTheme = viewModel::toggleTheme,
                                            onClearProgress = viewModel::clearAllProgress,
                                            onBuildExportJson = viewModel::buildExportJson,
                                            onShareText = shareText,
                                            onFeedback = viewModel::showFeedback,
                                            onEditUserName = { showUserNameDialog = true },
                                            onSetReminderEnabled = viewModel::setReminderEnabled,
                                            onSetReminderTime = viewModel::setReminderTime,
                                            onSendTestReminder = viewModel::sendTestReminderNotification
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Video Player Modal Overlay
        val currentVideo = uiState.allVideos.find { it.key == uiState.player.currentKey }
        val upNextVideos = if (currentVideo != null) {
            val videosMap = uiState.allVideos.associateBy { it.key }
            val q = uiState.player.queueKeys
            val idx = q.indexOf(currentVideo.key)
            if (idx >= 0) {
                q.drop(idx + 1).take(12).mapNotNull { videosMap[it] }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        VideoPlayerOverlay(
            isOpen = uiState.player.isOpen,
            currentVideo = currentVideo,
            upNextVideos = upNextVideos,
            onClose = viewModel::closePlayer,
            onSelectVideo = { viewModel.switchPlayerVideo(it.key) },
            onToggleSave = viewModel::toggleSaved,
            onToggleWatched = viewModel::toggleWatched,
            onAutoWatched = viewModel::markWatchedSilently,
            onVideoEnded = viewModel::playNextInQueue,
            onShare = shareVideo
        )

        // Floating Toast Feedback Pill
        AnimatedVisibility(
            visible = uiState.feedbackMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 86.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ilmColors.watched,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = uiState.feedbackMessage.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = ilmColors.onWatched,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        if (showUserNameDialog) {
            UserNameDialog(
                currentName = uiState.userName,
                onDismiss = { showUserNameDialog = false },
                onConfirm = { newName ->
                    viewModel.updateUserName(newName)
                    showUserNameDialog = false
                }
            )
        }
    }
}
