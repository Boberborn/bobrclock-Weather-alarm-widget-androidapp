package com.bobr.clockweatheralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isTest = intent.getBooleanExtra(AlarmScheduler.EXTRA_TEST, false)
        val alarmId = intent.getIntExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val alarm = if (isTest) null else AlarmStore.find(context, alarmId)
        if (!isTest && (alarm == null || !alarm.enabled)) {
            AlarmLog.log(
                context,
                "alarm #$alarmId triggered but skipped (not found / disabled)",
            )
            return
        }
        AlarmLog.log(context, if (isTest) "test alarm fired" else "alarm #$alarmId fired")

        if (!isTest && alarm != null && alarm.daysMask == 0) {
            AlarmStore.save(context, alarm.copy(enabled = false))
            AlarmLog.log(context, "one-time alarm #$alarmId completed, now disabled")
        }

        val service = Intent(context, AlarmService::class.java)
            .setAction(AlarmService.ACTION_START)
            .putExtra(
                AlarmScheduler.EXTRA_SOUND_URI,
                if (isTest) {
                    intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI)
                } else {
                    alarm?.soundUri
                },
            )
            .putExtra(
                AlarmScheduler.EXTRA_SOUND_NAME,
                if (isTest) {
                    intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_NAME)
                } else {
                    alarm?.soundName
                },
            )
        context.startForegroundService(service)

        if (!isTest) {
            AlarmScheduler.scheduleById(context, alarmId)
        }
    }
}
