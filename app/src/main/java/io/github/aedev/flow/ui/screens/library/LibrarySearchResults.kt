package io.github.aedev.flow.ui.screens.library

import androidx.annotation.StringRes
import io.github.aedev.flow.R
import io.github.aedev.flow.data.local.LikedVideoInfo
import io.github.aedev.flow.data.local.VideoHistoryEntry
import io.github.aedev.flow.data.model.PlaylistInfo
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.music.DownloadedTrack
import io.github.aedev.flow.data.video.DownloadedVideo
import io.github.aedev.flow.ui.components.library.LibraryMediaItem
import io.github.aedev.flow.ui.components.library.toLibraryMediaItem

/** Raw, uncapped library sources the search runs against. */
internal data class LibrarySearchSources(
    val history: List<VideoHistoryEntry> = emptyList(),
    val videoPlaylists: List<PlaylistInfo> = emptyList(),
    val musicPlaylists: List<PlaylistInfo> = emptyList(),
    val watchLater: List<Video> = emptyList(),
    val likes: List<LikedVideoInfo> = emptyList(),
    val downloadedVideos: List<DownloadedVideo> = emptyList(),
    val downloadedTracks: List<DownloadedTrack> = emptyList(),
    val savedShorts: List<Video> = emptyList(),
)

/** The live search; `sections` is null until a non-blank query has sources to run against. */
internal data class LibrarySearchState(
    val query: String,
    val sections: List<LibrarySearchSection>?,
)

internal data class LibrarySearchSection(
    val id: String,
    @StringRes val titleRes: Int,
    val rows: List<LibrarySearchRow>,
)

internal sealed interface LibrarySearchRow {
    data class Media(
        val item: LibraryMediaItem,
    ) : LibrarySearchRow

    data class Playlist(
        val playlist: PlaylistInfo,
        val isMusic: Boolean,
    ) : LibrarySearchRow
}

/**
 * Groups every source whose title (or channel/artist) contains [query] case-insensitively —
 * playlists by name only — keeping the Library's section order and dropping empty sections.
 * A blank query yields no sections.
 */
internal fun buildLibrarySearchSections(
    query: String,
    sources: LibrarySearchSources,
): List<LibrarySearchSection> {
    val needle = query.trim()
    if (needle.isEmpty()) return emptyList()

    fun matches(
        title: String,
        subtitle: String,
    ) = title.contains(needle, ignoreCase = true) || subtitle.contains(needle, ignoreCase = true)

    fun nameMatches(name: String) = name.contains(needle, ignoreCase = true)

    val sections = mutableListOf<LibrarySearchSection>()

    val history =
        sources.history
            .filter { matches(it.title, it.channelName) }
            .map { LibrarySearchRow.Media(it.toLibraryMediaItem()) }
    if (history.isNotEmpty()) {
        sections += LibrarySearchSection("history", R.string.library_history_label, history)
    }

    val playlists =
        sources.videoPlaylists
            .filter { nameMatches(it.name) }
            .map { LibrarySearchRow.Playlist(it, isMusic = false) } +
            sources.musicPlaylists
                .filter { nameMatches(it.name) }
                .map { LibrarySearchRow.Playlist(it, isMusic = true) }
    if (playlists.isNotEmpty()) {
        sections += LibrarySearchSection("playlists", R.string.library_playlists_label, playlists)
    }

    val watchLater =
        sources.watchLater
            .filter { matches(it.title, it.channelName) }
            .map { LibrarySearchRow.Media(LibraryMediaItem.VideoItem(it)) }
    if (watchLater.isNotEmpty()) {
        sections += LibrarySearchSection("watch-later", R.string.library_watch_later_label, watchLater)
    }

    val likes =
        sources.likes
            .filter { matches(it.title, it.channelName) }
            .map { LibrarySearchRow.Media(it.toLibraryMediaItem()) }
    if (likes.isNotEmpty()) {
        sections += LibrarySearchSection("likes", R.string.library_liked_videos_label, likes)
    }

    val downloads =
        (
            sources.downloadedVideos
                .filter { matches(it.video.title, it.video.channelName) }
                .map { it.downloadedAt to LibrarySearchRow.Media(LibraryMediaItem.DownloadedVideoItem(it)) } +
                sources.downloadedTracks
                    .filter { matches(it.track.title, it.track.artist) }
                    .map { it.downloadedAt to LibrarySearchRow.Media(LibraryMediaItem.DownloadedMusicItem(it)) }
        ).sortedByDescending { it.first }
            .map { it.second }
    if (downloads.isNotEmpty()) {
        sections += LibrarySearchSection("downloads", R.string.library_downloads_label, downloads)
    }

    val savedShorts =
        sources.savedShorts
            .filter { matches(it.title, it.channelName) }
            .map { LibrarySearchRow.Media(LibraryMediaItem.VideoItem(it)) }
    if (savedShorts.isNotEmpty()) {
        sections += LibrarySearchSection("saved-shorts", R.string.library_saved_shorts_label, savedShorts)
    }

    return sections
}
