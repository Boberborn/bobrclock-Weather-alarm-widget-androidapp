package com.bobr.clockweatheralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SNOOZE) {
            AlarmScheduler.scheduleSnooze(
                context,
                intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI),
                intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_NAME) ?: "Default alarm",
            )
        }
        context.startService(
            Intent(context, AlarmService::class.java).setAction(AlarmService.ACTION_STOP),
        )
    }

    companion object {
        const val ACTION_STOP = "com.bobr.clockweatheralarm.NOTIFICATION_STOP"
        const val ACTION_SNOOZE = "com.bobr.clockweatheralarm.NOTIFICATION_SNOOZE"
    }
}
