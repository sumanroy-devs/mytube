package io.github.aedev.flow.data.video.downloader

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.mp4.Mp4Tag
import java.io.File
import java.util.concurrent.TimeUnit

private const val TAG = "AudioMetadataWriter"
private const val MAX_ARTWORK_BYTES = 2 * 1024 * 1024

private val artworkClient by lazy {
    OkHttpClient
        .Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
}

/**
 * Writes title/artist/album/cover-art tags into a finished MP4 download so MediaStore — and any
 * other player — reads real metadata instead of filename guesswork. Must run before the media
 * scan so the indexer picks the tags up. Every failure is logged and swallowed: the audio file
 * itself is already complete, and a missing tag must never fail the download.
 */
internal suspend fun writeAudioMetadata(
    filePath: String,
    title: String,
    artist: String,
    album: String?,
    thumbnailUrl: String?,
) {
    if (!filePath.endsWith(".m4a", ignoreCase = true)) return
    withContext(Dispatchers.IO) {
        runCatching {
            val audioFile = AudioFileIO.read(File(filePath))
            val tag = audioFile.tag ?: audioFile.createDefaultTag()
            title.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.TITLE, it) }
            artist.takeIf { it.isNotBlank() && it != "Unknown" }?.let { tag.setField(FieldKey.ARTIST, it) }
            album?.takeIf { it.isNotBlank() }?.let { tag.setField(FieldKey.ALBUM, it) }
            thumbnailUrl
                ?.let { fetchArtwork(it) }
                ?.let { artwork ->
                    // Artwork has no byte[] overload on Tag.setField — MP4 exposes its own
                    // createArtworkField, which sniffs JPEG/PNG bytes to set the covr datatype.
                    val mp4Tag = tag as? Mp4Tag ?: return@let
                    mp4Tag.setField(mp4Tag.createArtworkField(artwork))
                }
            audioFile.commit()
        }.onFailure {
            Log.w(TAG, "Tag write failed for $filePath (non-fatal)", it)
        }
    }
}

private fun fetchArtwork(url: String): ByteArray? =
    runCatching {
        if (!url.startsWith("http")) return null
        artworkClient
            .newCall(Request.Builder().url(url).build())
            .execute()
            .use { response ->
                if (!response.isSuccessful) return null
                val bytes = response.body?.bytes() ?: return null
                bytes.takeIf { it.size in 1..MAX_ARTWORK_BYTES }
            }
    }.getOrNull()
