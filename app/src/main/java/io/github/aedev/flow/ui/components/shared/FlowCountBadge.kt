package io.github.aedev.flow.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aedev.flow.R

/**
 * The numeric bubble over an icon action: a fixed 16dp circle so the count never resizes the
 * control beneath it, capped at [R.string.notification_badge_9_plus] past nine. Positioning is
 * the caller's — pass a [Modifier] (typically top-end with an offset) to hang it over the
 * button's edge. Hidden at zero so callers can pass a raw count unconditionally.
 */
@Composable
fun FlowCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    if (count <= 0) return
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .size(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text =
                if (count > 9) {
                    stringResource(R.string.notification_badge_9_plus)
                } else {
                    count.toString()
                },
            color = MaterialTheme.colorScheme.onPrimary,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 9.sp,
                ),
        )
    }
}
