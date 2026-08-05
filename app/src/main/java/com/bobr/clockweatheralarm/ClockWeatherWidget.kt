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
                render(context, options(context, appWidgetManager, it)),
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
            render(context, options(context, appWidgetManager, appWidgetId)),
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            WeatherScheduler.refreshNow(context)
        } else if (intent.action == ACTION_TAP) {
            val now = System.currentTimeMillis()
            if (now - lastTap < DOUBLE_TAP_MS) {
                lastTap = 0
                val open = Intent(context, InstructionActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(open)
            } else {
                lastTap = now
                WeatherScheduler.refreshNow(context)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.bobr.clockweatheralarm.REFRESH_WEATHER"
        const val ACTION_TAP = "com.bobr.clockweatheralarm.TAP_WIDGET"
        private const val DOUBLE_TAP_MS = 400L
        private const val CELL_HEIGHT_DP = 58
        private var lastTap = 0L

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClockWeatherWidget::class.java)
            manager.getAppWidgetIds(component).forEach {
                manager.updateAppWidget(
                    it,
                    render(context, options(context, manager, it)),
                )
            }
        }

        private data class WidgetOptions(
            val small: Boolean,
            val showHourly: Boolean,
            val minHeight: Int,
            val minWidth: Int,
        ) {
            val wide4: Boolean get() = minWidth >= 300
            val narrow: Boolean get() = minWidth < 250
        }

        private fun options(context: Context, manager: AppWidgetManager, widgetId: Int): WidgetOptions {
            val options = manager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
            android.util.Log.d("WidgetSize", "id=$widgetId $options")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val sizes = options.getParcelableArrayList<android.util.Size>(
                    AppWidgetManager.OPTION_APPWIDGET_SIZES,
                    android.util.Size::class.java,
                )
                val single = options.getParcelable<android.util.Size>(
                    AppWidgetManager.OPTION_APPWIDGET_SIZES,
                    android.util.Size::class.java,
                )
                val raw = options.get(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                android.util.Log.d("WidgetSize", "id=$widgetId sizesList=$sizes sizesSingle=$single rawClass=${raw?.javaClass}")
                val rawList = raw as? ArrayList<*>
                if (rawList != null) {
                    android.util.Log.d("WidgetSize", "id=$widgetId rawSizes=$rawList")
                    for (item in rawList) {
                        android.util.Log.d("WidgetSize", "id=$widgetId rawSizeItem=${item?.javaClass} $item")
                    }
                }
            }
            android.util.Log.d(
                "WidgetSize",
                "id=$widgetId minWidth=$minWidth minHeight=$minHeight " +
                    "density=${context.resources.displayMetrics.density} " +
                    "scaled=${context.resources.displayMetrics.scaledDensity}",
            )
            return WidgetOptions(
                small = minWidth in 1..250,
                // Five forecast columns need real horizontal room.  On narrow
                // widgets they wrapped the hour onto a second line.
                showHourly = minHeight >= 65 && minWidth >= 280,
                minHeight = minHeight,
                minWidth = minWidth,
            )
        }

        private fun render(context: Context, opts: WidgetOptions): RemoteViews {
            val small = opts.small
            val showHourly = opts.showHourly
            val wide4 = opts.wide4
            val prefs = Prefs.values(context)
            val location = prefs.getString(Prefs.LOCATION_NAME, null)
                ?: context.getString(R.string.set_location)
            val temperature = prefs.getString(Prefs.WEATHER_TEMP, null)
            val weatherCode = prefs.getInt(Prefs.WEATHER_CODE, -1)
            val alarms = AlarmStore.load(context).filter { it.enabled }.take(3)
            // A tall narrow widget needs the small vertical layout, but not
            // the five-column forecast.  The wide middle widget keeps it.
            // The short wide widget uses the same type, colours, city and UV
            // layout as the detailed middle widget; it only omits the forecast.
            val compact = false
            val layout = when {
                small -> R.layout.widget_clock_weather_small
                else -> R.layout.widget_clock_weather
            }

            return RemoteViews(context.packageName, layout).apply {
                setTextViewText(R.id.weather_location, location.substringBefore(",").trim())
                setTextViewText(
                    R.id.weather_temperature,
                    temperature?.let { "$it°" } ?: context.getString(R.string.weather_waiting),
                )
                setTextViewText(R.id.weather_condition, conditionText(weatherCode))
                setImageViewResource(R.id.weather_icon, weatherIconRes(weatherCode))
                if (compact) {
                    val isSmall = opts.minHeight < 35 || opts.narrow
                    setViewVisibility(R.id.clock_row, android.view.View.VISIBLE)
                    setViewVisibility(R.id.clock_col, android.view.View.GONE)
                    setViewVisibility(R.id.date_top, android.view.View.GONE)
                    setViewVisibility(R.id.weather_block, android.view.View.VISIBLE)
                    setViewVisibility(R.id.weather_location, android.view.View.GONE)
                    setViewVisibility(R.id.weather_condition, android.view.View.GONE)
                    setViewVisibility(R.id.weather_uv, android.view.View.GONE)
                    if (isSmall) {
                        setTextViewTextSize(R.id.weather_temperature, android.util.TypedValue.COMPLEX_UNIT_SP, 24f)
                        setTextViewTextSize(R.id.time_row, android.util.TypedValue.COMPLEX_UNIT_SP, 40f)
                        setTextViewTextSize(R.id.weather_condition, android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                        setTextViewTextSize(R.id.weather_location, android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                        setTextViewTextSize(R.id.weather_uv, android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                    } else {
                        setTextViewTextSize(R.id.weather_temperature, android.util.TypedValue.COMPLEX_UNIT_SP, 32f)
                        setTextViewTextSize(R.id.time_row, android.util.TypedValue.COMPLEX_UNIT_SP, 52f)
                        setTextViewTextSize(R.id.weather_condition, android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                        setTextViewTextSize(R.id.weather_location, android.util.TypedValue.COMPLEX_UNIT_SP, 11f)
                        setTextViewTextSize(R.id.weather_uv, android.util.TypedValue.COMPLEX_UNIT_SP, 10f)
                    }
                    if (alarms.isNotEmpty()) {
                        setViewVisibility(R.id.alarm_block, android.view.View.VISIBLE)
                        setViewVisibility(R.id.alarm_label, android.view.View.VISIBLE)
                        val alarmText = alarms.joinToString(", ") { String.format("%02d:%02d", it.hour, it.minute) }
                        setTextViewText(R.id.alarm_label, context.getString(R.string.alarm_widget_time))
                        setTextViewText(R.id.alarm_status, alarmText)
                    } else {
                        setViewVisibility(R.id.alarm_block, android.view.View.GONE)
                    }
                    if (opts.minHeight >= 40 && !opts.narrow) {
                        setViewVisibility(R.id.hourly_container, android.view.View.VISIBLE)
                        renderHourly(this, context, prefs)
                    } else {
                        setViewVisibility(R.id.hourly_container, android.view.View.GONE)
                    }
                } else {
                    val showAlarms = prefs.getBoolean(Prefs.WIDGET_SHOW_ALARMS, true) && alarms.isNotEmpty()
                    setViewVisibility(
                        R.id.alarm_label,
                        if (showAlarms) android.view.View.VISIBLE else android.view.View.GONE,
                    )
                    setViewVisibility(
                        R.id.alarm_status,
                        if (showAlarms) android.view.View.VISIBLE else android.view.View.GONE,
                    )
                    setViewVisibility(R.id.weather_block, android.view.View.VISIBLE)
                    setViewVisibility(R.id.weather_temperature, android.view.View.VISIBLE)
                    setViewVisibility(
                        R.id.weather_condition,
                        if (opts.wide4) android.view.View.VISIBLE else android.view.View.GONE,
                    )
                    setViewVisibility(
                        R.id.weather_location,
                        if (opts.small) android.view.View.GONE else android.view.View.VISIBLE,
                    )
                    setViewVisibility(
                        R.id.weather_uv,
                        if (opts.wide4) android.view.View.VISIBLE else android.view.View.GONE,
                    )
                    setTextViewText(
                        R.id.weather_uv,
                        prefs.getString(Prefs.WEATHER_UV, null)?.let { "UV $it" } ?: "",
                    )
                    setViewVisibility(R.id.clock_row, android.view.View.GONE)
                    setViewVisibility(R.id.clock_col, android.view.View.VISIBLE)
                    setViewVisibility(R.id.date_top, android.view.View.VISIBLE)
                    val narrow4 = !opts.wide4
                    val timeSp = if (narrow4) 28f else 32f
                    setTextViewTextSize(
                        R.id.time_col,
                        android.util.TypedValue.COMPLEX_UNIT_SP,
                        timeSp,
                    )
                    if (narrow4) {
                        setTextViewTextSize(R.id.weather_temperature, android.util.TypedValue.COMPLEX_UNIT_SP, 24f)
                        setTextViewTextSize(R.id.weather_location, android.util.TypedValue.COMPLEX_UNIT_SP, 8f)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            setViewLayoutWidth(R.id.weather_icon, 34f, 0)
                            setViewLayoutHeight(R.id.weather_icon, 34f, 0)
                        }
                    }
                    setTextViewText(
                        R.id.alarm_label,
                        context.getString(R.string.alarm_widget_time),
                    )
                    setTextViewText(
                        R.id.alarm_status,
                        if (alarms.isEmpty()) {
                            context.getString(R.string.alarm_widget_off)
                        } else {
                            alarms.joinToString(", ") { String.format("%02d:%02d", it.hour, it.minute) }
                        },
                    )
                    if (showHourly) {
                        setViewVisibility(R.id.hourly_container, android.view.View.VISIBLE)
                        renderHourly(this, context, prefs)
                    } else {
                        setViewVisibility(R.id.hourly_container, android.view.View.GONE)
                    }
                }

                applyConfig(this, context, opts, prefs)

                val tap = PendingIntent.getBroadcast(
                    context,
                    6102,
                    Intent(context, ClockWeatherWidget::class.java).setAction(ACTION_TAP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                setOnClickPendingIntent(R.id.widget_root, tap)
            }
        }

        private fun applyConfig(
            rv: RemoteViews,
            context: Context,
            opts: WidgetOptions,
            prefs: android.content.SharedPreferences,
        ) {
            val key = Prefs.cellKeyFromSize(opts.minWidth, opts.minHeight)
            val cfg = Prefs.widgetConfig(context, key)
            val sp = android.util.TypedValue.COMPLEX_UNIT_SP
            rv.setTextViewTextSize(R.id.time_col, sp, cfg.clockSp)
            rv.setTextViewTextSize(R.id.weather_temperature, sp, cfg.tempSp)
            rv.setTextViewTextSize(R.id.weather_condition, sp, cfg.condSp)
            rv.setTextViewTextSize(R.id.weather_location, sp, cfg.locSp)
            rv.setTextViewTextSize(R.id.weather_uv, sp, cfg.uvSp)
            rv.setTextViewTextSize(R.id.alarm_status, sp, cfg.alarmSp)
            rv.setTextViewTextSize(R.id.alarm_label, sp, cfg.alarmSp)
            rv.setTextColor(R.id.time_col, cfg.clockColor)
            rv.setTextColor(R.id.weather_temperature, cfg.tempColor)
            rv.setTextColor(R.id.weather_condition, cfg.condColor)
            rv.setTextColor(R.id.weather_location, cfg.locColor)
            rv.setTextColor(R.id.weather_uv, cfg.uvColor)
            rv.setTextColor(R.id.alarm_status, cfg.alarmColor)
            rv.setTextColor(R.id.alarm_label, cfg.alarmColor)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                rv.setViewLayoutWidth(R.id.weather_icon, cfg.iconDp, 0)
                rv.setViewLayoutHeight(R.id.weather_icon, cfg.iconDp, 0)
            }
            rv.setViewVisibility(
                R.id.weather_location,
                if (cfg.showLocation) android.view.View.VISIBLE else android.view.View.GONE,
            )
            rv.setViewVisibility(
                R.id.weather_condition,
                if (cfg.showCondition) android.view.View.VISIBLE else android.view.View.GONE,
            )
            rv.setViewVisibility(
                R.id.weather_uv,
                if (cfg.showUv) android.view.View.VISIBLE else android.view.View.GONE,
            )
            if (!cfg.showAlarms) {
                rv.setViewVisibility(R.id.alarm_block, android.view.View.GONE)
                rv.setViewVisibility(R.id.alarm_label, android.view.View.GONE)
                rv.setViewVisibility(R.id.alarm_status, android.view.View.GONE)
            }
            if (!cfg.showHourly) {
                rv.setViewVisibility(R.id.hourly_container, android.view.View.GONE)
            }
            rv.removeAllViews(R.id.hourly_container)
            if (cfg.showHourly && opts.showHourly) {
                rv.setViewVisibility(R.id.hourly_container, android.view.View.VISIBLE)
                renderHourly(rv, context, prefs, cfg)
            }
        }

        private fun renderHourly(
            rv: RemoteViews,
            context: Context,
            prefs: android.content.SharedPreferences,
            cfg: Prefs.WidgetConfig = Prefs.WidgetConfig(),
        ) {
            val hourly = prefs.getString(Prefs.WEATHER_HOURLY, null) ?: return
            val now = java.time.LocalTime.now()
            rv.removeAllViews(R.id.hourly_container)
            var added = 0
            hourly.split("|").forEach { entry ->
                if (added >= 5) return@forEach
                val parts = entry.split(";")
                if (parts.size >= 2) {
                    val entryTime = try {
                        java.time.LocalTime.parse(parts[0])
                    } catch (_: Exception) {
                        null
                    }
                    if (entryTime == null || !entryTime.isAfter(now)) return@forEach
                    val item = RemoteViews(context.packageName, R.layout.widget_hour_item)
                    item.setTextViewText(R.id.hour_time, parts[0])
                    item.setTextViewText(R.id.hour_temp, "${parts[1]}°")
                    item.setTextViewTextSize(R.id.hour_time, android.util.TypedValue.COMPLEX_UNIT_SP, cfg.hourTimeSp)
                    item.setTextViewTextSize(R.id.hour_temp, android.util.TypedValue.COMPLEX_UNIT_SP, cfg.hourTempSp)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        item.setViewLayoutWidth(R.id.hour_icon, cfg.hourIconDp, 0)
                        item.setViewLayoutHeight(R.id.hour_icon, cfg.hourIconDp, 0)
                    }
                    item.setImageViewResource(
                        R.id.hour_icon,
                        weatherIconRes(parts.getOrNull(2)?.toIntOrNull() ?: -1),
                    )
                    rv.addView(R.id.hourly_container, item)
                    added++
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

        private fun weatherIconRes(code: Int): Int = when (code) {
            0, 1 -> R.drawable.ic_weather_clear
            2 -> R.drawable.ic_weather_partly_cloudy
            45, 48 -> R.drawable.ic_weather_fog
            51, 53, 55, 56, 57, 66 -> R.drawable.ic_weather_drizzle
            61, 63, 65, 67, 80, 81, 82 -> R.drawable.ic_weather_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_weather_snow
            95, 96, 99 -> R.drawable.ic_weather_thunderstorm
            else -> R.drawable.ic_weather_cloudy
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
