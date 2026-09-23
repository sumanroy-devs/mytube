package io.github.aedev.flow.ui.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aedev.flow.ui.components.shared.MediaKind
import io.github.aedev.flow.utils.PerformanceDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LocalMediaSearchViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val localMediaStore: LocalMediaStore,
    ) : ViewModel() {
        private val sharing = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L)

        /** Which tab of the Local media screen launched this search; fixed for the screen's lifetime. */
        val kind: MediaKind =
            if (savedStateHandle.get<String>(KIND_ARG) == MediaKind.Music.name) {
                MediaKind.Music
            } else {
                MediaKind.Videos
            }

        private val _query = MutableStateFlow("")

        val query: StateFlow<String> = _query

        // One MediaStore scan per search screen, lazily on first subscription and off the main thread.
        private val items: Flow<List<LocalMediaItem>> =
            flow {
                emit(
                    withContext(PerformanceDispatcher.diskIO) {
                        if (kind == MediaKind.Music) localMediaStore.queryMusic() else localMediaStore.queryVideos()
                    },
                )
            }

        internal val state: StateFlow<LocalMediaSearchState?> =
            _query
                .map { it.trim() }
                .distinctUntilChanged()
                .combine(items) { trimmed, scanned ->
                    LocalMediaSearchState(
                        query = trimmed,
                        results =
                            if (trimmed.isEmpty()) {
                                null
                            } else {
                                buildLocalMediaSearchResults(trimmed, scanned)
                            },
                    )
                }.flowOn(Dispatchers.Default)
                .stateIn(viewModelScope, sharing, null)

        fun onQueryChange(value: String) {
            _query.value = value
        }

        companion object {
            const val KIND_ARG = "kind"
        }
    }
