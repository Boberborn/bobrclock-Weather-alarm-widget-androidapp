package com.bobr.clockweatheralarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ClockWeatherWidget.updateAll(context)
        AlarmScheduler.scheduleNextTick(context)
    }
}
