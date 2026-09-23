package io.github.aedev.flow.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aedev.flow.R
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.music.DownloadedTrack
import io.github.aedev.flow.data.music.model.MusicTrack
import io.github.aedev.flow.data.video.DownloadedVideo
import io.github.aedev.flow.ui.components.layout.topbar.FlowTopBar
import io.github.aedev.flow.ui.components.library.LibraryNavigationRow
import io.github.aedev.flow.ui.components.shared.FlowEmptyState

private val ListContentPadding = PaddingValues(vertical = 12.dp)
private val ShelfSpacing = 24.dp

@Composable
fun LibraryScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    onNavigateToLikedVideos: () -> Unit,
    onNavigateToWatchLater: () -> Unit,
    onNavigateToSavedShorts: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToLocalMedia: () -> Unit,
    onSearchClick: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onMusicClick: (MusicTrack, List<MusicTrack>, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMusicPlaylistClick: (String) -> Unit,
    onDownloadedVideoClick: (List<DownloadedVideo>, Int) -> Unit,
    onDownloadedMusicClick: (List<DownloadedTrack>, Int) -> Unit,
    onSavedShortClick: (Video) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val historyTitle = stringResource(R.string.library_history_label)
    val playlistsTitle = stringResource(R.string.library_playlists_label)
    val likesTitle = stringResource(R.string.library_liked_videos_label)
    val downloadsTitle = stringResource(R.string.library_downloads_label)
    val watchLaterTitle = stringResource(R.string.library_watch_later_label)
    val savedShortsTitle = stringResource(R.string.library_saved_shorts_label)
    val shortsEnabled by viewModel.shortsEnabled.collectAsStateWithLifecycle()
    val isLibraryEmpty by viewModel.isLibraryEmpty.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            FlowTopBar(
                title = stringResource(R.string.library),
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                    IconButton(onClick = onNavigateToLocalMedia) {
                        Icon(
                            imageVector = Icons.Outlined.PermMedia,
                            contentDescription = stringResource(R.string.library_local_media_label),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
            contentPadding = ListContentPadding,
            verticalArrangement = Arrangement.spacedBy(ShelfSpacing),
        ) {
            if (isLibraryEmpty) {
                item(key = "library-empty", contentType = "empty") {
                    FlowEmptyState(
                        title = stringResource(R.string.library_empty_title),
                        subtitle = stringResource(R.string.library_empty_body),
                        icon = Icons.Outlined.VideoLibrary,
                    )
                }
            } else {
                item(key = "history", contentType = "media-shelf") {
                    LibraryMediaShelfRoute(
                        title = historyTitle,
                        itemsFlow = viewModel.history,
                        sourceName = historyTitle,
                        onTitleClick = onNavigateToHistory,
                        onVideoClick = onVideoClick,
                        onMusicClick = onMusicClick,
                        onDownloadedVideoClick = onDownloadedVideoClick,
                        onDownloadedMusicClick = onDownloadedMusicClick,
                    )
                }

                item(key = "playlists", contentType = "playlist-shelf") {
                    LibraryPlaylistsShelf(
                        title = playlistsTitle,
                        videoPlaylistsFlow = viewModel.playlists,
                        musicPlaylistsFlow = viewModel.musicPlaylists,
                        onTitleClick = onNavigateToPlaylists,
                        onVideoPlaylistClick = onPlaylistClick,
                        onMusicPlaylistClick = onMusicPlaylistClick,
                    )
                }

                item(key = "watch-later", contentType = "video-shelf") {
                    LibraryVideoShelf(
                        title = watchLaterTitle,
                        videosFlow = viewModel.watchLater,
                        onTitleClick = onNavigateToWatchLater,
                        onVideoClick = onVideoClick,
                    )
                }

                item(key = "likes", contentType = "media-shelf") {
                    LibraryMediaShelfRoute(
                        title = likesTitle,
                        itemsFlow = viewModel.likes,
                        sourceName = likesTitle,
                        onTitleClick = onNavigateToLikedVideos,
                        onVideoClick = onVideoClick,
                        onMusicClick = onMusicClick,
                        onDownloadedVideoClick = onDownloadedVideoClick,
                        onDownloadedMusicClick = onDownloadedMusicClick,
                    )
                }

                item(key = "downloads", contentType = "media-shelf") {
                    LibraryMediaShelfRoute(
                        title = downloadsTitle,
                        itemsFlow = viewModel.downloads,
                        sourceName = downloadsTitle,
                        onTitleClick = onNavigateToDownloads,
                        onVideoClick = onVideoClick,
                        onMusicClick = onMusicClick,
                        onDownloadedVideoClick = onDownloadedVideoClick,
                        onDownloadedMusicClick = onDownloadedMusicClick,
                    )
                }

                if (shortsEnabled) {
                    item(key = "saved-shorts", contentType = "shorts-shelf") {
                        LibraryShortsShelfRoute(
                            title = savedShortsTitle,
                            shortsFlow = viewModel.savedShorts,
                            onTitleClick = onNavigateToSavedShorts,
                            onShortClick = onSavedShortClick,
                        )
                    }
                }
            }
        }
    }
}
