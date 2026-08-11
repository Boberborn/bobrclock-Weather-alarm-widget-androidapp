package com.bobr.clockweatheralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "android.intent.action.BOOT_COMPLETED" -> AlarmLog.log(context, "boot completed")
            "android.intent.action.MY_PACKAGE_REPLACED" -> AlarmLog.log(context, "app updated")
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED" ->
                AlarmLog.log(context, "exact alarm permission changed")
        }
        AlarmScheduler.scheduleAll(context)
        WeatherScheduler.ensureScheduled(context)
        ClockWeatherWidget.updateAll(context)
    }
}
