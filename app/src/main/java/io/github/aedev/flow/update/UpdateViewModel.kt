package io.github.aedev.flow.update

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.aedev.flow.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val updateManager = UpdateManager(application)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress: StateFlow<Float> = _downloadProgress.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val prefs = application.getSharedPreferences(UpdateManager.PREFS_NAME, Context.MODE_PRIVATE)
    private val _isAutoUpdateEnabled =
        MutableStateFlow(
            if (BuildConfig.ENABLE_UPDATE_FEATURE) prefs.getBoolean(UpdateManager.KEY_AUTO_UPDATE, false) else false,
        )
    val isAutoUpdateEnabled: StateFlow<Boolean> = _isAutoUpdateEnabled.asStateFlow()

    fun toggleAutoUpdate(enabled: Boolean) {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        prefs.edit().putBoolean(UpdateManager.KEY_AUTO_UPDATE, enabled).apply()
        _isAutoUpdateEnabled.value = enabled
        if (enabled) {
            checkForUpdate(manual = false)
        }
    }

    init {
        // Only initialize auto-update if feature is enabled
        if (BuildConfig.ENABLE_UPDATE_FEATURE && isAutoUpdateEnabled.value) {
            checkForUpdate(manual = false)
        }
    }

    sealed class UpdateState {
        object Idle : UpdateState()

        object Loading : UpdateState()

        data class Available(
            val release: Release,
        ) : UpdateState()

        object NoUpdate : UpdateState()

        object Error : UpdateState()

        data class ReadyToInstall(
            val release: Release,
        ) : UpdateState()
    }

    fun dismissNoUpdate() {
        _updateState.value = UpdateState.Idle
    }

    fun checkForUpdate(manual: Boolean = false) {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val release = updateManager.checkForUpdate(forceShow = manual)
                if (release != null) {
                    val existingFile = updateManager.getApkFile(release)
                    if (existingFile != null) {
                        _updateState.value = UpdateState.ReadyToInstall(release)
                    } else {
                        _updateState.value = UpdateState.Available(release)
                    }
                } else {
                    if (manual) {
                        _updateState.value = UpdateState.NoUpdate
                    } else {
                        _updateState.value = UpdateState.Idle
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (manual) {
                    _updateState.value = UpdateState.Error
                } else {
                    _updateState.value = UpdateState.Idle
                }
            }
        }
    }

    fun downloadUpdate(release: Release) {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        // No APK asset matches this device → fall back to the release page in the browser
        if (!updateManager.hasCompatibleApk(release)) {
            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(updateManager.getReleasePageUrl(release)),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            getApplication<Application>().startActivity(intent)
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true
            try {
                updateManager.downloadUpdate(release).collect { progress ->
                    _downloadProgress.value = progress
                }
                _isDownloading.value = false
                _updateState.value = UpdateState.ReadyToInstall(release)
            } catch (e: Exception) {
                e.printStackTrace()
                _isDownloading.value = false
                _updateState.value = UpdateState.Error
            }
        }
    }

    fun installUpdate(release: Release) {
        // No-op if update feature is disabled
        if (!BuildConfig.ENABLE_UPDATE_FEATURE) {
            return
        }

        val file = updateManager.getApkFile(release) ?: return
        val context = getApplication<Application>()
        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file,
            )
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
    }

    fun dismiss() {
        // Clean up downloaded APK when user dismisses the dialog
        updateManager.clearCache()
        _updateState.value = UpdateState.Idle
    }

    fun ignoreVersion(version: String) {
        updateManager.ignoreVersion(version)
        _updateState.value = UpdateState.Idle
    }
}
