package io.github.aedev.flow.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aedev.flow.R
import io.github.aedev.flow.ui.components.layout.topbar.FlowSearchTopBar
import io.github.aedev.flow.ui.components.library.LocalMediaRow
import io.github.aedev.flow.ui.components.shared.FlowEmptyState
import io.github.aedev.flow.ui.components.shared.MediaKind

private val ListContentPadding = PaddingValues(vertical = 12.dp)

/**
 * Dedicated search over one Local media tab. The tab is fixed by the route argument, so there is
 * no [io.github.aedev.flow.ui.components.shared.MediaKindSelector] here — the placeholder and the
 * prompt say which kind is being searched. Layout mirrors `LibrarySearchScreen`.
 */
@Composable
fun LocalMediaSearchScreen(
    onBack: () -> Unit,
    onVideoClick: (LocalMediaItem) -> Unit,
    onMusicClick: (items: List<LocalMediaItem>, index: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocalMediaSearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val results = state?.results
    val kind = viewModel.kind
    val kindLabel = stringResource(kind.labelRes)
    val background = MaterialTheme.colorScheme.background

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            FlowSearchTopBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onClose = onBack,
                placeholder = stringResource(R.string.local_media_search_placeholder, kindLabel),
            )
        },
    ) { padding ->
        val contentModifier = modifier.fillMaxSize().padding(padding).background(background)
        when {
            query.isBlank() -> {
                FlowEmptyState(
                    title = stringResource(R.string.local_media_search_prompt, kindLabel),
                    icon = Icons.Outlined.Search,
                    modifier = contentModifier,
                )
            }

            // The query is live but the MediaStore scan has not landed; stay blank rather than flash "no results".
            results == null -> {
                Unit
            }

            results.isEmpty() -> {
                FlowEmptyState(
                    title = stringResource(R.string.no_results_found),
                    icon = Icons.Outlined.Search,
                    modifier = contentModifier,
                )
            }

            else -> {
                LazyColumn(
                    modifier = contentModifier,
                    contentPadding = ListContentPadding,
                ) {
                    itemsIndexed(
                        items = results,
                        key = { _, item -> item.id },
                        contentType = { _, _ -> kind.name },
                    ) { index, item ->
                        LocalMediaRow(
                            item = item,
                            kind = kind,
                            onClick = {
                                if (kind == MediaKind.Videos) {
                                    onVideoClick(item)
                                } else {
                                    onMusicClick(results, index)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
