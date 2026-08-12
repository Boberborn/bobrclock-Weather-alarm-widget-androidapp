package com.bobr.clockweatheralarm

import android.content.Context
import org.json.JSONObject
import kotlin.math.roundToInt

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
    const val TEMP_UNIT = "temp_unit"
    const val WIDGET_CONFIG = "widget_config"
    const val TIME_FORMAT = "time_format"
    const val ALARM_IGNORE_SILENT = "alarm_ignore_silent"

    const val WIDGET_CELL_WIDTH_DP = 63f
    const val WIDGET_CELL_HEIGHT_DP = 34f

    const val WIDGET_REAL_CELL_WIDTH_DP = 63f
    const val WIDGET_REAL_CELL_HEIGHT_DP = 113f

    const val PREVIEW_ROWS = 2
    const val PREVIEW_COLS = 5
    const val PREVIEW_KEY = "2x5"
    const val PREVIEW_COLS_PREF = "preview_cols"

    fun previewCols(context: Context): Int =
        values(context).getInt(PREVIEW_COLS_PREF, PREVIEW_COLS)

    fun alarmIgnoresSilentMode(context: Context): Boolean =
        values(context).getBoolean(ALARM_IGNORE_SILENT, true)

    fun savePreviewCols(context: Context, cols: Int) {
        values(context).edit().putInt(PREVIEW_COLS_PREF, cols).apply()
    }

    data class WidgetConfig(
        val clockSp: Float = 32f,
        val clockColor: Int = 0xFFFFFFFF.toInt(),
        val dateSp: Float = 11f,
        val swapTimeDate: Boolean = false,
        val tempSp: Float = 28f,
        val tempColor: Int = 0xFFFFFFFF.toInt(),
        val condSp: Float = 9f,
        val condColor: Int = 0xFFFFFFFF.toInt(),
        val locSp: Float = 8f,
        val locColor: Int = 0xFFFFFFFF.toInt(),
        val uvSp: Float = 10f,
        val uvColor: Int = 0xFFFFFFFF.toInt(),
        val iconDp: Float = 60f,
        val showIcon: Boolean = true,
        val hourTimeSp: Float = 18f,
        val hourTempSp: Float = 18f,
        val hourIconDp: Float = 120f,
        val alarmSp: Float = 9f,
        val alarmColor: Int = 0xFFFFFFFF.toInt(),
        val showLocation: Boolean = true,
        val showCondition: Boolean = true,
        val showUv: Boolean = true,
        val showHourly: Boolean = true,
        val showAlarms: Boolean = true,
    ) {
        fun toJson(): String = JSONObject()
            .put("clockSp", clockSp.toDouble())
            .put("clockColor", clockColor.toLong())
            .put("dateSp", dateSp.toDouble())
            .put("swapTimeDate", swapTimeDate)
            .put("tempSp", tempSp.toDouble())
            .put("tempColor", tempColor.toLong())
            .put("condSp", condSp.toDouble())
            .put("condColor", condColor.toLong())
            .put("locSp", locSp.toDouble())
            .put("locColor", locColor.toLong())
            .put("uvSp", uvSp.toDouble())
            .put("uvColor", uvColor.toLong())
            .put("iconDp", iconDp.toDouble())
            .put("showIcon", showIcon)
            .put("hourTimeSp", hourTimeSp.toDouble())
            .put("hourTempSp", hourTempSp.toDouble())
            .put("hourIconDp", hourIconDp.toDouble())
            .put("alarmSp", alarmSp.toDouble())
            .put("alarmColor", alarmColor.toLong())
            .put("showLocation", showLocation)
            .put("showCondition", showCondition)
            .put("showUv", showUv)
            .put("showHourly", showHourly)
            .put("showAlarms", showAlarms)
            .toString()

        companion object {
            fun fromJson(raw: String): WidgetConfig = try {
                val o = JSONObject(raw)
                WidgetConfig(
                    clockSp = o.optDouble("clockSp", 32.0).toFloat(),
                    clockColor = o.optLong("clockColor", 0xFFFFFFFFL).toInt(),
                    dateSp = o.optDouble("dateSp", 11.0).toFloat(),
                    swapTimeDate = o.optBoolean("swapTimeDate", false),
                    tempSp = o.optDouble("tempSp", 28.0).toFloat(),
                    tempColor = o.optLong("tempColor", 0xFFFFFFFFL).toInt(),
                    condSp = o.optDouble("condSp", 9.0).toFloat(),
                    condColor = o.optLong("condColor", 0xFFFFFFFFL).toInt(),
                    locSp = o.optDouble("locSp", 8.0).toFloat(),
                    locColor = o.optLong("locColor", 0xFFFFFFFFL).toInt(),
                    uvSp = o.optDouble("uvSp", 10.0).toFloat(),
                    uvColor = o.optLong("uvColor", 0xFFFFFFFFL).toInt(),
                    iconDp = o.optDouble("iconDp", 40.0).toFloat(),
                    showIcon = o.optBoolean("showIcon", true),
                    hourTimeSp = o.optDouble("hourTimeSp", 9.0).toFloat(),
                    hourTempSp = o.optDouble("hourTempSp", 9.0).toFloat(),
                    hourIconDp = o.optDouble("hourIconDp", 60.0).toFloat(),
                    alarmSp = o.optDouble("alarmSp", 9.0).toFloat(),
                    alarmColor = o.optLong("alarmColor", 0xFFFFFFFFL).toInt(),
                    showLocation = o.optBoolean("showLocation", true),
                    showCondition = o.optBoolean("showCondition", true),
                    showUv = o.optBoolean("showUv", true),
                    showHourly = o.optBoolean("showHourly", true),
                    showAlarms = o.optBoolean("showAlarms", true),
                )
            } catch (_: Exception) {
                WidgetConfig()
            }
        }
    }

    fun cellKey(rows: Int, cols: Int): String = "${rows}x$cols"

    fun parseCellKey(key: String): Pair<Int, Int> {
        val parts = key.split("x")
        val rows = parts.getOrNull(0)?.toIntOrNull() ?: 2
        val cols = parts.getOrNull(1)?.toIntOrNull() ?: 5
        return rows.coerceAtLeast(1) to cols.coerceAtLeast(1)
    }

    fun cellKeyFromSize(minWidth: Int, minHeight: Int): String {
        val cols = (minWidth / WIDGET_CELL_WIDTH_DP).roundToInt().coerceAtLeast(1)
        val rows = (minHeight / WIDGET_CELL_HEIGHT_DP).roundToInt().coerceAtLeast(1)
        return cellKey(rows, cols)
    }

    fun defaultConfig(rows: Int, cols: Int): WidgetConfig {
        val narrow = cols < 5
        return when {
            rows < 2 -> WidgetConfig(
                clockSp = 23f, tempSp = 20f, condSp = 9f, locSp = 8f, uvSp = 10f,
                iconDp = 53f, showCondition = true, showUv = true, showLocation = false, showHourly = false,
            )
            cols == 2 -> WidgetConfig(
                clockSp = 23f, tempSp = 20f, condSp = 9f, locSp = 8f, uvSp = 10f,
                iconDp = 53f, showCondition = true, showUv = true, showLocation = false, showHourly = true,
            )
            cols == 3 -> WidgetConfig(
                clockSp = 36f, dateSp = 14f, tempSp = 30f, condSp = 10f, locSp = 8f, uvSp = 10f,
                iconDp = 125f, showCondition = true, showUv = true, showLocation = true, showHourly = true,
            )
            cols == 4 -> WidgetConfig(
                clockSp = 36f, dateSp = 14f, tempSp = 30f, condSp = 10f, locSp = 8f, uvSp = 10f,
                iconDp = 152f, showCondition = true, showUv = true, showLocation = true, showHourly = true,
            )
            else -> WidgetConfig(
                clockSp = 36f, dateSp = 14f, tempSp = 30f, condSp = 10f, locSp = 8f, uvSp = 10f,
                iconDp = 166f, showUv = true, showLocation = true, showHourly = true,
            )
        }
    }

    fun widgetConfig(context: Context, key: String): WidgetConfig {
        val raw = values(context).getString("$WIDGET_CONFIG/$key", null)
        return if (raw == null) {
            val (rows, cols) = parseCellKey(key)
            defaultConfig(rows, cols)
        } else {
            WidgetConfig.fromJson(raw)
        }
    }

    fun saveWidgetConfig(context: Context, key: String, config: WidgetConfig) {
        values(context).edit()
            .putString("$WIDGET_CONFIG/$key", config.toJson())
            .apply()
    }

    fun resetWidgetConfig(context: Context, key: String) {
        values(context).edit().remove("$WIDGET_CONFIG/$key").apply()
    }

    fun clearWidgetConfigs(context: Context, keepKey: String? = null) {
        val editor = values(context).edit()
        val all = values(context).all.keys.filter {
            it.startsWith("$WIDGET_CONFIG/") && it != "$WIDGET_CONFIG/$keepKey"
        }
        for (k in all) editor.remove(k)
        editor.apply()
    }

    fun values(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun windUnit(context: Context): String {
        val stored = values(context).getString(WIND_UNIT, null)
        if (stored != null) return stored
        val cc = values(context).getString(COUNTRY_CODE, null)?.uppercase()
        return if (cc in setOf("US", "GB", "LR", "MM")) "mph" else "kmh"
    }

    fun windUnitLabel(context: Context): String =
        if (windUnit(context) == "mph") "mph" else "km/h"

    fun tempIsFahrenheit(context: Context): Boolean {
        val stored = values(context).getString(TEMP_UNIT, null)
        if (stored != null) return stored == "F"
        val cc = values(context).getString(COUNTRY_CODE, null)?.uppercase()
        return cc in setOf("US", "GB", "LR", "MM")
    }

    fun tempLabel(context: Context): String =
        if (tempIsFahrenheit(context)) "°F" else "°C"

    fun displayTemp(context: Context, celsius: Int): String {
        val value = if (tempIsFahrenheit(context)) {
            (celsius * 9f / 5f + 32f).roundToInt()
        } else {
            celsius
        }
        return "$value${tempLabel(context)}"
    }

    fun timeFormat(context: Context): String =
        values(context).getString(TIME_FORMAT, "system") ?: "system"

    fun alarmIgnoresSilentMode(context: Context): Boolean =
        values(context).getBoolean(ALARM_IGNORE_SILENT, true)

    fun saveBackup(context: Context) {
        val o = JSONObject()
        for ((k, v) in values(context).all) {
            when (v) {
                is String -> o.put(k, v)
                is Int -> o.put(k, v)
                is Long -> o.put(k, v)
                is Float -> o.put(k, v.toDouble())
                is Double -> o.put(k, v)
                is Boolean -> o.put(k, v)
                is java.util.Set<*> -> {
                    val arr = org.json.JSONArray()
                    for (item in v) arr.put(item.toString())
                    o.put(k, arr)
                }
            }
        }
        context.openFileOutput(BACKUP_FILE, Context.MODE_PRIVATE).use { out ->
            out.write(o.toString().toByteArray(Charsets.UTF_8))
        }
    }

    fun restoreBackup(context: Context): Boolean = try {
        val raw = context.openFileInput(BACKUP_FILE).bufferedReader().use { it.readText() }
        val o = JSONObject(raw)
        val editor = values(context).edit()
        for (k in values(context).all.keys) editor.remove(k)
        val it = o.keys()
        while (it.hasNext()) {
            val k = it.next()
            val v = o.get(k)
            when (v) {
                is JSONObject -> editor.putString(k, v.toString())
                is Int -> editor.putInt(k, v)
                is Long -> editor.putLong(k, v)
                is Double -> if (v == Math.floor(v) && v < Long.MAX_VALUE) {
                    editor.putLong(k, v.toLong())
                } else {
                    editor.putString(k, v.toString())
                }
                is Boolean -> editor.putBoolean(k, v)
                is String -> editor.putString(k, v)
                else -> editor.putString(k, v.toString())
            }
        }
        editor.apply()
        true
    } catch (_: Exception) {
        false
    }

    fun clearAll(context: Context) {
        values(context).edit().clear().apply()
    }

    private const val BACKUP_FILE = "bobr_backup.json"
}
