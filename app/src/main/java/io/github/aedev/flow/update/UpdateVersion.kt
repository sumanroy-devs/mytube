package io.github.aedev.flow.update

/**
 * Version comparison for the self-update check. Suffix stripping keeps
 * "1.3.0-playstore", "1.3.0-fdroid", "1.3.0-preview.1" and "v1.3.0" all
 * comparing as plain "1.3.0".
 */
internal object UpdateVersion {
    fun normalize(version: String): String = version.trim().removePrefix("v").substringBefore('-')

    fun isNewer(
        remote: String,
        current: String,
    ): Boolean {
        val remoteParts = normalize(remote).split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = normalize(current).split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(remoteParts.size, currentParts.size)) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
