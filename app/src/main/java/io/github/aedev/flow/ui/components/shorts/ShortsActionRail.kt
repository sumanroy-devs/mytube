package io.github.aedev.flow.ui.components.shorts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.aedev.flow.R
import io.github.aedev.flow.ui.components.videoplayer.controls.PlayerPillIconButton
import io.github.aedev.flow.ui.theme.PlayerScrimContent

private val RailSpacing = 8.dp
private val RailButtonSize = 44.dp
private val RailIconSize = 24.dp
private val RailLabelSpacing = 2.dp

/** The column of actions down a reel's right edge. Labels are the caller's: a count, a name, or nothing. */
@Composable
internal fun ShortsActionRail(
    isLiked: Boolean,
    likeLabel: String,
    onLikeClick: () -> Unit,
    commentLabel: String,
    onCommentsClick: () -> Unit,
    isSaved: Boolean,
    saveLabel: String,
    onSaveClick: () -> Unit,
    shareLabel: String,
    onShareClick: () -> Unit,
    moreLabel: String,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(RailSpacing),
        modifier = modifier,
    ) {
        ShortsRailAction(
            icon = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
            label = likeLabel,
            contentDescription = stringResource(R.string.action_like),
            contentColor = if (isLiked) MaterialTheme.colorScheme.primary else PlayerScrimContent,
            onClick = onLikeClick,
        )
        ShortsRailAction(
            icon = Icons.Outlined.ChatBubbleOutline,
            label = commentLabel,
            contentDescription = stringResource(R.string.action_comments),
            onClick = onCommentsClick,
        )
        ShortsRailAction(
            icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            label = saveLabel,
            contentDescription = stringResource(R.string.action_save),
            contentColor = if (isSaved) MaterialTheme.colorScheme.primary else PlayerScrimContent,
            onClick = onSaveClick,
        )
        ShortsRailAction(
            icon = Icons.AutoMirrored.Outlined.Send,
            label = shareLabel,
            contentDescription = stringResource(R.string.action_share),
            onClick = onShareClick,
        )
        ShortsRailAction(
            icon = Icons.Rounded.MoreVert,
            label = moreLabel,
            contentDescription = stringResource(R.string.cd_more_options),
            onClick = onMoreClick,
        )
    }
}

@Composable
private fun ShortsRailAction(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    onClick: () -> Unit,
    contentColor: Color = PlayerScrimContent,
) {
    // Fixed 44dp slot keeps every icon on one vertical line at the wall; a wider label
    // overflows sideways instead of widening the slot and pushing the icon inward.
    Layout(
        content = {
            PlayerPillIconButton(
                onClick = onClick,
                icon = icon,
                contentDescription = contentDescription,
                buttonSize = RailButtonSize,
                iconSize = RailIconSize,
                contentColor = contentColor,
                containerColor = Color.Transparent,
                haptic = HapticFeedbackType.TextHandleMove,
            )
            if (label.isNotBlank()) {
                Spacer(modifier = Modifier.height(RailLabelSpacing))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(shadow = ShortsOverlayDefaults.TextShadow),
                    color = PlayerScrimContent,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
    ) { measurables, constraints ->
        val button = measurables.first().measure(constraints)
        val under = measurables.drop(1).map { it.measure(constraints) }
        layout(button.width, button.height + under.sumOf { it.height }) {
            button.placeRelative(0, 0)
            var y = button.height
            under.forEach { part ->
                part.placeRelative((button.width - part.width) / 2, y)
                y += part.height
            }
        }
    }
}
