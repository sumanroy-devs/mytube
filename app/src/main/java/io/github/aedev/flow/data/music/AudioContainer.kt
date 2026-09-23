package io.github.aedev.flow.data.music

/**
 * The container a selected YouTube audio stream actually lives in. Downloads used to be hard-coded
 * to `.mp3`/`audio/mpeg` while the bytes were MP4 or WebM, so the extension and MIME type are
 * derived from the stream's real `mimeType` instead.
 */
internal data class AudioContainer(
    val extension: String,
    val mimeType: String,
)

internal fun audioContainerFor(formatMimeType: String): AudioContainer =
    when {
        formatMimeType.contains("mp4", ignoreCase = true) -> AudioContainer("m4a", "audio/mp4")
        formatMimeType.contains("webm", ignoreCase = true) -> AudioContainer("webm", "audio/webm")
        else -> AudioContainer("m4a", "audio/mp4")
    }
