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
    const val WEATHER_HOURLY_ALL = "weather_hourly_all"
    const val WEATHER_UV = "weather_uv"
    const val WEATHER_CODE = "weather_code"
    const val WEATHER_WIND = "weather_wind_mph"
    const val WEATHER_WIND_DIR = "weather_wind_deg"
    const val WEATHER_DAILY = "weather_daily"
    const val WEATHER_UPDATED = "weather_updated"
    const val WEATHER_INTERVAL_MINUTES = "weather_interval_minutes"
    const val WIDGET_SHOW_ALARMS = "widget_show_alarms"
    const val WEATHER_FEELS_LIKE = "weather_feels_like"
    const val WEATHER_HUMIDITY = "weather_humidity"
    const val WEATHER_PRESSURE = "weather_pressure"
    const val WEATHER_CLOUD_COVER = "weather_cloud_cover"
    const val WEATHER_WIND_GUST = "weather_wind_gust"
    const val WEATHER_PRECIPITATION = "weather_precipitation"
    const val WEATHER_VISIBILITY = "weather_visibility"
    const val WEATHER_DEW_POINT = "weather_dew_point"
    const val COUNTRY_CODE = "country_code"
    const val WIND_UNIT = "wind_unit"

    fun values(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun windUnit(context: Context): String {
        val stored = values(context).getString(WIND_UNIT, null)
        if (stored != null) return stored
        val cc = values(context).getString(COUNTRY_CODE, null)?.uppercase()
        return if (cc in setOf("US", "GB", "LR", "MM")) "mph" else "kmh"
    }

    fun windUnitLabel(context: Context): String =
        if (windUnit(context) == "mph") "mph" else "km/h"
}
