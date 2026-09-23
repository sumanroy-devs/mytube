package io.github.aedev.flow.ui.screens.library

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Pure coverage for the local media search's matching rules. */
class LocalMediaSearchResultsTest {
    private fun videoItem(
        id: Long,
        title: String,
        subtitle: String = "",
    ) = LocalMediaItem(
        id = id,
        contentUri = "content://media/external/video/$id",
        title = title,
        subtitle = subtitle,
        durationMs = 0L,
        sizeBytes = 1L,
        isVideo = true,
    )

    private fun musicItem(
        id: Long,
        title: String,
        subtitle: String = "",
    ) = LocalMediaItem(
        id = id,
        contentUri = "content://media/external/audio/$id",
        title = title,
        subtitle = subtitle,
        durationMs = 60_000L,
        sizeBytes = 1L,
        isVideo = false,
        artworkUri = null,
    )

    @Test
    fun `blank query yields no results`() {
        val items = listOf(videoItem(1, title = "Needle"))

        assertThat(buildLocalMediaSearchResults("   ", items)).isEmpty()
    }

    @Test
    fun `no matches yields no results`() {
        val items = listOf(videoItem(1, title = "Cooking"))

        assertThat(buildLocalMediaSearchResults("piano", items)).isEmpty()
    }

    @Test
    fun `title matches case-insensitively and order is preserved`() {
        val items =
            listOf(
                videoItem(1, title = "Needle in a haystack"),
                videoItem(2, title = "Unrelated"),
                musicItem(3, title = "OTHER NEEDLE SONG"),
            )

        val results = buildLocalMediaSearchResults("needle", items)

        assertThat(results.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    @Test
    fun `subtitle matches for folders and artists`() {
        val items =
            listOf(
                videoItem(1, title = "Clip", subtitle = "Camera"),
                musicItem(2, title = "Track", subtitle = "The NEEDLE band"),
                videoItem(3, title = "Clip", subtitle = "Downloads"),
            )

        val results = buildLocalMediaSearchResults("needle", items)

        assertThat(results.map { it.id }).containsExactly(2L)
    }

    @Test
    fun `query is trimmed before matching`() {
        val items = listOf(musicItem(1, title = "Road Trip"))

        val results = buildLocalMediaSearchResults("  road trip  ", items)

        assertThat(results).hasSize(1)
    }
}
