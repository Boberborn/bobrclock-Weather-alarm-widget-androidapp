package com.bobr.clockweatheralarm

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

object WeatherScheduler {
    const val SCHEDULED_JOB_ID = 5101
    private const val IMMEDIATE_JOB_ID = 5102
    const val DEFAULT_INTERVAL_MINUTES = 60
    const val MIN_INTERVAL_MINUTES = 10
    const val MAX_INTERVAL_MINUTES = 12 * 60

    fun intervalMinutes(context: Context): Int =
        Prefs.values(context)
            .getInt(Prefs.WEATHER_INTERVAL_MINUTES, DEFAULT_INTERVAL_MINUTES)
            .coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)

    fun ensureScheduled(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        if (scheduler.getPendingJob(SCHEDULED_JOB_ID) == null) {
            scheduleNext(context)
        }
    }

    fun scheduleNext(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        val job = JobInfo.Builder(
            SCHEDULED_JOB_ID,
            ComponentName(context, WeatherJobService::class.java),
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setMinimumLatency(intervalMinutes(context) * 60_000L)
            .setBackoffCriteria(30_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
            .build()
        scheduler.schedule(job)
    }

    fun updateInterval(context: Context, minutes: Int) {
        val stepped = (minutes / 10 * 10).coerceIn(
            MIN_INTERVAL_MINUTES,
            MAX_INTERVAL_MINUTES,
        )
        Prefs.values(context).edit()
            .putInt(Prefs.WEATHER_INTERVAL_MINUTES, stepped)
            .apply()
        context.getSystemService(JobScheduler::class.java).cancel(SCHEDULED_JOB_ID)
        scheduleNext(context)
    }

    fun refreshNow(context: Context) {
        val scheduler = context.getSystemService(JobScheduler::class.java)
        val job = JobInfo.Builder(
            IMMEDIATE_JOB_ID,
            ComponentName(context, WeatherJobService::class.java),
        )
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setOverrideDeadline(0)
            .build()
        scheduler.schedule(job)
    }
}
