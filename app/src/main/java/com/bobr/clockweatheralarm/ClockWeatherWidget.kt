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
                setTextViewText(R.id.weather_condition, weatherDescription(weatherCode))
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
