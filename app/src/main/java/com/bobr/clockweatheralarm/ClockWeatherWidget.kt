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
        appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, render(context)) }
        WeatherScheduler.ensureScheduled(context)
        WeatherScheduler.refreshNow(context)
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
                manager.updateAppWidget(it, render(context))
            }
        }

        private fun render(context: Context): RemoteViews {
            val prefs = Prefs.values(context)
            val location = prefs.getString(Prefs.LOCATION_NAME, null)
                ?: context.getString(R.string.set_location)
            val temperature = prefs.getString(Prefs.WEATHER_TEMP, null)
            val weatherCode = prefs.getInt(Prefs.WEATHER_CODE, -1)
            val updatedAt = prefs.getLong(Prefs.WEATHER_UPDATED, 0L)
            val nextAlarm = AlarmStore.nextEnabled(context)?.first

            return RemoteViews(context.packageName, R.layout.widget_clock_weather).apply {
                setTextViewText(R.id.weather_location, location)
                setTextViewText(
                    R.id.weather_temperature,
                    temperature?.let { "$it°" } ?: context.getString(R.string.weather_waiting),
                )
                setTextViewText(R.id.weather_condition, weatherDescription(weatherCode))
                setTextViewText(
                    R.id.weather_updated,
                    if (updatedAt == 0L) {
                        context.getString(R.string.tap_refresh)
                    } else {
                        DateUtils.getRelativeTimeSpanString(
                            updatedAt,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                        ).toString()
                    },
                )
                setTextViewText(
                    R.id.alarm_status,
                    if (nextAlarm != null) {
                        context.getString(
                            R.string.alarm_widget_time,
                            nextAlarm.hour,
                            nextAlarm.minute,
                        )
                    } else {
                        context.getString(R.string.alarm_widget_off)
                    },
                )

                val open = PendingIntent.getActivity(
                    context,
                    6101,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                val refresh = PendingIntent.getBroadcast(
                    context,
                    6102,
                    Intent(context, ClockWeatherWidget::class.java).setAction(ACTION_REFRESH),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, open)
                setOnClickPendingIntent(R.id.refresh_button, refresh)

                renderHourly(this, context, prefs)
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

        private fun weatherDescription(code: Int): String = when (code) {
            0 -> "Clear"
            1, 2 -> "Partly cloudy"
            3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
            71, 73, 75, 77, 85, 86 -> "Snow"
            95, 96, 99 -> "Thunderstorm"
            else -> "Weather"
        }
    }
}
