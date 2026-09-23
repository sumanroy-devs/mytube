package io.github.aedev.flow.ui.screens.library

/** The live local-media search; `results` is null until a non-blank query has a scan to run against. */
internal data class LocalMediaSearchState(
    val query: String,
    val results: List<LocalMediaItem>?,
)

/**
 * Filters [items] down to those whose title (or folder/artist subtitle) contains [query]
 * case-insensitively, preserving MediaStore order. A blank query yields no results.
 */
internal fun buildLocalMediaSearchResults(
    query: String,
    items: List<LocalMediaItem>,
): List<LocalMediaItem> {
    val needle = query.trim()
    if (needle.isEmpty()) return emptyList()
    return items.filter {
        it.title.contains(needle, ignoreCase = true) || it.subtitle.contains(needle, ignoreCase = true)
    }
}
