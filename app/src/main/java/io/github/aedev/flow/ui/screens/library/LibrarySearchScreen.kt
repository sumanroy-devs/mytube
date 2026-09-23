package io.github.aedev.flow.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aedev.flow.R
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.music.DownloadedTrack
import io.github.aedev.flow.data.music.model.MusicTrack
import io.github.aedev.flow.data.video.DownloadedVideo
import io.github.aedev.flow.ui.components.layout.topbar.FlowSearchTopBar
import io.github.aedev.flow.ui.components.library.LibraryMediaItem
import io.github.aedev.flow.ui.components.music.item.MusicTrackItem
import io.github.aedev.flow.ui.components.shared.CollectionThumbnail
import io.github.aedev.flow.ui.components.shared.FlowEmptyState
import io.github.aedev.flow.ui.components.shared.MediaRow
import io.github.aedev.flow.ui.components.shared.MediaThumbnail

private val ListContentPadding = PaddingValues(vertical = 12.dp)

@Composable
fun LibrarySearchScreen(
    onBack: () -> Unit,
    onVideoClick: (Video) -> Unit,
    onMusicClick: (MusicTrack, List<MusicTrack>, String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMusicPlaylistClick: (String) -> Unit,
    onDownloadedVideoClick: (List<DownloadedVideo>, Int) -> Unit,
    onDownloadedMusicClick: (List<DownloadedTrack>, Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibrarySearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sections = state?.sections
    val background = MaterialTheme.colorScheme.background

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            FlowSearchTopBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClose = onBack,
                placeholder = stringResource(R.string.library_search_placeholder),
            )
        },
    ) { padding ->
        val contentModifier = modifier.fillMaxSize().padding(padding).background(background)
        when {
            query.isBlank() ->
                FlowEmptyState(
                    title = stringResource(R.string.library_search_prompt),
                    icon = Icons.Outlined.Search,
                    modifier = contentModifier,
                )

            // The query is live but its source lists have not landed; stay blank rather than flash "no results".
            sections == null -> Unit

            sections.isEmpty() ->
                FlowEmptyState(
                    title = stringResource(R.string.no_results_found),
                    icon = Icons.Outlined.Search,
                    modifier = contentModifier,
                )

            else ->
                LazyColumn(
                    modifier = contentModifier,
                    contentPadding = ListContentPadding,
                ) {
                    sections.forEach { section ->
                        val musicQueue =
                            section.rows.mapNotNull { row ->
                                ((row as? LibrarySearchRow.Media)?.item as? LibraryMediaItem.MusicItem)?.track
                            }
                        val downloadedVideoQueue =
                            section.rows.mapNotNull { row ->
                                ((row as? LibrarySearchRow.Media)?.item as? LibraryMediaItem.DownloadedVideoItem)?.download
                            }
                        val downloadedMusicQueue =
                            section.rows.mapNotNull { row ->
                                ((row as? LibrarySearchRow.Media)?.item as? LibraryMediaItem.DownloadedMusicItem)?.download
                            }

                        item(key = "${section.id}:header", contentType = "section-header") {
                            LibrarySearchSectionHeader(title = stringResource(section.titleRes))
                        }

                        items(
                            items = section.rows,
                            key = { "${section.id}:${searchRowKey(it)}" },
                            contentType = { searchRowContentType(it) },
                        ) { row ->
                            val sectionTitle = stringResource(section.titleRes)
                            when (row) {
                                is LibrarySearchRow.Playlist -> {
                                    val playlist = row.playlist
                                    MediaRow(
                                        title = playlist.name,
                                        subtitle =
                                            if (row.isMusic) {
                                                stringResource(R.string.tracks_count_template, playlist.videoCount)
                                            } else {
                                                pluralStringResource(
                                                    R.plurals.videos_count_template,
                                                    playlist.videoCount,
                                                    playlist.videoCount,
                                                )
                                            },
                                        onClick = {
                                            if (row.isMusic) {
                                                onMusicPlaylistClick(playlist.id)
                                            } else {
                                                onPlaylistClick(playlist.id)
                                            }
                                        },
                                        thumbnail = {
                                            CollectionThumbnail(
                                                thumbnailUrl = playlist.thumbnailUrl,
                                                placeholder = Icons.AutoMirrored.Outlined.PlaylistPlay,
                                            )
                                        },
                                    )
                                }

                                is LibrarySearchRow.Media ->
                                    when (val item = row.item) {
                                        is LibraryMediaItem.VideoItem -> {
                                            val video = item.video
                                            MediaRow(
                                                title = video.title,
                                                subtitle = video.channelName,
                                                onClick = { onVideoClick(video) },
                                                thumbnail = {
                                                    MediaThumbnail(
                                                        videoId = video.id,
                                                        thumbnailUrl = video.thumbnailUrl,
                                                        durationSeconds = video.duration,
                                                        showWatchProgress = true,
                                                    )
                                                },
                                            )
                                        }

                                        is LibraryMediaItem.MusicItem ->
                                            MusicTrackItem(
                                                track = item.track,
                                                onClick = { onMusicClick(item.track, musicQueue, sectionTitle) },
                                                showMenu = false,
                                            )

                                        is LibraryMediaItem.DownloadedVideoItem -> {
                                            val video = item.download.video
                                            MediaRow(
                                                title = video.title,
                                                subtitle = video.channelName,
                                                onClick = {
                                                    val index = downloadedVideoQueue.indexOfFirst { it.video.id == video.id }
                                                    if (index >= 0) onDownloadedVideoClick(downloadedVideoQueue, index)
                                                },
                                                thumbnail = {
                                                    MediaThumbnail(
                                                        videoId = video.id,
                                                        thumbnailUrl = video.thumbnailUrl,
                                                        durationSeconds = video.duration,
                                                        showWatchProgress = true,
                                                    )
                                                },
                                            )
                                        }

                                        is LibraryMediaItem.DownloadedMusicItem -> {
                                            val track = item.download.track
                                            MusicTrackItem(
                                                track = track,
                                                onClick = {
                                                    val index =
                                                        downloadedMusicQueue.indexOfFirst { it.track.videoId == track.videoId }
                                                    if (index >= 0) onDownloadedMusicClick(downloadedMusicQueue, index)
                                                },
                                                isDownloaded = true,
                                                showMenu = false,
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

@Composable
private fun LibrarySearchSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

private fun searchRowKey(row: LibrarySearchRow): String =
    when (row) {
        is LibrarySearchRow.Playlist ->
            if (row.isMusic) "music-playlist:${row.playlist.id}" else "playlist:${row.playlist.id}"
        is LibrarySearchRow.Media -> row.item.key
    }

private fun searchRowContentType(row: LibrarySearchRow): String =
    when (row) {
        is LibrarySearchRow.Playlist -> "playlist"
        is LibrarySearchRow.Media ->
            when (row.item) {
                is LibraryMediaItem.VideoItem, is LibraryMediaItem.DownloadedVideoItem -> "video"
                is LibraryMediaItem.MusicItem, is LibraryMediaItem.DownloadedMusicItem -> "music"
            }
    }
