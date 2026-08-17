package com.bobr.clockweatheralarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {
    private const val LEGACY_REQUEST_ALARM = 4101
    private const val REQUEST_ALARM_BASE = 10_000
    private const val REQUEST_SHOW_BASE = 20_000
    private const val REQUEST_TEST = 30_001
    private const val REQUEST_SNOOZE = 30_002
    private const val REQUEST_TICK = 40_000

    fun scheduleAll(context: Context): Boolean {
        cancelLegacy(context)
        val manager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            AlarmLog.log(context, "reschedule skipped: exact alarm permission missing")
            return false
        }
        val enabled = AlarmStore.load(context).filter { it.enabled && it.daysMask != 0 }
        AlarmLog.log(context, "reschedule: ${enabled.size} enabled alarm(s)")
        val result = enabled.all { schedule(context, it) }
        ClockWeatherWidget.updateAll(context)
        scheduleNextTick(context)
        return result
    }

    fun ensureScheduled(context: Context): Boolean {
        val enabled = AlarmStore.load(context).filter { it.enabled && it.daysMask != 0 }
        if (enabled.isEmpty()) return true
        val missing = enabled.any { a ->
            PendingIntent.getBroadcast(
                context,
                REQUEST_ALARM_BASE + a.id,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) == null
        }
        return if (missing) scheduleAll(context) else true
    }

    fun scheduleById(context: Context, id: Int): Boolean {
        val alarm = AlarmStore.find(context, id) ?: return false
        if (!alarm.enabled || alarm.daysMask == 0) return false
        return schedule(context, alarm)
    }

    fun schedule(context: Context, alarm: SavedAlarm): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            return false
        }

        val alarmIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_ALARM_BASE + alarm.id,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_ALARM_ID, alarm.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showIntent = PendingIntent.getActivity(
            context,
            REQUEST_SHOW_BASE + alarm.id,
            Intent(context, InstructionActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val next = AlarmStore.nextTrigger(alarm)
        return try {
            manager.setAlarmClock(
                AlarmManager.AlarmClockInfo(next, showIntent),
                alarmIntent,
            )
            AlarmLog.log(
                context,
                "scheduled alarm #${alarm.id} for $next",
            )
            ClockWeatherWidget.updateAll(context)
            scheduleNextTick(context)
            true
        } catch (_: SecurityException) {
            AlarmLog.log(context, "schedule alarm #${alarm.id} FAILED (SecurityException)")
            false
        }
    }

    fun scheduleTest(context: Context, soundUri: String?, soundName: String): Boolean =
        scheduleTemporary(context, REQUEST_TEST, 10_000L, soundUri, soundName)

    fun scheduleSnooze(context: Context, soundUri: String?, soundName: String): Boolean =
        scheduleTemporary(
            context,
            REQUEST_SNOOZE,
            5L * 60L * 1000L,
            soundUri,
            soundName,
        )

    private fun scheduleTemporary(
        context: Context,
        requestCode: Int,
        delayMillis: Long,
        soundUri: String?,
        soundName: String,
    ): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms()) {
            return false
        }
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_TEST, true)
                .putExtra(EXTRA_SOUND_URI, soundUri)
                .putExtra(EXTRA_SOUND_NAME, soundName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return try {
            manager.setAlarmClock(
                AlarmManager.AlarmClockInfo(System.currentTimeMillis() + delayMillis, null),
                alarmIntent,
            )
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun cancel(context: Context, id: Int) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val alarmIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_ALARM_BASE + id,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmIntent?.let(manager::cancel)
        scheduleNextTick(context)
    }

    fun scheduleNextTick(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            REQUEST_TICK,
            Intent(context, AlarmTickReceiver::class.java).setAction(ACTION_ALARM_TICK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val next = AlarmStore.nextEnabled(context)
        if (next == null) {
            manager.cancel(pi)
            return
        }
        try {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC, next.second, pi)
        } catch (_: SecurityException) {
            AlarmLog.log(context, "scheduleNextTick failed (SecurityException)")
        }
    }

    private fun cancelLegacy(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val legacy = PendingIntent.getBroadcast(
            context,
            LEGACY_REQUEST_ALARM,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        legacy?.let(manager::cancel)
    }

    const val EXTRA_ALARM_ID = "alarm_id"
    const val EXTRA_TEST = "test_alarm"
    const val EXTRA_SOUND_URI = "sound_uri"
    const val EXTRA_SOUND_NAME = "sound_name"
    const val ACTION_ALARM_TICK = "com.bobr.clockweatheralarm.ALARM_TICK"
}
