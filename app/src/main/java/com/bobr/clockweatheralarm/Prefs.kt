package com.bobr.clockweatheralarm

import android.content.Context

object Prefs {
    private const val FILE = "bobr_clock_weather"
    const val ALARMS = "alarms"
    const val NEXT_ALARM_ID = "next_alarm_id"
    // Legacy keys retained only for one-time migration.
    const val ALARM_ENABLED = "alarm_enabled"
    const val ALARM_HOUR = "alarm_hour"
    const val ALARM_MINUTE = "alarm_minute"
    const val LOCATION_NAME = "location_name"
    const val POSTCODE = "postcode"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val WEATHER_TEMP = "weather_temp"
    const val WEATHER_HOURLY = "weather_hourly"
    const val WEATHER_CODE = "weather_code"
    const val WEATHER_UPDATED = "weather_updated"
    const val WEATHER_INTERVAL_MINUTES = "weather_interval_minutes"

    fun values(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
