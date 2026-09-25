package io.github.aedev.flow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.aedev.flow.R
import io.github.aedev.flow.update.Release
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun UpdateDialog(
    release: Release,
    isDownloading: Boolean,
    progress: Float,
    readyToInstall: Boolean,
    currentVersion: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit,
    onIgnore: () -> Unit,
) {
    val downloadSize = release.assets.find { it.name.endsWith(".apk") }?.size ?: 0L
    val formattedDate = formatDate(release.publishedAt)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (readyToInstall) Icons.Filled.SystemUpdate else Icons.Filled.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text =
                        if (readyToInstall) {
                            stringResource(R.string.update_dialog_title_install)
                        } else {
                            stringResource(R.string.update_dialog_title_available)
                        },
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = release.tagName.removePrefix("v"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                if (!readyToInstall) {
                    // Show version info for update available state
                    InfoRow(label = stringResource(R.string.update_dialog_current_version), value = currentVersion)
                    InfoRow(label = stringResource(R.string.update_dialog_latest_version), value = release.tagName.removePrefix("v"))
                    InfoRow(label = stringResource(R.string.update_dialog_release_date), value = formattedDate)
                    InfoRow(
                        label = stringResource(R.string.update_dialog_size),
                        value =
                            if (downloadSize > 0) {
                                formatFileSize(downloadSize)
                            } else {
                                stringResource(R.string.update_dialog_unknown_size)
                            },
                    )
                }

                if (release.body.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.update_dialog_whats_new),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = release.body.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (isDownloading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = stringResource(R.string.update_dialog_downloading), style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = if (progress >= 0) "${progress.toInt()}%" else "—",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (progress >= 0) {
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    } else {
                        // Content length unknown → indeterminate bar instead of a frozen 0%
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(onClick = onAction) {
                    Text(
                        stringResource(
                            if (readyToInstall) R.string.update_dialog_install else R.string.update_dialog_download,
                        ),
                    )
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                Row {
                    if (!readyToInstall) {
                        TextButton(onClick = onIgnore) {
                            Text(stringResource(R.string.update_dialog_ignore))
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.update_dialog_cancel))
                    }
                }
            }
        },
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return ""
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun formatDate(dateString: String): String =
    try {
        // GitHub API returns ISO 8601 format: "2024-01-15T10:30:00Z"
        DateTimeFormatter
            .ofPattern("MMM dd, yyyy", Locale.US)
            .withZone(ZoneOffset.UTC)
            .format(Instant.parse(dateString))
    } catch (e: DateTimeParseException) {
        dateString
    }
