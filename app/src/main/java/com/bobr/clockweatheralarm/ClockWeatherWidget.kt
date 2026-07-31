package com.bobr.clockweatheralarm

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.widget.RemoteViews
import java.util.Locale

class ClockWeatherWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach {
            appWidgetManager.updateAppWidget(
                it,
                render(context, options(appWidgetManager, it)),
            )
        }
        WeatherScheduler.ensureScheduled(context)
        WeatherScheduler.refreshNow(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        appWidgetManager.updateAppWidget(
            appWidgetId,
            render(context, options(appWidgetManager, appWidgetId)),
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            WeatherScheduler.refreshNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.bobr.clockweatheralarm.REFRESH_WEATHER"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWeatherWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                manager.updateAppWidget(
                    it,
                    render(context, options(manager, it)),
                )
            }
        }

        private data class WidgetOptions(val small: Boolean, val showHourly: Boolean)

        private fun options(manager: AppWidgetManager, widgetId: Int): WidgetOptions {
            val options = manager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            android.util.Log.d("WidgetSize", "id=$widgetId minWidth=$minWidth minHeight=$minHeight")
            return WidgetOptions(
                small = minWidth in 1..250,
                showHourly = minHeight >= 40,
            )
        }

        private fun render(context: Context, opts: WidgetOptions): RemoteViews {
            val small = opts.small
            val showHourly = opts.showHourly
            val prefs = Prefs.values(context)
            val location = prefs.getString(Prefs.LOCATION_NAME, null)
                ?: context.getString(R.string.set_location)
            val temperature = prefs.getString(Prefs.WEATHER_TEMP, null)
            val weatherCode = prefs.getInt(Prefs.WEATHER_CODE, -1)
            val alarms = AlarmStore.load(context).take(3)
            val layout = if (small) {
                R.layout.widget_clock_weather_small
            } else {
                R.layout.widget_clock_weather
            }

            return RemoteViews(context.packageName, layout).apply {
                setTextViewText(R.id.weather_location, location.substringBefore(",").trim())
                setTextViewText(
                    R.id.weather_temperature,
                    temperature?.let { "$it°" } ?: context.getString(R.string.weather_waiting),
                )
                setTextViewText(R.id.weather_condition, conditionText(weatherCode))
                val showAlarms = prefs.getBoolean(Prefs.WIDGET_SHOW_ALARMS, true) && !small
                val alarmVisibility = if (showAlarms) android.view.View.VISIBLE else android.view.View.GONE
                setViewVisibility(R.id.alarm_label, alarmVisibility)
                setViewVisibility(R.id.alarm_status, alarmVisibility)
                setTextViewText(R.id.alarm_label, context.getString(R.string.alarm_widget_time))
                setTextViewText(
                    R.id.alarm_status,
                    if (alarms.isEmpty()) {
                        context.getString(R.string.alarm_widget_off)
                    } else {
                        alarms.joinToString("\n") { String.format("%02d:%02d", it.hour, it.minute) }
                    },
                )

                val open = PendingIntent.getActivity(
                    context,
                    6101,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, open)

                if (showHourly) renderHourly(this, context, prefs)
            }
        }

        private fun renderHourly(rv: RemoteViews, context: Context, prefs: android.content.SharedPreferences) {
            val hourly = prefs.getString(Prefs.WEATHER_HOURLY, null) ?: return
            rv.removeAllViews(R.id.hourly_container)
            hourly.split("|").forEach { entry ->
                val parts = entry.split(";")
                if (parts.size == 2) {
                    val item = RemoteViews(context.packageName, R.layout.widget_hour_item)
                    item.setTextViewText(R.id.hour_time, parts[0])
                    item.setTextViewText(R.id.hour_temp, "${parts[1]}°")
                    rv.addView(R.id.hourly_container, item)
                }
            }
        }

        private fun conditionText(code: Int): String = when (code) {
            0 -> "Clear sky"
            1 -> "Mainly clear"
            2 -> "Partly cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51 -> "Light drizzle"
            53 -> "Drizzle"
            55 -> "Heavy drizzle"
            56, 57 -> "Freezing drizzle"
            61 -> "Light rain"
            63 -> "Rain"
            65 -> "Heavy rain"
            66, 67 -> "Freezing rain"
            71 -> "Light snow"
            73 -> "Snow"
            75 -> "Heavy snow"
            77 -> "Snow grains"
            80 -> "Light showers"
            81 -> "Showers"
            82 -> "Heavy showers"
            85 -> "Snow showers"
            86 -> "Heavy snow showers"
            95 -> "Thunderstorm"
            96 -> "Thunderstorm, hail"
            99 -> "Severe thunderstorm"
            else -> "Weather"
        }

        @Suppress("unused")
        private fun weatherIcon(code: Int): String = when (code) {
            0, 1 -> "Joyful"
            2 -> "Content"
            3 -> "Neutral"
            45, 48 -> "Dreamy"
            51, 53, 55 -> "Tearful"
            56, 57, 66 -> "Chill"
            61, 63, 80, 81 -> "Calm"
            65 -> "Pissed"
            67 -> "Furious"
            71, 73, 77, 85 -> "Frosty"
            75 -> "Icy rage"
            82 -> "Enraged"
            86 -> "Blizzard wrath"
            95 -> "Annoyed"
            96 -> "Mad"
            99 -> "Apocalyptic"
            else -> "Weather"
        }
    }
}
