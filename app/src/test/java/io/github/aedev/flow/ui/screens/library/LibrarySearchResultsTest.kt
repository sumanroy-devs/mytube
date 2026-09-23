package io.github.aedev.flow.ui.screens.library

import com.google.common.truth.Truth.assertThat
import io.github.aedev.flow.R
import io.github.aedev.flow.data.local.LikedVideoInfo
import io.github.aedev.flow.data.local.VideoHistoryEntry
import io.github.aedev.flow.data.model.PlaylistInfo
import io.github.aedev.flow.data.model.Video
import io.github.aedev.flow.data.music.DownloadedTrack
import io.github.aedev.flow.data.music.model.MusicTrack
import io.github.aedev.flow.data.video.DownloadedVideo
import io.github.aedev.flow.ui.components.library.LibraryMediaItem
import org.junit.Test

/** Pure coverage for the library search's grouping, matching and ordering rules. */
class LibrarySearchResultsTest {
    private fun video(
        id: String,
        title: String = id,
        channelName: String = "Channel",
    ) = Video(
        id = id,
        title = title,
        channelName = channelName,
        channelId = "channel-$id",
        thumbnailUrl = "",
        duration = 0,
        viewCount = 0L,
        uploadDate = "",
    )

    private fun history(
        id: String,
        title: String = id,
        channelName: String = "Channel",
        isMusic: Boolean = false,
    ) = VideoHistoryEntry(
        videoId = id,
        position = 0L,
        duration = 0L,
        timestamp = 0L,
        title = title,
        thumbnailUrl = "",
        channelName = channelName,
        isMusic = isMusic,
    )

    private fun liked(
        id: String,
        title: String = id,
    ) = LikedVideoInfo(videoId = id, title = title, thumbnail = "", channelName = "Channel")

    private fun playlist(
        id: String,
        name: String,
        description: String = "",
    ) = PlaylistInfo(
        id = id,
        name = name,
        description = description,
        videoCount = 1,
        thumbnailUrl = "",
        isPrivate = false,
        createdAt = 0L,
    )

    private fun downloadedVideo(
        id: String,
        downloadedAt: Long,
        title: String = id,
    ) = DownloadedVideo(video = video(id, title), filePath = "/tmp/$id", downloadedAt = downloadedAt)

    private fun downloadedTrack(
        videoId: String,
        downloadedAt: Long,
        title: String = videoId,
    ) = DownloadedTrack(
        track = MusicTrack(videoId = videoId, title = title, artist = "Artist", thumbnailUrl = "", duration = 0),
        filePath = "/tmp/$videoId",
        downloadedAt = downloadedAt,
    )

    @Test
    fun `blank query yields no sections`() {
        val sources = LibrarySearchSources(history = listOf(history("h1", title = "Needle")))

        assertThat(buildLibrarySearchSections("   ", sources)).isEmpty()
    }

    @Test
    fun `no matches yields no sections`() {
        val sources = LibrarySearchSources(history = listOf(history("h1", title = "Cooking")))

        assertThat(buildLibrarySearchSections("piano", sources)).isEmpty()
    }

    @Test
    fun `history matches on title case-insensitively and keeps its label`() {
        val sources =
            LibrarySearchSources(
                history =
                    listOf(
                        history("h1", title = "Needle in a haystack"),
                        history("h2", title = "Unrelated"),
                        history("h3", title = "OTHER", channelName = "the NEEDLE band", isMusic = true),
                    ),
            )

        val sections = buildLibrarySearchSections("needle", sources)

        assertThat(sections).hasSize(1)
        assertThat(sections[0].id).isEqualTo("history")
        assertThat(sections[0].titleRes).isEqualTo(R.string.library_history_label)
        assertThat(sections[0].rows).hasSize(2)
        val videoRow = sections[0].rows[0] as LibrarySearchRow.Media
        assertThat(videoRow.item).isInstanceOf(LibraryMediaItem.VideoItem::class.java)
        val musicRow = sections[0].rows[1] as LibrarySearchRow.Media
        assertThat(musicRow.item).isInstanceOf(LibraryMediaItem.MusicItem::class.java)
    }

    @Test
    fun `playlists match by name only and carry the music flag`() {
        val sources =
            LibrarySearchSources(
                videoPlaylists =
                    listOf(
                        playlist("p1", name = "Road Trip"),
                        playlist("p2", name = "Unrelated", description = "road trip mix"),
                    ),
                musicPlaylists = listOf(playlist("m1", name = "Road Trip Anthems")),
            )

        val rows = buildLibrarySearchSections("road trip", sources).single().rows

        assertThat(rows).hasSize(2)
        assertThat((rows[0] as LibrarySearchRow.Playlist).playlist.id).isEqualTo("p1")
        assertThat((rows[0] as LibrarySearchRow.Playlist).isMusic).isFalse()
        assertThat((rows[1] as LibrarySearchRow.Playlist).playlist.id).isEqualTo("m1")
        assertThat((rows[1] as LibrarySearchRow.Playlist).isMusic).isTrue()
    }

    @Test
    fun `sections follow library order and drop the empty ones`() {
        val sources =
            LibrarySearchSources(
                history = listOf(history("h1", title = "needle")),
                videoPlaylists = listOf(playlist("p1", name = "needle list")),
                watchLater = listOf(video("v1", title = "needle")),
                likes = listOf(liked("l1", title = "needle cut")),
                downloadedVideos = listOf(downloadedVideo("d1", downloadedAt = 1L, title = "needle stream")),
                savedShorts = listOf(video("s1", title = "needle short")),
            )

        val all = buildLibrarySearchSections("needle", sources)
        assertThat(all.map { it.id })
            .containsExactly("history", "playlists", "watch-later", "likes", "downloads", "saved-shorts")
            .inOrder()

        val downloadsOnly = buildLibrarySearchSections("needle stream", sources)
        assertThat(downloadsOnly.map { it.id }).containsExactly("downloads")
    }

    @Test
    fun `downloads merge videos and tracks newest first`() {
        val sources =
            LibrarySearchSources(
                downloadedVideos =
                    listOf(
                        downloadedVideo("v-old", downloadedAt = 100L, title = "Needle old"),
                        downloadedVideo("v-new", downloadedAt = 300L, title = "Needle new"),
                    ),
                downloadedTracks = listOf(downloadedTrack("t-mid", downloadedAt = 200L, title = "Needle song")),
            )

        val rows = buildLibrarySearchSections("needle", sources).single().rows
        val ordered =
            rows.map {
                when (val item = (it as LibrarySearchRow.Media).item) {
                    is LibraryMediaItem.DownloadedVideoItem -> item.download.video.id
                    is LibraryMediaItem.DownloadedMusicItem -> item.download.track.videoId
                    else -> error("unexpected download row: $item")
                }
            }

        assertThat(ordered).containsExactly("v-new", "t-mid", "v-old").inOrder()
    }

    @Test
    fun `saved shorts arrive as plain video items`() {
        val sources = LibrarySearchSources(savedShorts = listOf(video("s1", title = "Short needle")))

        val mediaRow =
            buildLibrarySearchSections("needle", sources).single().rows.single() as LibrarySearchRow.Media

        assertThat(mediaRow.item).isInstanceOf(LibraryMediaItem.VideoItem::class.java)
    }
}
