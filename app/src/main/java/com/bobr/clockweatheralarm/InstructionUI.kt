package com.bobr.clockweatheralarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Colors
// ---------------------------------------------------------------------------

private val PaperBackground = Color(0xFFF7F1E3)
private val PaperCard = Color(0xFFFAF5E9)
private val PaperWarmTint = Color(0xFFF6EBDC)
private val PaperSelected = Color(0xFFE9E3B9)

private val OlivePrimary = Color(0xFF687127)
private val OliveDark = Color(0xFF4F581D)
private val OliveMuted = Color(0xFF858953)
private val OlivePale = Color(0xFFD8D4A5)

private val MustardYellow = Color(0xFFF4B51D)
private val SunOrange = Color(0xFFEFA527)
private val WarmOrange = Color(0xFFE47E33)
private val CoralCheek = Color(0xFFE98B68)

private val TextBrown = Color(0xFF473527)
private val TextMuted = Color(0xFF776A57)

private val BorderBeige = Color(0xFFDED0AD)
private val DividerBeige = Color(0xFFE4D8BC)

private val CloudFill = Color(0xFFF6F1E4)
private val CloudBlue = Color(0xFF7796A4)
private val RainBlue = Color(0xFF54788D)
private val CloudGray = Color(0xFF747B7D)

private val DisabledTrack = Color(0xFFBBB6AA)
private val DisabledText = Color(0xFF969083)

private val Ink = Color(0xFF3E2F22)

// ---------------------------------------------------------------------------
// Models / demo state
// ---------------------------------------------------------------------------

private enum class InstructionTab { Clock, Alarms, Weather, More }

private enum class WeatherType { Clear, PartlyCloudy, Cloudy, Fog, Drizzle, Rain, Snow, Thunderstorm }

private enum class AlarmArtwork { Coffee, Plant, Moon }

private data class ForecastUiModel(
    val day: String,
    val weatherType: WeatherType,
    val high: Int,
    val low: Int,
)

private data class WeatherUiModel(
    val location: String,
    val condition: String,
    val temperature: String,
    val high: String,
    val low: String,
    val wind: String,
)

private data class DailyUiModel(
    val date: LocalDate,
    val weatherCode: Int,
    val high: Int,
    val low: Int,
)

private val DemoLocation = "Portland, OR"
private const val DemoCondition = "Partly Sunny"
private const val DemoTemperature = "72°"
private const val DemoHigh = "76°"
private const val DemoLow = "54°"
private const val DemoWind = "5 mph NW"

private val DemoWeather = WeatherUiModel(
    location = DemoLocation,
    condition = DemoCondition,
    temperature = DemoTemperature,
    high = DemoHigh,
    low = DemoLow,
    wind = DemoWind,
)

private val DemoForecast = listOf(
    ForecastUiModel("SUN", WeatherType.PartlyCloudy, 76, 54),
    ForecastUiModel("MON", WeatherType.Clear, 78, 55),
    ForecastUiModel("TUE", WeatherType.Rain, 68, 50),
    ForecastUiModel("WED", WeatherType.PartlyCloudy, 72, 52),
    ForecastUiModel("THU", WeatherType.Clear, 77, 53),
)

private val DemoAlarms = listOf(
    SavedAlarm(id = 1, hour = 7, minute = 0, enabled = true, daysMask = AlarmStore.ALL_DAYS, soundUri = null, soundName = "Default alarm"),
    SavedAlarm(id = 2, hour = 8, minute = 30, enabled = true, daysMask = AlarmStore.ALL_DAYS, soundUri = null, soundName = "Default alarm"),
    SavedAlarm(id = 3, hour = 9, minute = 30, enabled = false, daysMask = AlarmStore.ALL_DAYS, soundUri = null, soundName = "Default alarm"),
)

// ---------------------------------------------------------------------------
// InstructionUI
// ---------------------------------------------------------------------------

@Composable
fun InstructionUI(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var selectedTab by rememberSaveable { mutableStateOf(InstructionTab.Clock) }
    var alarmEditorVisible by remember { mutableStateOf(false) }
    var editingAlarmId by remember { mutableStateOf<Int?>(null) }

    var alarms by remember { mutableStateOf(if (isPreview) DemoAlarms else AlarmStore.load(context)) }
    var weather by remember { mutableStateOf(if (isPreview) DemoWeather else loadWeatherUi(context)) }
    var forecast by remember { mutableStateOf(if (isPreview) DemoForecast else loadForecastUi(context)) }
    var weatherNeedsRefresh by remember { mutableStateOf(false) }

    if (!isPreview) {
        LaunchedEffect(weatherNeedsRefresh) {
            if (weatherNeedsRefresh) {
                weatherNeedsRefresh = false
                delay(6_000)
                weather = loadWeatherUi(context)
                forecast = loadForecastUi(context)
            }
        }
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000)
                weather = loadWeatherUi(context)
                forecast = loadForecastUi(context)
            }
        }
    }

    fun refreshAlarms() {
        if (!isPreview) alarms = AlarmStore.load(context)
    }

    fun scheduleSaved(alarm: SavedAlarm) {
        AlarmScheduler.cancel(context, alarm.id)
        AlarmStore.save(context, alarm)
        val scheduled = !alarm.enabled || (
            ensureExactAlarmPermission(context) && AlarmScheduler.schedule(context, alarm)
            )
        ClockWeatherWidget.updateAll(context)
        refreshAlarms()
        toast(
            context,
            if (scheduled) "Alarm saved" else "Allow exact alarms, then save again.",
        )
    }

    MaterialTheme {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = PaperBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                BottomNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .paperTexture(),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    TopControls(
                        onMenu = { selectedTab = InstructionTab.More },
                        onSettings = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                        },
                    )
                    when (selectedTab) {
                        InstructionTab.Clock -> {
                            LargeClock(
                                time = rememberClockText(isPreview),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            DateLabel(
                                date = if (isPreview) {
                                    "Saturday, May 18, 2024"
                                } else {
                                    LocalDate.now().format(DAY_FORMAT)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(14.dp))
                            CurrentWeatherCard(
                                location = weather.location,
                                condition = weather.condition,
                                temperature = weather.temperature,
                                high = weather.high,
                                low = weather.low,
                                wind = weather.wind,
                                onClick = {
                                    weatherNeedsRefresh = true
                                    WeatherScheduler.refreshNow(context)
                                    toast(context, "Refreshing weather…")
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            ForecastCard(forecast = forecast)
                            Spacer(Modifier.height(6.dp))
                            AlarmsHeader(onAdd = {
                                editingAlarmId = null
                                alarmEditorVisible = true
                            })
                            Spacer(Modifier.height(8.dp))
                            AlarmsList(
                                alarms = alarms,
                                onToggle = { alarm, enabled ->
                                    val updated = alarm.copy(enabled = enabled)
                                    AlarmScheduler.cancel(context, alarm.id)
                                    AlarmStore.save(context, updated)
                                    if (enabled && ensureExactAlarmPermission(context)) {
                                        AlarmScheduler.schedule(context, updated)
                                    }
                                    ClockWeatherWidget.updateAll(context)
                                    refreshAlarms()
                                },
                                onClick = { alarm ->
                                    editingAlarmId = alarm.id
                                    alarmEditorVisible = true
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        InstructionTab.Alarms -> {
                            Spacer(Modifier.height(8.dp))
                            AlarmsHeader(onAdd = {
                                editingAlarmId = null
                                alarmEditorVisible = true
                            })
                            Spacer(Modifier.height(8.dp))
                            AlarmsList(
                                alarms = alarms,
                                onToggle = { alarm, enabled ->
                                    val updated = alarm.copy(enabled = enabled)
                                    AlarmScheduler.cancel(context, alarm.id)
                                    AlarmStore.save(context, updated)
                                    if (enabled && ensureExactAlarmPermission(context)) {
                                        AlarmScheduler.schedule(context, updated)
                                    }
                                    ClockWeatherWidget.updateAll(context)
                                    refreshAlarms()
                                },
                                onClick = { alarm ->
                                    editingAlarmId = alarm.id
                                    alarmEditorVisible = true
                                },
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        InstructionTab.Weather -> {
                            Spacer(Modifier.height(8.dp))
                            CurrentWeatherCard(
                                location = weather.location,
                                condition = weather.condition,
                                temperature = weather.temperature,
                                high = weather.high,
                                low = weather.low,
                                wind = weather.wind,
                                onClick = {
                                    weatherNeedsRefresh = true
                                    WeatherScheduler.refreshNow(context)
                                    toast(context, "Refreshing weather…")
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            ForecastCard(forecast = forecast)
                            Spacer(Modifier.height(16.dp))
                        }
                        InstructionTab.More -> {
                            MoreTabContent(
                                context = context,
                                onRefreshNow = {
                                    weatherNeedsRefresh = true
                                    WeatherScheduler.refreshNow(context)
                                    toast(context, "Refreshing weather…")
                                },
                                onOpenMainSettings = {
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        if (alarmEditorVisible) {
            val editing = editingAlarmId?.let { AlarmStore.find(context, it) }
            AlarmEditorDialog(
                existing = editing,
                onDismiss = { alarmEditorVisible = false },
                onDelete = editing?.let { alarm ->
                    {
                        AlarmScheduler.cancel(context, alarm.id)
                        AlarmStore.delete(context, alarm.id)
                        ClockWeatherWidget.updateAll(context)
                        alarmEditorVisible = false
                        refreshAlarms()
                    }
                },
                onSave = { alarm -> scheduleSaved(alarm); alarmEditorVisible = false },
            )
        }
    }
}

private fun ensureExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val manager = context.getSystemService(AlarmManager::class.java)
    if (manager.canScheduleExactAlarms()) return true
    context.startActivity(
        Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}"),
        ),
    )
    return false
}

private fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

private fun loadWeatherUi(context: Context): WeatherUiModel {
    val prefs = Prefs.values(context)
    val location = prefs.getString(Prefs.LOCATION_NAME, null)
        ?: context.getString(R.string.set_location)
    val temperature = prefs.getString(Prefs.WEATHER_TEMP, null)
    val wind = prefs.getInt(Prefs.WEATHER_WIND, -1)
    val windDir = prefs.getInt(Prefs.WEATHER_WIND_DIR, -1)
    val daily = loadDailyUi(context)
    val today = daily.firstOrNull()
    return WeatherUiModel(
        location = location,
        condition = weatherConditionText(prefs.getInt(Prefs.WEATHER_CODE, -1)),
        temperature = temperature?.let { "$it°" } ?: context.getString(R.string.weather_waiting),
        high = today?.let { "${it.high}°" } ?: "--°",
        low = today?.let { "${it.low}°" } ?: "--°",
        wind = if (wind >= 0 && windDir >= 0) {
            "$wind mph ${windCompass(windDir)}"
        } else {
            "--"
        },
    )
}

private fun loadForecastUi(context: Context): List<ForecastUiModel> =
    loadDailyUi(context)
        .filterNot { it.date == java.time.LocalDate.now() }
        .take(5)
        .map { daily ->
            ForecastUiModel(
                day = daily.date.format(DAY_SHORT_FORMAT).uppercase(Locale.getDefault()),
                weatherType = weatherType(daily.weatherCode),
                high = daily.high,
                low = daily.low,
            )
        }

private fun loadDailyUi(context: Context): List<DailyUiModel> {
    val raw = Prefs.values(context).getString(Prefs.WEATHER_DAILY, null) ?: return emptyList()
    return raw.split("|").mapNotNull { entry ->
        val parts = entry.split(";")
        if (parts.size < 4) return@mapNotNull null
        try {
            DailyUiModel(
                date = java.time.LocalDate.parse(parts[0]),
                weatherCode = parts[1].toInt(),
                high = parts[2].toInt(),
                low = parts[3].toInt(),
            )
        } catch (_: Exception) {
            null
        }
    }
}

private fun weatherConditionText(code: Int): String = when (code) {
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
private fun weatherEmotionLabel(code: Int): String = when (code) {
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

private fun weatherType(code: Int): WeatherType = when (code) {
    0, 1 -> WeatherType.Clear
    2 -> WeatherType.PartlyCloudy
    3 -> WeatherType.Cloudy
    45, 48 -> WeatherType.Fog
    51, 53, 55 -> WeatherType.Drizzle
    56, 57, 66 -> WeatherType.Drizzle
    61, 63, 80, 81 -> WeatherType.Rain
    65, 67 -> WeatherType.Rain
    71, 73, 75, 77, 85, 86 -> WeatherType.Snow
    82 -> WeatherType.Rain
    95, 96, 99 -> WeatherType.Thunderstorm
    else -> WeatherType.Cloudy
}

private fun windCompass(deg: Int): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[((deg + 22) / 45) % 8]
}

private fun daysLabel(mask: Int): String {
    if (mask == AlarmStore.ALL_DAYS) return "Every day"
    val days = listOf(
        "Mon" to Calendar.MONDAY,
        "Tue" to Calendar.TUESDAY,
        "Wed" to Calendar.WEDNESDAY,
        "Thu" to Calendar.THURSDAY,
        "Fri" to Calendar.FRIDAY,
        "Sat" to Calendar.SATURDAY,
        "Sun" to Calendar.SUNDAY,
    )
    return days.filter { (_, calendarDay) ->
        mask and (1 shl (calendarDay - Calendar.SUNDAY)) != 0
    }.joinToString(" ") { it.first }
}

private fun alarmArtwork(alarm: SavedAlarm): AlarmArtwork = when (alarm.id % 3) {
    1 -> AlarmArtwork.Coffee
    2 -> AlarmArtwork.Plant
    else -> AlarmArtwork.Moon
}

private val DAY_SHORT_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE", Locale.US)

private val DAY_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
private val CLOCK_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

@Composable
private fun rememberClockText(isPreview: Boolean): String {
    val text by produceState(initialValue = "09:41", key1 = isPreview) {
        if (!isPreview) {
            while (true) {
                value = LocalTime.now().format(CLOCK_FORMAT)
                delay(1000)
            }
        }
    }
    return text
}

// ---------------------------------------------------------------------------
// Paper texture + hand-drawn borders
// ---------------------------------------------------------------------------

private fun Modifier.paperTexture(): Modifier = drawWithCache {
    val rnd = Random(20240518)
    val specs = List(260) {
        Triple(
            rnd.nextFloat() * size.width,
            rnd.nextFloat() * size.height,
            rnd.nextFloat(),
        )
    }
    val dotRadius = 1.1f.dp.toPx()
    val grain = TextMuted.copy(alpha = 0.035f)
    onDrawBehind {
        specs.forEach { (x, y, t) ->
            val r = dotRadius * (0.6f + t * 1.1f)
            drawCircle(grain, r, Offset(x, y))
        }
    }
}

private fun Modifier.handDrawnBorder(
    color: Color = BorderBeige,
    cornerRadius: Dp = 24.dp,
    strokeWidth: Dp = 1.dp,
): Modifier = drawWithCache {
    val w = strokeWidth.toPx()
    val r = cornerRadius.toPx()
    onDrawBehind {
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.8f, w * 0.8f),
            size = Size(size.width - w * 1.6f, size.height - w * 1.6f),
            cornerRadius = CornerRadius(r, r),
            style = Stroke(w),
        )
        drawRoundRect(
            color = color.copy(alpha = 0.3f),
            topLeft = Offset(w * 1.8f, w * 1.4f),
            size = Size(size.width - w * 3.6f, size.height - w * 2.8f),
            cornerRadius = CornerRadius(r * 1.08f, r * 1.08f),
            style = Stroke(w * 0.7f),
        )
    }
}

// ---------------------------------------------------------------------------
// Top controls
// ---------------------------------------------------------------------------

@Composable
private fun TopControls(onMenu: () -> Unit, onSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MenuIcon(
            onClick = onMenu,
            contentDescription = "Open menu",
        )
        SettingsIcon(
            onClick = onSettings,
            contentDescription = "Open settings",
        )
    }
}

@Composable
private fun Modifier.olivePress(onClick: () -> Unit): Modifier = this.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = androidx.compose.material3.ripple(color = OlivePrimary.copy(alpha = 0.18f)),
    onClick = onClick,
)

@Composable
private fun MenuIcon(onClick: () -> Unit, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .olivePress(onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(30.dp)) {
            val w = size.width
            val y1 = size.height * 0.22f
            val y2 = size.height * 0.5f
            val y3 = size.height * 0.78f
            val inset = size.width * 0.08f
            drawLine(
                color = OlivePrimary,
                start = Offset(inset, y1),
                end = Offset(w - inset * 1.3f, y1),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = OlivePrimary,
                start = Offset(inset * 1.3f, y2),
                end = Offset(w - inset, y2),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = OlivePrimary,
                start = Offset(inset, y3),
                end = Offset(w - inset * 0.8f, y3),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun SettingsIcon(onClick: () -> Unit, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .olivePress(onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(30.dp)) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.width * 0.30f
            val teeth = size.width * 0.16f
            val sw = 1.8.dp.toPx()
            repeat(8) { i ->
                val a = (i * 45f + 11.25f) * (Math.PI / 180.0)
                val cx = cos(a).toFloat()
                val sy = sin(a).toFloat()
                drawLine(
                    color = OlivePrimary,
                    start = Offset(c.x + cx * r, c.y + sy * r),
                    end = Offset(c.x + cx * (r + teeth), c.y + sy * (r + teeth)),
                    strokeWidth = sw,
                    cap = StrokeCap.Round,
                )
            }
            drawCircle(
                color = OlivePrimary,
                radius = r,
                center = c,
                style = Stroke(sw),
            )
            drawCircle(
                color = OlivePrimary,
                radius = r * 0.42f,
                center = c,
                style = Stroke(sw),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Clock + date
// ---------------------------------------------------------------------------

@Composable
private fun LargeClock(time: String, modifier: Modifier = Modifier) {
    val annotated = buildAnnotatedString {
        val parts = time.split(":")
        append(parts.getOrElse(0) { "09" })
        withStyle(SpanStyle(color = MustardYellow)) { append(":") }
        append(parts.getOrElse(1) { "41" })
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.BoxWithConstraints(
            Modifier.fillMaxWidth(),
        ) {
            val clockSize = if (maxWidth < 360.dp) 74.sp else 82.sp
            Text(
                text = annotated,
                fontSize = clockSize,
                color = OlivePrimary,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DateLabel(date: String, modifier: Modifier = Modifier) {
    Text(
        text = date,
        modifier = modifier.padding(top = 6.dp),
        fontSize = 21.sp,
        color = TextBrown,
        fontFamily = FontFamily.Cursive,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

// ---------------------------------------------------------------------------
// Current weather card
// ---------------------------------------------------------------------------

@Composable
private fun CurrentWeatherCard(
    location: String,
    condition: String,
    temperature: String,
    high: String,
    low: String,
    wind: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(PaperCard.copy(alpha = 0.85f))
            .handDrawnBorder(cornerRadius = 24.dp)
            .olivePress(onClick)
            .semantics {
                contentDescription =
                    "$location, $condition, $temperature, high $high, low $low, wind $wind"
            }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.44f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            SunWithClouds(Modifier.size(120.dp, 105.dp))
        }
        Column(
            modifier = Modifier
                .weight(0.56f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LocationPin(Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = location,
                    fontSize = 17.sp,
                    color = OliveDark,
                    fontFamily = FontFamily.Cursive,
                    maxLines = 1,
                )
            }
            Text(
                text = condition,
                fontSize = 18.sp,
                color = TextBrown,
                fontFamily = FontFamily.Cursive,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = temperature,
                    fontSize = 58.sp,
                    color = OlivePrimary,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "H: $high",
                        fontSize = 17.sp,
                        color = WarmOrange,
                        fontFamily = FontFamily.Cursive,
                    )
                    Text(
                        text = "L: $low",
                        fontSize = 17.sp,
                        color = RainBlue,
                        fontFamily = FontFamily.Cursive,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                WindIcon(Modifier.size(16.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    text = wind,
                    fontSize = 15.sp,
                    color = OliveMuted,
                    fontFamily = FontFamily.Cursive,
                )
            }
        }
    }
}

@Composable
private fun LocationPin(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height * 0.42f)
        val r = size.width * 0.22f
        drawCircle(OlivePrimary, r, c)
        val path = Path()
        path.moveTo(c.x - r * 0.75f, c.y + r * 0.4f)
        path.lineTo(c.x, size.height * 0.92f)
        path.lineTo(c.x + r * 0.75f, c.y + r * 0.4f)
        path.close()
        drawPath(path, OlivePrimary)
        drawCircle(PaperCard, r * 0.42f, c)
    }
}

@Composable
private fun WindIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sw = 1.6.dp.toPx()
        val xs = listOf(0.9f, 1f, 0.82f)
        val ys = listOf(0.24f, 0.5f, 0.76f)
        xs.forEachIndexed { i, length ->
            val xEnd = size.width * length
            val y = size.height * ys[i]
            val path = Path().apply {
                moveTo(0f, y)
                quadraticBezierTo(size.width * 0.5f, y - size.height * 0.14f, xEnd, y)
            }
            drawPath(path, OliveMuted, style = Stroke(sw, cap = StrokeCap.Round))
        }
    }
}

// ---------------------------------------------------------------------------
// Five-day forecast card
// ---------------------------------------------------------------------------

@Composable
private fun ForecastCard(forecast: List<ForecastUiModel>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PaperCard)
            .handDrawnBorder(cornerRadius = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        forecast.forEachIndexed { index, day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = day.day,
                    fontSize = 16.sp,
                    color = OliveDark,
                    fontFamily = FontFamily.Cursive,
                )
                WeatherIcon(day.weatherType, Modifier.size(34.dp))
                Text(
                    text = "${day.high}°",
                    fontSize = 18.sp,
                    color = TextBrown,
                    fontFamily = FontFamily.Cursive,
                )
                Text(
                    text = "${day.low}°",
                    fontSize = 16.sp,
                    color = RainBlue,
                    fontFamily = FontFamily.Cursive,
                )
            }
            if (index < forecast.lastIndex) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .padding(vertical = 12.dp)
                        .background(DividerBeige),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Alarms header + rows
// ---------------------------------------------------------------------------

@Composable
private fun AlarmsHeader(onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AlarmClockIcon(Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Alarms",
            fontSize = 24.sp,
            color = OliveDark,
            fontFamily = FontFamily.Cursive,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(48.dp)
                .olivePress(onAdd)
                .semantics { contentDescription = "Add alarm" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(OlivePrimary),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(20.dp)) {
                    val sw = 2.2.dp.toPx()
                    drawLine(
                        color = PaperWarmTint,
                        start = Offset(size.width * 0.28f, size.height / 2f),
                        end = Offset(size.width * 0.72f, size.height / 2f),
                        strokeWidth = sw,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = PaperWarmTint,
                        start = Offset(size.width / 2f, size.height * 0.28f),
                        end = Offset(size.width / 2f, size.height * 0.72f),
                        strokeWidth = sw,
                        cap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmClockIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height * 0.52f)
        val r = size.width * 0.34f
        val sw = 1.8.dp.toPx()
        drawCircle(OliveDark, r, c, style = Stroke(sw))
        drawLine(
            color = OliveDark,
            start = c,
            end = Offset(c.x, c.y - r * 0.62f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = OliveDark,
            start = c,
            end = Offset(c.x + r * 0.55f, c.y + r * 0.2f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
        val bellW = r * 0.9f
        drawArc(
            color = OliveDark,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(c.x - bellW, c.y - r * 1.25f),
            size = Size(bellW * 2f, r * 0.9f),
            style = Stroke(sw),
        )
        drawLine(
            color = OliveDark,
            start = Offset(c.x - r * 0.5f, c.y - r * 1.05f),
            end = Offset(c.x - r * 0.75f, c.y - r * 0.95f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = OliveDark,
            start = Offset(c.x + r * 0.5f, c.y - r * 1.05f),
            end = Offset(c.x + r * 0.75f, c.y - r * 0.95f),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun AlarmsList(
    alarms: List<SavedAlarm>,
    onToggle: (SavedAlarm, Boolean) -> Unit,
    onClick: (SavedAlarm) -> Unit,
) {
    if (alarms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PaperWarmTint.copy(alpha = 0.5f))
                .handDrawnBorder(cornerRadius = 18.dp)
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No alarms yet. Tap + to add one.",
                fontSize = 16.sp,
                color = TextMuted,
                fontFamily = FontFamily.Cursive,
                textAlign = TextAlign.Center,
            )
        }
        return
    }
    alarms.forEach { alarm ->
        AlarmRow(
            alarm = alarm,
            onToggle = { enabled -> onToggle(alarm, enabled) },
            onClick = { onClick(alarm) },
        )
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun AlarmRow(
    alarm: SavedAlarm,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
) {
    val time = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val schedule = daysLabel(alarm.daysMask)
    val artwork = alarmArtwork(alarm)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperWarmTint.copy(alpha = 0.7f))
            .handDrawnBorder(cornerRadius = 18.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .olivePress(onClick)
                .semantics {
                    contentDescription = "$time, $schedule, " +
                        if (alarm.enabled) "enabled" else "disabled"
                },
            contentAlignment = Alignment.Center,
        ) {
            AlarmArtworkIcon(artwork, Modifier.size(44.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .olivePress(onClick),
        ) {
            Text(
                text = time,
                fontSize = 33.sp,
                color = if (alarm.enabled) TextBrown else DisabledText,
                fontFamily = FontFamily.Cursive,
                lineHeight = 34.sp,
            )
            Text(
                text = schedule,
                fontSize = 15.sp,
                color = if (alarm.enabled) OliveDark else DisabledText,
                fontFamily = FontFamily.Cursive,
                maxLines = 1,
            )
        }
        CustomSwitch(
            checked = alarm.enabled,
            onCheckedChange = onToggle,
            modifier = Modifier.semantics {
                stateDescription = if (alarm.enabled) "Alarm enabled" else "Alarm disabled"
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Custom switch
// ---------------------------------------------------------------------------

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "switch-thumb",
    )
    Box(
        modifier = modifier
            .width(52.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) OlivePrimary else DisabledTrack)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(24.dp)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(PaperWarmTint),
        )
    }
}

// ---------------------------------------------------------------------------
// Bottom navigation
// ---------------------------------------------------------------------------

@Composable
private fun BottomNavigationBar(
    selectedTab: InstructionTab,
    onTabSelected: (InstructionTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp)
            .background(PaperBackground)
            .handDrawnBorder(color = DividerBeige, cornerRadius = 0.dp, strokeWidth = 1.dp)
            .drawWithCache {
                onDrawBehind {
                    drawLine(
                        color = DividerBeige,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            },
    ) {
        InstructionTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PaperSelected else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onTabSelected(tab) },
                    )
                    .semantics {
                        selected = isSelected
                        contentDescription = tab.name
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TabIcon(tab, isSelected, Modifier.size(26.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tab.name,
                        fontSize = 13.sp,
                        color = if (isSelected) OlivePrimary else TextBrown,
                        fontFamily = FontFamily.Cursive,
                    )
                }
            }
        }
    }
}

@Composable
private fun TabIcon(tab: InstructionTab, selected: Boolean, modifier: Modifier = Modifier) {
    val color = if (selected) OlivePrimary else TextBrown
    Canvas(modifier) {
        val sw = 1.7.dp.toPx()
        when (tab) {
            InstructionTab.Clock -> {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.width * 0.34f
                drawCircle(color, r, c, style = Stroke(sw))
                drawLine(color, c, Offset(c.x, c.y - r * 0.6f), sw, StrokeCap.Round)
                drawLine(color, c, Offset(c.x + r * 0.5f, c.y + r * 0.18f), sw, StrokeCap.Round)
            }
            InstructionTab.Alarms -> {
                val c = Offset(size.width / 2f, size.height * 0.54f)
                val r = size.width * 0.3f
                drawCircle(color, r, c, style = Stroke(sw))
                drawLine(color, c, Offset(c.x, c.y - r * 0.55f), sw, StrokeCap.Round)
                drawLine(color, c, Offset(c.x + r * 0.5f, c.y + r * 0.18f), sw, StrokeCap.Round)
                drawArc(
                    color = color,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(c.x - r * 0.95f, c.y - r * 1.3f),
                    size = Size(r * 1.9f, r * 0.85f),
                    style = Stroke(sw),
                )
            }
            InstructionTab.Weather -> {
                val sunC = Offset(size.width * 0.62f, size.height * 0.34f)
                val sunR = size.width * 0.22f
                drawCircle(MustardYellow, sunR * 0.8f, sunC)
                val cloudC = Offset(size.width * 0.4f, size.height * 0.62f)
                drawCloud(cloudC.x, cloudC.y, size.width * 0.7f, size.height * 0.4f, sw)
            }
            InstructionTab.More -> {
                val w = size.width * 0.6f
                val h = size.height * 0.6f
                val tl = Offset((size.width - w) / 2f, (size.height - h) / 2f)
                val corner = CornerRadius(size.width * 0.08f, size.width * 0.08f)
                drawRoundRect(color, tl, Size(w, h), corner, style = Stroke(sw))
                drawLine(
                    color,
                    Offset(size.width / 2f, tl.y),
                    Offset(size.width / 2f, tl.y + h),
                    sw,
                )
                drawLine(
                    color,
                    Offset(tl.x, size.height / 2f),
                    Offset(tl.x + w, size.height / 2f),
                    sw,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Weather icon system (Canvas)
// ---------------------------------------------------------------------------

@Composable
private fun WeatherIcon(type: WeatherType, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (type) {
            WeatherType.Clear -> drawClearIcon()
            WeatherType.PartlyCloudy -> drawPartlyCloudyIcon()
            WeatherType.Cloudy -> drawCloudyIcon()
            WeatherType.Fog -> drawFogIcon()
            WeatherType.Drizzle -> drawDrizzleIcon()
            WeatherType.Rain -> drawRainIcon()
            WeatherType.Snow -> drawSnowIcon()
            WeatherType.Thunderstorm -> drawThunderstormIcon()
        }
    }
}

private fun DrawScope.drawClearIcon() {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.width * 0.28f
    drawSunBody(c, r, rays = true)
    drawFace(c.x, c.y, r * 0.52f, EyeStyle.Open, MouthStyle.Smile, cheeks = true)
}

private fun DrawScope.drawPartlyCloudyIcon() {
    val sunC = Offset(size.width * 0.62f, size.height * 0.36f)
    val sunR = size.width * 0.24f
    drawSunBody(sunC, sunR, rays = false)
    drawFace(sunC.x, sunC.y, sunR * 0.5f, EyeStyle.Open, MouthStyle.Smile, cheeks = true)
    val cloudC = Offset(size.width * 0.42f, size.height * 0.62f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.66f, size.height * 0.34f, 1.4.dp.toPx())
}

private fun DrawScope.drawCloudyIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.58f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.8f, size.height * 0.4f, 1.5.dp.toPx())
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.04f,
        size.width * 0.16f,
        EyeStyle.Open,
        MouthStyle.Calm,
        cheeks = false,
    )
}

private fun DrawScope.drawFogIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.8f, size.height * 0.36f, 1.5.dp.toPx())
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.03f,
        size.width * 0.15f,
        EyeStyle.HalfClosed,
        MouthStyle.Calm,
        cheeks = false,
    )
    val sw = 1.4.dp.toPx()
    listOf(0.76f, 0.5f, 0.68f).forEachIndexed { i, len ->
        val y = size.height * (0.82f + i * 0.0f) + i * size.height * 0.02f
        val path = Path().apply {
            moveTo(size.width * 0.14f, y)
            lineTo(size.width * (0.14f + len * 0.72f), y)
        }
        drawPath(path, CloudBlue, style = Stroke(sw, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawDrizzleIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.78f, size.height * 0.36f, 1.5.dp.toPx())
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.03f,
        size.width * 0.15f,
        EyeStyle.Open,
        MouthStyle.Calm,
        cheeks = false,
    )
    val dropR = size.width * 0.045f
    val dropY = size.height * 0.78f
    listOf(0.36f, 0.5f, 0.64f).forEach { fx ->
        drawCircle(RainBlue, dropR, Offset(size.width * fx, dropY))
    }
}

private fun DrawScope.drawRainIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.78f, size.height * 0.36f, 1.5.dp.toPx())
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.04f,
        size.width * 0.15f,
        EyeStyle.HalfClosed,
        MouthStyle.Frown,
        cheeks = false,
    )
    val dropR = size.width * 0.045f
    listOf(0.34f, 0.48f, 0.62f).forEachIndexed { i, fx ->
        val dropY = size.height * (0.74f + (i % 2) * 0.06f)
        drawCircle(RainBlue, dropR, Offset(size.width * fx, dropY))
    }
}

private fun DrawScope.drawSnowIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.78f, size.height * 0.36f, 1.5.dp.toPx())
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.03f,
        size.width * 0.15f,
        EyeStyle.Open,
        MouthStyle.Smile,
        cheeks = true,
    )
    drawSnowflake(size.width * 0.36f, size.height * 0.8f, size.width * 0.05f)
    drawSnowflake(size.width * 0.5f, size.height * 0.78f, size.width * 0.05f)
    drawSnowflake(size.width * 0.64f, size.height * 0.8f, size.width * 0.05f)
}

private fun DrawScope.drawThunderstormIcon() {
    val cloudC = Offset(size.width / 2f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.8f, size.height * 0.38f, 1.5.dp.toPx(), fill = CloudGray)
    drawFace(
        cloudC.x,
        cloudC.y + size.height * 0.03f,
        size.width * 0.15f,
        EyeStyle.Angry,
        MouthStyle.Frown,
        cheeks = false,
    )
    val bolt = Path().apply {
        moveTo(size.width * 0.48f, size.height * 0.62f)
        lineTo(size.width * 0.42f, size.height * 0.78f)
        lineTo(size.width * 0.5f, size.height * 0.76f)
        lineTo(size.width * 0.44f, size.height * 0.94f)
        lineTo(size.width * 0.6f, size.height * 0.7f)
        lineTo(size.width * 0.52f, size.height * 0.72f)
        lineTo(size.width * 0.58f, size.height * 0.62f)
        close()
    }
    drawPath(bolt, MustardYellow)
}

private fun DrawScope.drawCloud(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    sw: Float,
    fill: Color = CloudFill,
    outline: Color = CloudBlue,
) {
    val inset = sw * 0.9f
    val lobes = listOf(
        Triple(cx - w * 0.2f, cy, h * 0.36f),
        Triple(cx + w * 0.2f, cy + h * 0.04f, h * 0.3f),
        Triple(cx, cy - h * 0.14f, h * 0.34f),
    )
    lobes.forEach { (lx, ly, lr) ->
        drawCircle(outline, lr, Offset(lx, ly), style = Stroke(sw))
    }
    val baseRect = Offset(cx - w * 0.3f, cy - h * 0.1f)
    drawRoundRect(
        outline,
        baseRect,
        Size(w * 0.6f, h * 0.52f),
        CornerRadius(h * 0.18f),
        style = Stroke(sw),
    )
    lobes.forEach { (lx, ly, lr) ->
        drawCircle(fill, lr - inset, Offset(lx, ly))
    }
    drawRoundRect(
        fill,
        Offset(baseRect.x, baseRect.y + inset),
        Size(w * 0.6f, h * 0.52f),
        CornerRadius(h * 0.16f),
    )
}

private fun DrawScope.drawSnowflake(x: Float, y: Float, r: Float) {
    val sw = r * 0.28f
    repeat(3) { i ->
        val a = (i * 60f) * (Math.PI / 180.0)
        val dx = cos(a).toFloat() * r
        val dy = sin(a).toFloat() * r
        drawLine(
            color = CloudBlue,
            start = Offset(x - dx, y - dy),
            end = Offset(x + dx, y + dy),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSunBody(c: Offset, r: Float, rays: Boolean) {
    if (rays) {
        repeat(12) { i ->
            val a = (i * 30f + 3f) * (Math.PI / 180.0)
            val cx = cos(a).toFloat()
            val sy = sin(a).toFloat()
            val inner = r * 1.12f
            val outer = r * 1.42f
            drawLine(
                color = SunOrange,
                start = Offset(c.x + cx * inner, c.y + sy * inner),
                end = Offset(c.x + cx * outer, c.y + sy * outer),
                strokeWidth = r * 0.14f,
                cap = StrokeCap.Round,
            )
        }
    }
    drawCircle(MustardYellow, r, c)
    drawCircle(SunOrange.copy(alpha = 0.25f), r * 1.02f, Offset(c.x + r * 0.03f, c.y + r * 0.03f), style = Stroke(r * 0.08f))
}

// ---------------------------------------------------------------------------
// Shared face helpers
// ---------------------------------------------------------------------------

private enum class EyeStyle { Open, HalfClosed, Angry }
private enum class MouthStyle { Smile, Calm, Frown }

private fun DrawScope.drawFace(
    cx: Float,
    cy: Float,
    r: Float,
    eyeStyle: EyeStyle,
    mouthStyle: MouthStyle,
    cheeks: Boolean,
) {
    val eyeDx = r * 0.62f
    val eyeY = cy - r * 0.12f
    val eyeR = r * 0.1f
    when (eyeStyle) {
        EyeStyle.Open -> {
            drawCircle(Ink, eyeR, Offset(cx - eyeDx, eyeY))
            drawCircle(Ink, eyeR, Offset(cx + eyeDx, eyeY))
        }
        EyeStyle.HalfClosed -> {
            val sw = r * 0.12f
            drawLine(
                Ink,
                Offset(cx - eyeDx - eyeR, eyeY),
                Offset(cx - eyeDx + eyeR, eyeY),
                sw,
                StrokeCap.Round,
            )
            drawLine(
                Ink,
                Offset(cx + eyeDx - eyeR, eyeY),
                Offset(cx + eyeDx + eyeR, eyeY),
                sw,
                StrokeCap.Round,
            )
        }
        EyeStyle.Angry -> {
            val sw = r * 0.14f
            drawLine(
                Ink,
                Offset(cx - eyeDx - eyeR, eyeY - r * 0.18f),
                Offset(cx - eyeDx + eyeR, eyeY + r * 0.02f),
                sw,
                StrokeCap.Round,
            )
            drawLine(
                Ink,
                Offset(cx + eyeDx - eyeR, eyeY + r * 0.02f),
                Offset(cx + eyeDx + eyeR, eyeY - r * 0.18f),
                sw,
                StrokeCap.Round,
            )
        }
    }
    if (cheeks) {
        drawCircle(CoralCheek.copy(alpha = 0.75f), r * 0.16f, Offset(cx - r * 0.9f, cy + r * 0.5f))
        drawCircle(CoralCheek.copy(alpha = 0.75f), r * 0.16f, Offset(cx + r * 0.9f, cy + r * 0.5f))
    }
    val sw = r * 0.12f
    when (mouthStyle) {
        MouthStyle.Smile -> {
            val mouth = Path().apply {
                moveTo(cx - r * 0.55f, cy + r * 0.25f)
                quadraticBezierTo(cx, cy + r * 0.85f, cx + r * 0.55f, cy + r * 0.25f)
            }
            drawPath(mouth, Ink, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        MouthStyle.Calm -> {
            val mouth = Path().apply {
                moveTo(cx - r * 0.4f, cy + r * 0.5f)
                quadraticBezierTo(cx, cy + r * 0.62f, cx + r * 0.4f, cy + r * 0.5f)
            }
            drawPath(mouth, Ink, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        MouthStyle.Frown -> {
            val mouth = Path().apply {
                moveTo(cx - r * 0.5f, cy + r * 0.5f)
                quadraticBezierTo(cx, cy + r * 0.12f, cx + r * 0.5f, cy + r * 0.5f)
            }
            drawPath(mouth, Ink, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

// ---------------------------------------------------------------------------
// Current weather illustration (sun + two clouds)
// ---------------------------------------------------------------------------

@Composable
private fun SunWithClouds(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sunC = Offset(size.width * 0.5f, size.height * 0.4f)
        val sunR = size.width * 0.32f
        drawSunBody(sunC, sunR, rays = true)
        drawFace(
            sunC.x,
            sunC.y + sunR * 0.05f,
            sunR * 0.55f,
            EyeStyle.Open,
            MouthStyle.Smile,
            cheeks = true,
        )
        drawCloud(size.width * 0.28f, size.height * 0.82f, size.width * 0.52f, size.height * 0.3f, 1.6.dp.toPx())
        drawCloud(size.width * 0.78f, size.height * 0.9f, size.width * 0.4f, size.height * 0.24f, 1.6.dp.toPx())
    }
}

// ---------------------------------------------------------------------------
// Alarm artwork
// ---------------------------------------------------------------------------

@Composable
private fun AlarmArtworkIcon(artwork: AlarmArtwork, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        when (artwork) {
            AlarmArtwork.Coffee -> drawCoffee()
            AlarmArtwork.Plant -> drawPlant()
            AlarmArtwork.Moon -> drawMoon()
        }
    }
}

private fun DrawScope.drawCoffee() {
    val sw = 1.8.dp.toPx()
    val mugW = size.width * 0.5f
    val mugH = size.height * 0.5f
    val mugLeft = size.width * 0.24f
    val mugTop = size.height * 0.38f
    val body = Path().apply {
        moveTo(mugLeft, mugTop)
        lineTo(mugLeft, mugTop + mugH)
        quadraticBezierTo(mugLeft + mugW / 2f, mugTop + mugH * 1.14f, mugLeft + mugW, mugTop + mugH)
        lineTo(mugLeft + mugW, mugTop)
        close()
    }
    drawPath(body, OlivePrimary)
    drawCircle(PaperWarmTint, mugW * 0.14f, Offset(mugLeft + mugW / 2f, mugTop + mugH * 0.4f))
    drawArc(
        color = OliveDark,
        startAngle = 90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(mugLeft + mugW, mugTop + mugH * 0.14f),
        size = Size(mugW * 0.28f, mugH * 0.4f),
        style = Stroke(sw),
    )
    listOf(0.5f, 0.68f, 0.86f).forEach { fy ->
        val path = Path().apply {
            moveTo(mugLeft + mugW * 0.5f, size.height * fy)
            quadraticBezierTo(mugLeft + mugW * 0.3f, size.height * fy - size.height * 0.08f, mugLeft + mugW * 0.14f, size.height * fy - size.height * 0.1f)
        }
        drawPath(path, OliveMuted, style = Stroke(1.4.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawPlant() {
    val potW = size.width * 0.52f
    val potH = size.height * 0.3f
    val potLeft = (size.width - potW) / 2f
    val potTop = size.height * 0.6f
    val sw = 1.6.dp.toPx()
    val pot = Path().apply {
        moveTo(potLeft, potTop)
        lineTo(potLeft + potW * 0.16f, size.height * 0.92f)
        lineTo(potLeft + potW * 0.84f, size.height * 0.92f)
        lineTo(potLeft + potW, potTop)
        close()
    }
    drawPath(pot, WarmOrange)
    drawLine(
        color = SunOrange,
        start = Offset(potLeft + potW * 0.1f, potTop + potH * 0.22f),
        end = Offset(potLeft + potW * 0.9f, potTop + potH * 0.22f),
        strokeWidth = sw,
        cap = StrokeCap.Round,
    )
    drawStem(size.width * 0.38f, size.height * 0.56f)
    drawStem(size.width * 0.5f, size.height * 0.52f)
    drawStem(size.width * 0.62f, size.height * 0.56f)
}

private fun DrawScope.drawStem(topX: Float, topY: Float) {
    val sw = 1.4.dp.toPx()
    drawLine(
        color = OliveDark,
        start = Offset(topX, topY + size.height * 0.08f),
        end = Offset(topX, topY),
        strokeWidth = sw,
        cap = StrokeCap.Round,
    )
    val leaf = Path().apply {
        moveTo(topX, topY + size.height * 0.04f)
        quadraticBezierTo(topX - size.width * 0.14f, topY - size.height * 0.02f, topX - size.width * 0.12f, topY + size.height * 0.1f)
        quadraticBezierTo(topX - size.width * 0.04f, topY + size.height * 0.08f, topX, topY + size.height * 0.04f)
        close()
    }
    drawPath(leaf, OlivePrimary)
}

private fun DrawScope.drawMoon() {
    val c = Offset(size.width * 0.54f, size.height * 0.5f)
    val r = size.width * 0.3f
    drawCircle(MustardYellow, r, c)
    drawCircle(PaperBackground, r * 0.78f, Offset(c.x - r * 0.38f, c.y - r * 0.2f))
    drawFace(c.x + r * 0.08f, c.y + r * 0.05f, r * 0.52f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
    listOf(
        Offset(size.width * 0.16f, size.height * 0.3f),
        Offset(size.width * 0.26f, size.height * 0.6f),
        Offset(size.width * 0.2f, size.height * 0.46f),
    ).forEach { star ->
        drawStar(star.x, star.y, size.width * 0.04f)
    }
}

private fun DrawScope.drawStar(x: Float, y: Float, r: Float) {
    val sw = r * 0.3f
    drawLine(CloudBlue, Offset(x - r, y), Offset(x + r, y), sw, StrokeCap.Round)
    drawLine(CloudBlue, Offset(x, y - r), Offset(x, y + r), sw, StrokeCap.Round)
}

// ---------------------------------------------------------------------------
// More tab
// ---------------------------------------------------------------------------

@Composable
private fun MoreTabContent(
    context: Context,
    onRefreshNow: () -> Unit,
    onOpenMainSettings: () -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Settings",
        fontSize = 24.sp,
        color = OliveDark,
        fontFamily = FontFamily.Cursive,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Refresh weather now",
                    fontSize = 17.sp,
                    color = TextBrown,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fetches the current conditions and the 6-day forecast.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onRefreshNow,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = OlivePrimary,
                        contentColor = PaperWarmTint,
                    ),
                ) {
                    Text("Refresh", fontFamily = FontFamily.Cursive)
                }
            }
        },
    )
    Spacer(Modifier.height(10.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Alarm & widget settings",
                    fontSize = 17.sp,
                    color = TextBrown,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sound, permissions, location, and widget options live in the classic settings.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onOpenMainSettings,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = OlivePrimary,
                        contentColor = PaperWarmTint,
                    ),
                ) {
                    Text("Open settings", fontFamily = FontFamily.Cursive)
                }
            }
        },
    )
}

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(PaperCard)
            .handDrawnBorder(cornerRadius = 22.dp),
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Alarm editor dialog
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditorDialog(
    existing: SavedAlarm?,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (SavedAlarm) -> Unit,
) {
    val context = LocalContext.current
    val isNew = existing == null
    val initialHour = existing?.hour ?: 7
    val initialMinute = existing?.minute ?: 0
    val timeState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }
    var daysMask by remember { mutableIntStateOf(existing?.daysMask ?: AlarmStore.ALL_DAYS) }
    val dayOptions = listOf(
        "Mon" to Calendar.MONDAY,
        "Tue" to Calendar.TUESDAY,
        "Wed" to Calendar.WEDNESDAY,
        "Thu" to Calendar.THURSDAY,
        "Fri" to Calendar.FRIDAY,
        "Sat" to Calendar.SATURDAY,
        "Sun" to Calendar.SUNDAY,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperCard,
        titleContentColor = OliveDark,
        textContentColor = TextBrown,
        title = {
            Text(
                text = if (isNew) "New alarm" else "Edit alarm",
                fontFamily = FontFamily.Cursive,
            )
        },
        text = {
            Column {
                TimePicker(
                    state = timeState,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Enabled",
                        fontSize = 17.sp,
                        color = TextBrown,
                        fontFamily = FontFamily.Cursive,
                        modifier = Modifier.weight(1f),
                    )
                    CustomSwitch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Repeat on",
                    fontSize = 17.sp,
                    color = TextBrown,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    dayOptions.forEach { (name, calendarDay) ->
                        val bit = 1 shl (calendarDay - Calendar.SUNDAY)
                        val selected = daysMask and bit != 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) OlivePrimary else PaperWarmTint)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        daysMask = if (selected) daysMask and bit.inv() else daysMask or bit
                                    },
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = name,
                                fontSize = 14.sp,
                                color = if (selected) PaperWarmTint else OliveDark,
                                fontFamily = FontFamily.Cursive,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val days = if (daysMask == 0) AlarmStore.ALL_DAYS else daysMask
                    val saved = SavedAlarm(
                        id = existing?.id ?: AlarmStore.nextId(context),
                        hour = timeState.hour,
                        minute = timeState.minute,
                        enabled = enabled,
                        daysMask = days,
                        soundUri = existing?.soundUri,
                        soundName = existing?.soundName ?: "Default alarm",
                    )
                    onSave(saved)
                },
            ) {
                Text("Save", fontFamily = FontFamily.Cursive, color = OlivePrimary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", fontFamily = FontFamily.Cursive, color = WarmOrange)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", fontFamily = FontFamily.Cursive, color = TextMuted)
                }
            }
        },
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun InstructionUIPreview() {
    InstructionUI()
}

@Preview(showBackground = true, widthDp = 320, heightDp = 720)
@Composable
private fun InstructionUINarrowPreview() {
    InstructionUI()
}
