package io.github.aedev.flow.update

import android.content.Context
import android.os.Build
import io.github.aedev.flow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Serializable
data class Release(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String,
    @SerialName("body") val body: String = "",
    @SerialName("published_at") val publishedAt: String,
    @SerialName("assets") val assets: List<Asset>,
    @SerialName("html_url") val htmlUrl: String = "",
)

@Serializable
data class Asset(
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("name") val name: String,
    @SerialName("size") val size: Long,
    @SerialName("content_type") val contentType: String,
)

class UpdateManager(
    private val context: Context,
) {
    companion object {
        const val RELEASE_PAGE_URL = "https://github.com/sumanroy-devs/mytube/releases/latest"
        const val PREFS_NAME = "flow_update_prefs"
        const val KEY_IGNORED_VERSION = "ignored_version"
        const val KEY_AUTO_UPDATE = "auto_update"
        const val KEY_LAST_CHECK = "last_update_check"

        private const val API_URL = "https://api.github.com/repos/sumanroy-devs/mytube/releases/latest"

        /**
         * True only for the shipped release package: .debug installs carry a
         * different applicationId, so a release APK would land as a separate app instead
         * of updating this one.
         */
        val isUpdateActive: Boolean
            get() = BuildConfig.ENABLE_UPDATE_FEATURE && BuildConfig.APPLICATION_ID == BuildConfig.RELEASE_APPLICATION_ID
    }

    private val client = OkHttpClient()
    private val json =
        Json {
            ignoreUnknownKeys = true
            // GitHub may send null for empty release fields (e.g. "body")
            coerceInputValues = true
        }

    suspend fun checkForUpdate(forceShow: Boolean = false): Release? {
        // Disabled builds or packages that can't update in place never see a release
        if (!isUpdateActive) {
            return null
        }

        val release = getLatestRelease(API_URL) ?: return null
        val currentVersion = UpdateVersion.normalize(BuildConfig.VERSION_NAME)
        val remoteVersion = UpdateVersion.normalize(release.tagName)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ignoredVersion = prefs.getString(KEY_IGNORED_VERSION, null)

        // If this version was ignored, don't show it unless forced (manual check)
        if (!forceShow && ignoredVersion == remoteVersion) {
            return null
        }

        return if (UpdateVersion.isNewer(remoteVersion, currentVersion)) {
            release
        } else {
            null
        }
    }

    fun ignoreVersion(version: String) {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs
            .edit()
            .putString(KEY_IGNORED_VERSION, UpdateVersion.normalize(version))
            .apply()
    }

    private suspend fun getLatestRelease(url: String): Release? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            // 404 = no releases published yet (fresh repo) — not a connection problem
            if (response.code == 404) {
                response.close()
                return@withContext null
            }
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body.string()
            if (responseBody.isEmpty()) throw IOException("Empty body")
            json.decodeFromString<Release>(responseBody)
        }

    /** True when at least one release asset can be installed on this device. */
    fun hasCompatibleApk(release: Release): Boolean = selectBestApkAsset(release.assets) != null

    /**
     * Release page URL — fallback when the release has no compatible APK asset,
     * so the update flow never dead-ends.
     */
    fun getReleasePageUrl(release: Release): String = release.htmlUrl.ifBlank { RELEASE_PAGE_URL }

    fun downloadUpdate(release: Release): Flow<Float> {
        // Return completed flow immediately if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return flowOf(100f)
        }

        val asset =
            selectBestApkAsset(release.assets)
                ?: throw Exception("No compatible APK asset found")

        val destination = File(context.externalCacheDir, asset.name)
        return downloadApk(asset.downloadUrl, destination, asset.size)
    }

    private fun selectBestApkAsset(assets: List<Asset>): Asset? {
        val deviceArch = getDeviceArchitecture()

        // First, try to find architecture-specific APK
        val archSpecificApk =
            assets.firstOrNull { asset ->
                asset.name.endsWith(".apk") && nameMatchesArch(asset.name, deviceArch)
            }
        if (archSpecificApk != null) {
            return archSpecificApk
        }

        // Fallback to universal APK
        val universalApk =
            assets.firstOrNull { asset ->
                asset.name.endsWith(".apk") && nameMatchesArch(asset.name, "universal")
            }
        if (universalApk != null) {
            return universalApk
        }

        // Last resort: any APK
        return assets.firstOrNull { it.name.endsWith(".apk") }
    }

    /**
     * Matches an asset name against an ABI with token boundaries so "x86" never
     * matches an "x86_64" APK ("mytube-x86_64-v1.3.0.apk" must not be picked for x86).
     */
    private fun nameMatchesArch(
        assetName: String,
        arch: String,
    ): Boolean =
        Regex("(^|[-.])${Regex.escape(arch)}([-.]|$)", RegexOption.IGNORE_CASE)
            .containsMatchIn(assetName)

    private fun getDeviceArchitecture(): String {
        // Map the primary device ABI to the APK naming convention
        return when (Build.SUPPORTED_ABIS.firstOrNull()) {
            "arm64-v8a" -> "arm64-v8a"
            "armeabi-v7a" -> "armeabi-v7a"
            "x86" -> "x86"
            "x86_64" -> "x86_64"
            else -> "universal" // Fallback for unknown architectures
        }
    }

    private fun downloadApk(
        url: String,
        destination: File,
        expectedSize: Long,
    ): Flow<Float> =
        flow {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                response.close()
                throw IOException("Unexpected code $response")
            }

            val body = response.body
            val contentLength = body.contentLength()
            // Stream into a side file so an interrupted download never leaves a truncated
            // APK at the final path where getApkFile() would offer it as ready to install.
            val partFile = File(destination.parentFile, "${destination.name}.part")
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(partFile)
            var totalBytesRead: Long = 0

            try {
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    val progress =
                        if (contentLength > 0) {
                            (totalBytesRead.toFloat() / contentLength.toFloat()) * 100
                        } else {
                            -1f
                        }
                    emit(progress)
                }
                outputStream.flush()
            } finally {
                inputStream.close()
                outputStream.close()
            }

            if (expectedSize > 0 && totalBytesRead != expectedSize) {
                partFile.delete()
                throw IOException("Incomplete download: got $totalBytesRead of $expectedSize bytes")
            }
            if (!partFile.renameTo(destination)) {
                partFile.delete()
                throw IOException("Could not move downloaded file into place")
            }
            emit(100f)
        }.flowOn(Dispatchers.IO)

    fun getApkFile(release: Release): File? {
        // Return null if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return null
        }

        val asset = selectBestApkAsset(release.assets) ?: return null
        val file = File(context.externalCacheDir, asset.name)
        if (!file.exists()) return null
        // Truncated leftovers (interrupted download, older versions) are not installable
        if (asset.size > 0 && file.length() != asset.size) {
            file.delete()
            return null
        }
        return file
    }

    fun clearCache() {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        context.externalCacheDir?.listFiles()?.forEach {
            if (it.name.endsWith(".apk") || it.name.endsWith(".apk.part")) it.delete()
        }
    }
}
