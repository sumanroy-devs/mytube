package io.github.aedev.flow.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aedev.flow.data.local.LikedVideosRepository
import io.github.aedev.flow.data.local.PlaylistRepository
import io.github.aedev.flow.data.local.ViewHistory
import io.github.aedev.flow.data.video.VideoDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import io.github.aedev.flow.data.music.DownloadManager as MusicDownloadManager

@HiltViewModel
class LibrarySearchViewModel
    @Inject
    constructor(
        playlistRepository: PlaylistRepository,
        likedVideosRepository: LikedVideosRepository,
        viewHistory: ViewHistory,
        videoDownloadManager: VideoDownloadManager,
        musicDownloadManager: MusicDownloadManager,
    ) : ViewModel() {
        private val sharing = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L)

        private val _query = MutableStateFlow("")

        val query: StateFlow<String> = _query

        private val sources: Flow<LibrarySearchSources> =
            combine(
                playlistRepository.getAllPlaylistsFlow(),
                playlistRepository.getMusicPlaylistsFlow(),
                playlistRepository.getVideoOnlyWatchLaterFlow(),
                playlistRepository.getVideoOnlySavedShortsFlow(),
                likedVideosRepository.getAllLikedVideos(),
            ) { videoPlaylists, musicPlaylists, watchLater, savedShorts, likes ->
                LibrarySearchSources(
                    videoPlaylists = videoPlaylists,
                    musicPlaylists = musicPlaylists,
                    watchLater = watchLater,
                    savedShorts = savedShorts,
                    likes = likes,
                )
            }.combine(viewHistory.getAllHistory()) { sourceData, history ->
                sourceData.copy(history = history)
            }.combine(videoDownloadManager.downloadedVideos) { sourceData, downloads ->
                sourceData.copy(downloadedVideos = downloads)
            }.combine(musicDownloadManager.downloadedTracks) { sourceData, tracks ->
                sourceData.copy(downloadedTracks = tracks)
            }

        internal val state: StateFlow<LibrarySearchState?> =
            _query
                .map { it.trim() }
                .distinctUntilChanged()
                .combine(sources) { trimmed, sourceData ->
                    LibrarySearchState(
                        query = trimmed,
                        sections =
                            if (trimmed.isEmpty()) {
                                null
                            } else {
                                buildLibrarySearchSections(trimmed, sourceData)
                            },
                    )
                }.flowOn(Dispatchers.Default)
                .stateIn(viewModelScope, sharing, null)

        fun onQueryChange(value: String) {
            _query.value = value
        }
    }
