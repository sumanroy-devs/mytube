package io.github.aedev.flow.update

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import io.github.aedev.flow.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Checks for application updates in the background every 12 hours and posts an
 * update notification (ported from the video project's UpdateCheckWorker).
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    companion object {
        // Kept from MyTube's previous updater so existing scheduled work carries over.
        const val WORK_NAME = "update_check_work"
        private const val TAG = "UpdateCheckWorker"
        private const val COOLDOWN_HOURS = 12L

        fun schedulePeriodicCheck(
            context: Context,
            reschedule: Boolean = false,
        ) {
            // No-op when self-update can't apply to this build/package
            if (!UpdateManager.isUpdateActive) {
                return
            }

            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<UpdateCheckWorker>(12, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        WorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS,
                    ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                if (reschedule) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                workRequest,
            )
            Log.d(TAG, "Scheduled update check every 12 hours")
        }

        fun cancelScheduledChecks(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled scheduled update checks")
        }
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            if (!UpdateManager.isUpdateActive) {
                return@withContext Result.success()
            }

            // Never run background checks from debug builds
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Skipping background update check in DEBUG mode")
                return@withContext Result.success()
            }

            try {
                val prefs = applicationContext.getSharedPreferences(UpdateManager.PREFS_NAME, Context.MODE_PRIVATE)
                val lastCheck = prefs.getLong(UpdateManager.KEY_LAST_CHECK, 0L)
                val currentTime = System.currentTimeMillis()

                if (currentTime - lastCheck < TimeUnit.HOURS.toMillis(COOLDOWN_HOURS)) {
                    Log.d(TAG, "Skipping check due to cooldown")
                    return@withContext Result.success()
                }

                Log.d(TAG, "Checking for updates...")
                // UpdateManager already handles package gating, version compare and the ignore list
                val release = UpdateManager(applicationContext).checkForUpdate(forceShow = false)

                if (release != null) {
                    Log.d(TAG, "New version found: ${release.tagName}")
                    UpdateNotification.show(applicationContext, release)
                } else {
                    Log.d(TAG, "No new updates found")
                }

                prefs.edit().putLong(UpdateManager.KEY_LAST_CHECK, currentTime).apply()
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check for updates", e)
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        }
}
