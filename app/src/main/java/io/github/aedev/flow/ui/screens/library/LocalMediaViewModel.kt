package io.github.aedev.flow.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aedev.flow.utils.PerformanceDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LocalMediaItem(
    val id: Long,
    val contentUri: String,
    val title: String,
    val subtitle: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val isVideo: Boolean,
    val artworkUri: String? = null,
)

data class LocalMediaUiState(
    val videos: List<LocalMediaItem> = emptyList(),
    val music: List<LocalMediaItem> = emptyList(),
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val permissionDenied: Boolean = false,
)

@HiltViewModel
class LocalMediaViewModel
    @Inject
    constructor(
        private val localMediaStore: LocalMediaStore,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LocalMediaUiState())
        val uiState: StateFlow<LocalMediaUiState> = _uiState.asStateFlow()

        fun scan() {
            if (_uiState.value.isScanning) return
            _uiState.update { it.copy(isScanning = true, permissionDenied = false) }
            viewModelScope.launch {
                val videos = withContext(PerformanceDispatcher.diskIO) { localMediaStore.queryVideos() }
                val music = withContext(PerformanceDispatcher.diskIO) { localMediaStore.queryMusic() }
                _uiState.update {
                    it.copy(videos = videos, music = music, isScanning = false, hasScanned = true)
                }
            }
        }

        fun onPermissionDenied() {
            _uiState.update { it.copy(permissionDenied = true, isScanning = false, hasScanned = true) }
        }

        companion object {
            fun localMediaId(item: LocalMediaItem): String = "local_${item.id}"
        }
    }
