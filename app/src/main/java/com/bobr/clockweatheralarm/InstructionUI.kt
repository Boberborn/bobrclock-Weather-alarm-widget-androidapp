package com.bobr.clockweatheralarm

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.PowerManager
import android.provider.OpenableColumns
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
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

private data class HourUiModel(
    val time: String,
    val temperature: Int,
    val weatherCode: Int,
)

private data class DayForecastUiModel(
    val date: LocalDate,
    val label: String,
    val isToday: Boolean,
    val condition: String,
    val weatherType: WeatherType,
    val high: Int,
    val low: Int,
    val currentTemp: String?,
    val wind: String,
    val hours: List<HourUiModel>,
)

private data class HourDetailUiModel(
    val time: String,
    val temperature: Int,
    val weatherCode: Int,
    val precipProb: Int,
)

private data class DailyDetailUiModel(
    val date: LocalDate,
    val label: String,
    val isToday: Boolean,
    val weatherCode: Int,
    val weatherType: WeatherType,
    val condition: String,
    val high: Int,
    val low: Int,
    val appHigh: Int,
    val appLow: Int,
    val sunrise: String,
    val sunset: String,
    val uvMax: String,
    val precipSum: Double,
    val precipProb: Int,
    val windMax: Int,
    val windGust: Int,
    val windDir: Int,
)

private data class CurrentDetailUiModel(
    val feelsLike: String,
    val humidity: Int,
    val pressure: Double,
    val cloudCover: Int,
    val windGust: Int,
    val precipitation: Double,
    val visibility: Double,
    val dewPoint: Double,
)

private val DemoLocation = "Portland, OR"
private const val DemoCondition = "Partly Sunny"
private const val DemoTemperature = "72°"
private const val DemoHigh = "76°"
private const val DemoLow = "54°"
private const val DemoWind = "5 km/h NW"

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
    ForecastUiModel("FRI", WeatherType.Snow, 38, 29),
)

private val DemoDayPages = listOf(
    DayForecastUiModel(
        date = LocalDate.now(),
        label = "TODAY",
        isToday = true,
        condition = DemoCondition,
        weatherType = WeatherType.PartlyCloudy,
        high = 76,
        low = 54,
        currentTemp = "72°",
        wind = DemoWind,
        hours = listOf(
            HourUiModel("12:00", 72, 2),
            HourUiModel("13:00", 74, 2),
            HourUiModel("14:00", 75, 0),
            HourUiModel("15:00", 76, 0),
            HourUiModel("16:00", 74, 2),
            HourUiModel("17:00", 72, 2),
        ),
    ),
    DayForecastUiModel(
        date = LocalDate.now().plusDays(1),
        label = "SAT",
        isToday = false,
        condition = "Clear sky",
        weatherType = WeatherType.Clear,
        high = 78,
        low = 55,
        currentTemp = null,
        wind = "",
        hours = listOf(
            HourUiModel("08:00", 57, 0),
            HourUiModel("11:00", 66, 0),
            HourUiModel("14:00", 74, 0),
            HourUiModel("17:00", 78, 0),
            HourUiModel("20:00", 70, 0),
        ),
    ),
    DayForecastUiModel(
        date = LocalDate.now().plusDays(2),
        label = "SUN",
        isToday = false,
        condition = "Light rain",
        weatherType = WeatherType.Rain,
        high = 68,
        low = 50,
        currentTemp = null,
        wind = "",
        hours = listOf(
            HourUiModel("08:00", 52, 61),
            HourUiModel("11:00", 58, 61),
            HourUiModel("14:00", 64, 63),
            HourUiModel("17:00", 68, 63),
            HourUiModel("20:00", 60, 61),
        ),
    ),
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
    var dayPages by remember {
        mutableStateOf(if (isPreview) DemoDayPages else loadDayPagesUi(context))
    }
    var weatherNeedsRefresh by remember { mutableStateOf(false) }
    var currentDetail by remember { mutableStateOf(if (isPreview) null else loadCurrentDetail(context)) }
    var dailyDetails by remember { mutableStateOf(if (isPreview) emptyList<DailyDetailUiModel>() else loadDailyDetails(context)) }
    var hourlyDetails by remember { mutableStateOf(if (isPreview) emptyMap<LocalDate, List<HourDetailUiModel>>() else loadHourlyDetails(context)) }
    var weatherCode by remember { mutableStateOf(if (isPreview) 0 else Prefs.values(context).getInt(Prefs.WEATHER_CODE, -1)) }
    var gpsAutoTried by remember { mutableStateOf(false) }

    val gpsAutoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            doGpsLocation(context)
            weatherNeedsRefresh = true
            WeatherScheduler.refreshNow(context)
        }
    }

    if (!isPreview) {
        LaunchedEffect(Unit) {
            if (!gpsAutoTried) {
                gpsAutoTried = true
                val prefs = Prefs.values(context)
                if (prefs.getString(Prefs.LATITUDE, null) == null) {
                    val activity = context as? android.app.Activity
                    if (activity != null &&
                        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                        activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        gpsAutoLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    } else {
                        doGpsLocation(context)
                        weatherNeedsRefresh = true
                        WeatherScheduler.refreshNow(context)
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            delay(2_000)
            val activity = context as? android.app.Activity ?: return@LaunchedEffect
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
            }
        }
        LaunchedEffect(Unit) {
            delay(4_000)
            if (!Settings.canDrawOverlays(context)) {
                requestOverlayPermission(context)
            }
        }
        LaunchedEffect(Unit) {
            delay(6_000)
            val pm = context.getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                requestIgnoreBatteryOptimizations(context)
            }
        }
        LaunchedEffect(weatherNeedsRefresh) {
            if (weatherNeedsRefresh) {
                weatherNeedsRefresh = false
                delay(6_000)
                weather = loadWeatherUi(context)
                forecast = loadForecastUi(context)
                dayPages = loadDayPagesUi(context)
                currentDetail = loadCurrentDetail(context)
                dailyDetails = loadDailyDetails(context)
                hourlyDetails = loadHourlyDetails(context)
                weatherCode = Prefs.values(context).getInt(Prefs.WEATHER_CODE, -1)
            }
        }
        LaunchedEffect(Unit) {
            while (true) {
                delay(60_000)
                weather = loadWeatherUi(context)
                forecast = loadForecastUi(context)
                dayPages = loadDayPagesUi(context)
                currentDetail = loadCurrentDetail(context)
                dailyDetails = loadDailyDetails(context)
                hourlyDetails = loadHourlyDetails(context)
                weatherCode = Prefs.values(context).getInt(Prefs.WEATHER_CODE, -1)
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
                        onSettings = { selectedTab = InstructionTab.More },
                    )
                    when (selectedTab) {
                        InstructionTab.Clock -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                LargeClock(
                                    time = rememberClockText(isPreview),
                                    modifier = Modifier.fillMaxWidth(0.55f),
                                )
                            }
                            CurrentWeatherCard(
                                location = weather.location,
                                condition = weather.condition,
                                temperature = weather.temperature,
                                high = weather.high,
                                low = weather.low,
                                wind = weather.wind,
                                weatherCode = weatherCode,
                                isNight = isNightAt(
                                    dailyDetails.firstOrNull { it.isToday }?.sunrise ?: "",
                                    dailyDetails.firstOrNull { it.isToday }?.sunset ?: "",
                                ),
                                onClick = {
                                    weatherNeedsRefresh = true
                                    WeatherScheduler.refreshNow(context)
                                    toast(context, "Refreshing weather…")
                                },
                            )
                            Spacer(Modifier.height(8.dp))
                            DateLabel(
                                date = if (isPreview) {
                                    "Saturday, May 18, 2024"
                                } else {
                                    LocalDate.now().format(DAY_FORMAT)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(6.dp))
                            MonthCalendar()
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
                            WeatherDashboard(
                                weather = weather,
                                currentDetail = currentDetail,
                                dailyDetails = dailyDetails,
                                hourlyDetails = hourlyDetails,
                                weatherCode = weatherCode,
                                onRefresh = {
                                    weatherNeedsRefresh = true
                                    WeatherScheduler.refreshNow(context)
                                    toast(context, "Refreshing weather…")
                                },
                            )
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
                                onWeatherStateReload = {
                                    weather = loadWeatherUi(context)
                                    forecast = loadForecastUi(context)
                                    dayPages = loadDayPagesUi(context)
                                    currentDetail = loadCurrentDetail(context)
                                    dailyDetails = loadDailyDetails(context)
                                    hourlyDetails = loadHourlyDetails(context)
                                    weatherCode = Prefs.values(context).getInt(Prefs.WEATHER_CODE, -1)
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
                onTest = { alarm ->
                    AlarmScheduler.scheduleTest(context, alarm.soundUri, alarm.soundName)
                    toast(context, "Test alarm in 10 s")
                },
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

private fun formatInterval(minutes: Int): String {
    if (minutes < 60) return "$minutes min"
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (remainder == 0) "$hours h" else "$hours h $remainder min"
}

private fun yesNo(value: Boolean) = if (value) "Yes" else "No"

private fun permissionStatus(context: Context): String {
    val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    val notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
    val fullScreen = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
    val battery = context.getSystemService(PowerManager::class.java)
        .isIgnoringBatteryOptimizations(context.packageName)
    val overlay = Settings.canDrawOverlays(context)
    return "Exact alarms: ${yesNo(exact)} · Notifications: ${yesNo(notifications)}\n" +
        "Full-screen alerts: ${yesNo(fullScreen)} · Battery: ${yesNo(battery)} · Overlay: ${yesNo(overlay)}"
}

private fun openAlarmPermissions(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ensureExactAlarmPermission(context)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (!manager.canUseFullScreenIntent()) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:${context.packageName}"),
                ),
            )
        }
    }
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    if (context.getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName)
    ) {
        toast(context, "Battery optimization is already off")
        return
    }
    try {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}"),
            ),
        )
    } catch (_: Exception) {
        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }
}

private fun requestOverlayPermission(context: Context) {
    if (Settings.canDrawOverlays(context)) {
        toast(context, "Overlay permission already granted")
        return
    }
    try {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            ),
        )
    } catch (_: Exception) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
    }
}

private fun requestNotificationPermission(context: Context) {
    val activity = context as? Activity ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
    } else {
        toast(context, "Notifications permission already granted")
    }
}

private fun geocode(query: String, count: Int): List<JSONObject>? {
    val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
    val endpoint = URL(
        "https://geocoding-api.open-meteo.com/v1/search" +
            "?name=$encoded&count=$count&language=en&format=json",
    )
    val connection = endpoint.openConnection() as HttpURLConnection
    return try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("User-Agent", "BobrClockWeatherAlarm/1.0")
        if (connection.responseCode !in 200..299) return null
        val body = connection.inputStream.bufferedReader().use { it.readText() }
        val results = JSONObject(body).optJSONArray("results") ?: return null
        buildList {
            for (i in 0 until results.length()) {
                add(results.getJSONObject(i))
            }
        }
    } catch (_: Exception) {
        null
    } finally {
        connection.disconnect()
    }
}

private fun cityLabel(result: JSONObject): String {
    val name = result.getString("name")
    val country = result.optString("country")
    return if (country.isBlank()) name else getLocationWithCountry(name, country)
}

private fun getLocationWithCountry(name: String, country: String): String = "$name, $country"

private fun applyLocation(context: Context, result: JSONObject) {
    val lat = result.getDouble("latitude").toString()
    val lon = result.getDouble("longitude").toString()
    val postcode = result.optString("postcode").ifBlank { null }
    val cc = result.optString("country_code", null)
    val editor = Prefs.values(context).edit()
        .putString(Prefs.LOCATION_NAME, cityLabel(result))
        .putString(Prefs.POSTCODE, postcode ?: "")
        .putString(Prefs.LATITUDE, lat)
        .putString(Prefs.LONGITUDE, lon)
    if (cc != null) editor.putString(Prefs.COUNTRY_CODE, cc)
    editor.apply()
    toast(context, "Location saved")
}

private fun useGpsLocation(context: Context) {
    doGpsLocation(context)
}

private fun doGpsLocation(context: Context) {
    val coords = WeatherJobService.lastKnownLocation(context)
    if (coords == null) {
        toast(context, "Location not found. Enable GPS and try again.")
        return
    }
    val geo = try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val addresses = geocoder.getFromLocation(coords.first.toDouble(), coords.second.toDouble(), 1)
        addresses?.firstOrNull()
    } catch (_: Exception) { null }
    val city = geo?.let { it.locality ?: it.subAdminArea ?: it.adminArea ?: it.featureName }
        ?: fetchCityFromNominatim(coords.first.toDouble(), coords.second.toDouble())
    val cc = geo?.countryCode
    val label = city ?: "Your location"
    val editor = Prefs.values(context).edit()
        .putString(Prefs.LOCATION_NAME, label)
        .remove(Prefs.POSTCODE)
        .putString(Prefs.LATITUDE, coords.first)
        .putString(Prefs.LONGITUDE, coords.second)
    if (cc != null) editor.putString(Prefs.COUNTRY_CODE, cc)
    editor.apply()
    toast(context, "Weather location set to $label")
}

private fun fetchCityFromNominatim(lat: Double, lon: Double): String? {
    return try {
        val url = URL(
            "https://nominatim.openstreetmap.org/reverse" +
                "?format=json&lat=$lat&lon=$lon&zoom=10&language=en",
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("User-Agent", "BobrClockWeatherAlarm/1.0")
        if (conn.responseCode !in 200..299) return null
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        val json = JSONObject(body)
        val address = json.optJSONObject("address")
        address?.let {
            it.optString("city").ifBlank {
                it.optString("town").ifBlank {
                    it.optString("village").ifBlank {
                        it.optString("municipality").ifBlank {
                            it.optString("county")
                        }
                    }
                }
            }
        }?.takeIf { it.isNotBlank() }
    } catch (_: Exception) { null }
}

private fun displayName(context: Context, uri: Uri): String? {
    var cursor: Cursor? = null
    return try {
        cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        )
        if (cursor?.moveToFirst() == true) cursor.getString(0) else null
    } catch (_: Exception) {
        null
    } finally {
        cursor?.close()
    }
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
            "$wind ${Prefs.windUnitLabel(context)} ${windCompass(windDir)}"
        } else {
            "--"
        },
    )
}

private fun loadForecastUi(context: Context): List<ForecastUiModel> =
    loadDailyUi(context)
        .filterNot { it.date == java.time.LocalDate.now() }
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

private fun loadHourlyByDate(context: Context): Map<LocalDate, List<HourUiModel>> {
    val raw = Prefs.values(context).getString(Prefs.WEATHER_HOURLY_ALL, null) ?: return emptyMap()
    val result = HashMap<LocalDate, MutableList<HourUiModel>>()
    raw.split("|").forEach { entry ->
        val parts = entry.split(";")
        if (parts.size < 4) return@forEach
        try {
            val date = LocalDate.parse(parts[0])
            result.getOrPut(date) { mutableListOf() }
                .add(HourUiModel(parts[1], parts[2].toInt(), parts[3].toInt()))
        } catch (_: Exception) {
        }
    }
    return result
}

private fun loadDayPagesUi(context: Context): List<DayForecastUiModel> {
    val prefs = Prefs.values(context)
    val location = prefs.getString(Prefs.LOCATION_NAME, null)
        ?: context.getString(R.string.set_location)
    val currentTemp = prefs.getString(Prefs.WEATHER_TEMP, null)
    val wind = prefs.getInt(Prefs.WEATHER_WIND, -1)
    val windDir = prefs.getInt(Prefs.WEATHER_WIND_DIR, -1)
    val windText = if (wind >= 0 && windDir >= 0) {
        "$wind ${Prefs.windUnitLabel(context)} ${windCompass(windDir)}"
    } else {
        "--"
    }
    val today = LocalDate.now()
    val hourlyByDate = loadHourlyByDate(context)
    val currentCode = prefs.getInt(Prefs.WEATHER_CODE, -1)
    return loadDailyUi(context).map { daily ->
        val isToday = daily.date == today
        val useCurrent = isToday && currentCode >= 0
        DayForecastUiModel(
            date = daily.date,
            label = if (isToday) {
                "TODAY"
            } else {
                daily.date.format(DAY_SHORT_FORMAT).uppercase(Locale.getDefault())
            },
            isToday = isToday,
            condition = if (useCurrent) {
                weatherConditionText(currentCode)
            } else {
                weatherConditionText(daily.weatherCode)
            },
            weatherType = if (useCurrent) {
                weatherType(currentCode)
            } else {
                weatherType(daily.weatherCode)
            },
            high = daily.high,
            low = daily.low,
            currentTemp = if (isToday) currentTemp?.let { "$it°" } else null,
            wind = if (isToday) windText else "",
            hours = hourlyByDate[daily.date] ?: emptyList(),
        )
    }
}

private fun loadCurrentDetail(context: Context): CurrentDetailUiModel {
    val prefs = Prefs.values(context)
    return CurrentDetailUiModel(
        feelsLike = prefs.getString(Prefs.WEATHER_FEELS_LIKE, null)?.let { "${it}°" } ?: "--°",
        humidity = prefs.getString(Prefs.WEATHER_HUMIDITY, null)?.toIntOrNull() ?: -1,
        pressure = prefs.getString(Prefs.WEATHER_PRESSURE, null)?.toDoubleOrNull() ?: -1.0,
        cloudCover = prefs.getString(Prefs.WEATHER_CLOUD_COVER, null)?.toIntOrNull() ?: -1,
        windGust = prefs.getString(Prefs.WEATHER_WIND_GUST, null)?.toIntOrNull() ?: -1,
        precipitation = prefs.getString(Prefs.WEATHER_PRECIPITATION, null)?.toDoubleOrNull() ?: -1.0,
        visibility = prefs.getString(Prefs.WEATHER_VISIBILITY, null)?.toDoubleOrNull() ?: -1.0,
        dewPoint = prefs.getString(Prefs.WEATHER_DEW_POINT, null)?.toDoubleOrNull() ?: -999.0,
    )
}

private fun loadDailyDetails(context: Context): List<DailyDetailUiModel> {
    val raw = Prefs.values(context).getString(Prefs.WEATHER_DAILY, null) ?: return emptyList()
    val today = LocalDate.now()
    return raw.split("|").mapNotNull { entry ->
        val parts = entry.split(";")
        if (parts.size < 4) return@mapNotNull null
        try {
            val date = LocalDate.parse(parts[0])
            DailyDetailUiModel(
                date = date,
                label = if (date == today) "TODAY" else date.format(DateTimeFormatter.ofPattern("EEE", java.util.Locale.getDefault())).uppercase(Locale.getDefault()),
                isToday = date == today,
                weatherCode = parts[1].toInt(),
                weatherType = weatherType(parts[1].toInt()),
                condition = weatherConditionText(parts[1].toInt()),
                high = parts[2].toInt(),
                low = parts[3].toInt(),
                appHigh = parts.getOrNull(4)?.toIntOrNull() ?: parts[2].toInt(),
                appLow = parts.getOrNull(5)?.toIntOrNull() ?: parts[3].toInt(),
                sunrise = parts.getOrNull(6)?.takeIf { it.isNotBlank() }?.let { s -> try { java.time.LocalDateTime.parse(s).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { "" } } ?: "",
                sunset = parts.getOrNull(7)?.takeIf { it.isNotBlank() }?.let { s -> try { java.time.LocalDateTime.parse(s).format(DateTimeFormatter.ofPattern("HH:mm")) } catch (_: Exception) { "" } } ?: "",
                uvMax = parts.getOrNull(8)?.takeIf { it.isNotBlank() } ?: "",
                precipSum = parts.getOrNull(9)?.toDoubleOrNull() ?: 0.0,
                precipProb = parts.getOrNull(10)?.toIntOrNull() ?: -1,
                windMax = parts.getOrNull(11)?.toDoubleOrNull()?.toInt() ?: -1,
                windGust = parts.getOrNull(12)?.toDoubleOrNull()?.toInt() ?: -1,
                windDir = parts.getOrNull(13)?.toIntOrNull() ?: -1,
            )
        } catch (_: Exception) { null }
    }
}

private fun loadHourlyDetails(context: Context): Map<LocalDate, List<HourDetailUiModel>> {
    val raw = Prefs.values(context).getString(Prefs.WEATHER_HOURLY_ALL, null) ?: return emptyMap()
    val result = HashMap<LocalDate, MutableList<HourDetailUiModel>>()
    raw.split("|").forEach { entry ->
        val parts = entry.split(";")
        if (parts.size < 5) return@forEach
        try {
            val date = LocalDate.parse(parts[0])
            result.getOrPut(date) { mutableListOf() }.add(
                HourDetailUiModel(
                    time = parts[1],
                    temperature = parts[2].toInt(),
                    weatherCode = parts[3].toInt(),
                    precipProb = parts[4].toIntOrNull() ?: -1,
                )
            )
        } catch (_: Exception) {}
    }
    return result
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

@Composable
private fun MonthCalendar() {
    val context = LocalContext.current
    val locale = java.util.Locale.getDefault()
    val firstDayOfWeek = java.time.temporal.WeekFields.of(locale).firstDayOfWeek
    val today = if (LocalInspectionMode.current) LocalDate.of(2024, 5, 18) else LocalDate.now()
    val baseYear = 1990
    val initialPage = (today.year - baseYear) * 12 + today.monthValue - 1
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1200 })
    val scope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val displayedYear = currentPage / 12 + baseYear
    val displayedMonth = (currentPage % 12) + 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaperCard.copy(alpha = 0.85f))
            .handDrawnBorder(cornerRadius = 20.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "‹", fontSize = 22.sp, color = OliveDark, fontFamily = FontFamily.Cursive)
            }
            val monthTitle = try {
                LocalDate.of(displayedYear, displayedMonth, 1)
                    .format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", locale))
                    .replaceFirstChar { it.uppercase() }
            } catch (_: Exception) { "" }
            Text(
                text = monthTitle,
                fontSize = 18.sp,
                color = OliveDark,
                fontFamily = FontFamily.Cursive,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "›", fontSize = 22.sp, color = OliveDark, fontFamily = FontFamily.Cursive)
            }
        }
        Spacer(Modifier.height(2.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val year = page / 12 + baseYear
            val month = (page % 12) + 1
            val daysInMonth = try { YearMonth.of(year, month).lengthOfMonth() } catch (_: Exception) { 0 }
            if (daysInMonth == 0) return@HorizontalPager
            val monthStart = LocalDate.of(year, month, 1)
            val firstOfMonth = monthStart.withDayOfMonth(1)
            val startDow = (firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
            val isCurrentMonth = (year == today.year && month == today.monthValue)

            val dayNames = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
                .let { all -> all.subList(firstDayOfWeek.value - 1, 7) + all.subList(0, firstDayOfWeek.value - 1) }
            val weekendIndices = listOf(java.time.DayOfWeek.SATURDAY, java.time.DayOfWeek.SUNDAY)
                .map { (it.value - firstDayOfWeek.value + 7) % 7 }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    dayNames.forEachIndexed { idx, name ->
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            color = if (weekendIndices.contains(idx)) Color(0xFFC0392B) else TextMuted,
                            fontFamily = FontFamily.Cursive,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                val totalCells = startDow + daysInMonth
                val weeks = (totalCells + 6) / 7
                var dayCounter = 1
                repeat(weeks) { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        repeat(7) { col ->
                            val cellIndex = week * 7 + col
                            if (cellIndex < startDow || dayCounter > daysInMonth) {
                                Spacer(Modifier.weight(1f).padding(3.dp))
                            } else {
                                val date = LocalDate.of(year, month, dayCounter)
                                val isToday = isCurrentMonth && dayCounter == today.dayOfMonth
                                val isWeekend = weekendIndices.contains(col)
                                val isHoliday = isCzechHoliday(date)
                                Text(
                                    text = "$dayCounter",
                                    fontSize = 14.sp,
                                    color = when {
                                        isToday -> PaperBackground
                                        isWeekend || isHoliday -> Color(0xFFC0392B)
                                        else -> TextBrown
                                    },
                                    fontFamily = FontFamily.Cursive,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(2.dp)
                                        .then(
                                            if (isToday)
                                                Modifier.clip(CircleShape).background(OlivePrimary)
                                            else
                                                Modifier,
                                        )
                                        .padding(3.dp),
                                )
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isCzechHoliday(date: LocalDate): Boolean {
    val (month, day) = date.monthValue to date.dayOfMonth
    return when ("$month-$day") {
        "1-1", "5-1", "5-8", "7-5", "7-6", "9-28", "10-28", "11-17", "12-24", "12-25", "12-26" -> true
        else -> {
            val easter = computeEasterSunday(date.year)
            val goodFriday = easter.minusDays(2)
            val easterMonday = easter.plusDays(1)
            date == goodFriday || date == easterMonday
        }
    }
}

private fun computeEasterSunday(year: Int): LocalDate {
    val a = year % 19
    val b = year / 100
    val c = year % 100
    val d = b / 4
    val e = b % 4
    val f = (b + 8) / 25
    val g = (b - f + 1) / 3
    val h = (19 * a + b - d - g + 15) % 30
    val i = c / 4
    val k = c % 4
    val l = (32 + 2 * e + 2 * i - h - k) % 7
    val m = (a + 11 * h + 22 * l) / 451
    val month = (h + l - 7 * m + 114) / 31
    val day = ((h + l - 7 * m + 114) % 31) + 1
    return LocalDate.of(year, month, day)
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
    weatherCode: Int,
    isNight: Boolean,
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
            DynamicWeatherArtwork(weatherCode = weatherCode, isNight = isNight, modifier = Modifier.size(120.dp, 105.dp))
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
// Weather day pager
// ---------------------------------------------------------------------------

@Composable
private fun WeatherDayPager(
    pages: List<DayForecastUiModel>,
    location: String,
    onRefresh: () -> Unit,
) {
    if (pages.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PaperCard.copy(alpha = 0.85f))
                .handDrawnBorder(cornerRadius = 24.dp)
                .olivePress(onRefresh)
                .padding(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No weather data yet — tap to refresh",
                fontSize = 18.sp,
                color = TextMuted,
                fontFamily = FontFamily.Cursive,
            )
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            DayWeatherPage(
                page = pages[page],
                location = location,
                onRefresh = onRefresh,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            pages.forEachIndexed { index, _ ->
                val active = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (active) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(if (active) OlivePrimary else DividerBeige),
                )
            }
        }
    }
}

@Composable
private fun DayWeatherPage(
    page: DayForecastUiModel,
    location: String,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = page.label,
            fontSize = 22.sp,
            color = OliveDark,
            fontFamily = FontFamily.Cursive,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp, bottom = 6.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(PaperCard.copy(alpha = 0.85f))
                .handDrawnBorder(cornerRadius = 24.dp)
                .olivePress(onRefresh)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(0.44f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                if (page.isToday) {
                    SunWithClouds(Modifier.size(120.dp, 105.dp))
                } else {
                    WeatherIcon(page.weatherType, Modifier.size(84.dp))
                }
            }
            Column(
                modifier = Modifier
                    .weight(0.56f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (page.isToday) {
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
                        text = page.condition,
                        fontSize = 18.sp,
                        color = TextBrown,
                        fontFamily = FontFamily.Cursive,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = page.currentTemp ?: "${page.high}°",
                            fontSize = 58.sp,
                            color = OlivePrimary,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "H: ${page.high}°",
                                fontSize = 17.sp,
                                color = WarmOrange,
                                fontFamily = FontFamily.Cursive,
                            )
                            Text(
                                text = "L: ${page.low}°",
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
                            text = page.wind,
                            fontSize = 15.sp,
                            color = OliveMuted,
                            fontFamily = FontFamily.Cursive,
                        )
                    }
                } else {
                    Text(
                        text = page.condition,
                        fontSize = 22.sp,
                        color = TextBrown,
                        fontFamily = FontFamily.Cursive,
                        maxLines = 2,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${page.high}°",
                            fontSize = 58.sp,
                            color = OlivePrimary,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "H: ${page.high}°",
                                fontSize = 17.sp,
                                color = WarmOrange,
                                fontFamily = FontFamily.Cursive,
                            )
                            Text(
                                text = "L: ${page.low}°",
                                fontSize = 17.sp,
                                color = RainBlue,
                                fontFamily = FontFamily.Cursive,
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        HourlyRow(hours = page.hours)
    }
}

@Composable
private fun HourlyRow(hours: List<HourUiModel>) {
    if (hours.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PaperCard)
            .handDrawnBorder(cornerRadius = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
        ) {
            items(hours) { hour ->
                Column(
                    modifier = Modifier.width(62.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = hour.time,
                        fontSize = 14.sp,
                        color = OliveMuted,
                        fontFamily = FontFamily.Cursive,
                    )
                    WeatherIcon(weatherType(hour.weatherCode), Modifier.size(32.dp))
                    Text(
                        text = "${hour.temperature}°",
                        fontSize = 16.sp,
                        color = TextBrown,
                        fontFamily = FontFamily.Cursive,
                    )
                }
            }
        }
    }
}


// ---------------------------------------------------------------------------
// Weather dashboard
// ---------------------------------------------------------------------------

@Composable
private fun WeatherDashboard(
    weather: WeatherUiModel,
    currentDetail: CurrentDetailUiModel?,
    dailyDetails: List<DailyDetailUiModel>,
    hourlyDetails: Map<LocalDate, List<HourDetailUiModel>>,
    weatherCode: Int,
    onRefresh: () -> Unit,
) {
    val today = LocalDate.now()
    val todayDetail = dailyDetails.firstOrNull { it.date == today }
    val days = dailyDetails.sortedBy { it.date }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { days.size })
    val hourStates = remember { mutableMapOf<LocalDate, LazyListState>() }
    var selectedDay by remember { mutableStateOf<DailyDetailUiModel?>(null) }

    val isNight = isNightAt(todayDetail?.sunrise ?: "", todayDetail?.sunset ?: "")

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            days.getOrNull(page)?.let { selectedDay = it }
        }
    }

    CurrentWeatherCardEnhanced(
        location = weather.location,
        condition = weather.condition,
        temperature = weather.temperature,
        high = weather.high,
        low = weather.low,
        wind = weather.wind,
        feelsLike = currentDetail?.feelsLike ?: "--°",
        precipProb = todayDetail?.precipProb ?: -1,
        weatherCode = weatherCode,
        isNight = isNight,
        onClick = onRefresh,
    )
    Spacer(Modifier.height(8.dp))
    HourlyDayPager(
        days = days,
        hourlyDetails = hourlyDetails,
        pagerState = pagerState,
        hourStates = hourStates,
    )
    Spacer(Modifier.height(10.dp))
    DailyForecastSection(
        dailyDetails = dailyDetails,
        selectedDay = selectedDay,
        onSelectDay = { day ->
            val newSel = if (selectedDay == day) null else day
            selectedDay = newSel
            if (newSel != null) {
                val idx = days.indexOfFirst { it.date == newSel.date }
                if (idx >= 0) scope.launch { pagerState.animateScrollToPage(idx) }
            }
        },
    )
    Spacer(Modifier.height(10.dp))
    WeatherDetailsGrid(
        currentDetail = currentDetail,
        todayDetail = todayDetail,
        weather = weather,
    )
}

private fun initialHourIndex(hours: List<HourDetailUiModel>, isToday: Boolean): Int {
    if (hours.isEmpty()) return 0
    if (!isToday) return 0
    val nowHour = LocalTime.now().hour
    val idx = hours.indexOfFirst {
        try { LocalTime.parse(it.time).hour >= nowHour } catch (_: Exception) { false }
    }
    return if (idx < 0) 0 else idx
}

private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "TODAY"
        today.plusDays(1) -> "TOMORROW"
        else -> date.format(DateTimeFormatter.ofPattern("EEE", java.util.Locale.getDefault())).uppercase(Locale.getDefault())
    }
}

private fun isNightAt(sunrise: String, sunset: String): Boolean {
    val sr = sunrise.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    val ss = sunset.takeIf { it.isNotBlank() }?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
    if (sr == null || ss == null) return false
    val now = LocalTime.now()
    return now.isBefore(sr) || !now.isBefore(ss)
}

@Composable
private fun CurrentWeatherCardEnhanced(
    location: String,
    condition: String,
    temperature: String,
    high: String,
    low: String,
    wind: String,
    feelsLike: String,
    precipProb: Int,
    weatherCode: Int,
    isNight: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PaperCard.copy(alpha = 0.85f))
            .handDrawnBorder(cornerRadius = 24.dp)
            .olivePress(onClick)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(130.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(0.42f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                DynamicWeatherArtwork(weatherCode = weatherCode, isNight = isNight, modifier = Modifier.size(120.dp, 105.dp))
            }
            Column(
                Modifier.weight(0.58f).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LocationPin(Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(location, fontSize = 17.sp, color = OliveDark, fontFamily = FontFamily.Cursive, maxLines = 1)
                }
                Text(condition, fontSize = 18.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(temperature, fontSize = 58.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Normal, maxLines = 1)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("H: $high", fontSize = 17.sp, color = WarmOrange, fontFamily = FontFamily.Cursive)
                        Text("L: $low", fontSize = 17.sp, color = RainBlue, fontFamily = FontFamily.Cursive)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WindIcon(Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(wind, fontSize = 15.sp, color = OliveMuted, fontFamily = FontFamily.Cursive)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Feels like", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                Text(feelsLike, fontSize = 18.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
            }
            Box(Modifier.width(1.dp).fillMaxHeight().background(DividerBeige))
            if (precipProb >= 0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rain", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                    Text("$precipProb%", fontSize = 18.sp, color = RainBlue, fontFamily = FontFamily.Cursive)
                }
                Box(Modifier.width(1.dp).fillMaxHeight().background(DividerBeige))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Wind", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                Text(wind, fontSize = 18.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
            }
        }
    }
}

@Composable
private fun HourlyDayPager(
    days: List<DailyDetailUiModel>,
    hourlyDetails: Map<LocalDate, List<HourDetailUiModel>>,
    pagerState: PagerState,
    hourStates: MutableMap<LocalDate, LazyListState>,
) {
    if (days.isEmpty()) return
    Column {
        Text(
            "HOURLY FORECAST",
            fontSize = 18.sp,
            color = OliveDark,
            fontFamily = FontFamily.Cursive,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            beyondViewportPageCount = 1,
        ) { page ->
            val day = days.getOrNull(page) ?: return@HorizontalPager
            HourlyDayStrip(
                day = day,
                hours = hourlyDetails[day.date] ?: emptyList(),
                state = hourStates.getOrPut(day.date) {
                    LazyListState(initialHourIndex(hourlyDetails[day.date] ?: emptyList(), day.isToday))
                },
            )
        }
    }
}

@Composable
private fun HourlyDayStrip(
    day: DailyDetailUiModel,
    hours: List<HourDetailUiModel>,
    state: LazyListState,
) {
    val now = LocalTime.now()
    val snapFling = rememberSnapFlingBehavior(lazyListState = state)
    Column {
        Text(
            day.label,
            fontSize = 12.sp,
            color = OliveMuted,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PaperCard)
                .handDrawnBorder(cornerRadius = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LazyRow(
                state = state,
                flingBehavior = snapFling,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(hours) { hour ->
                    val isNow = try {
                        val ht = LocalTime.parse(hour.time)
                        !ht.isAfter(now) && ht.isAfter(now.minusHours(1))
                    } catch (_: Exception) { false }
                    Column(
                        modifier = Modifier.width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            if (isNow && day.isToday) "Now" else hour.time,
                            fontSize = 13.sp,
                            color = if (isNow && day.isToday) OlivePrimary else OliveMuted,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = if (isNow && day.isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                        WeatherIcon(weatherType(hour.weatherCode), Modifier.size(30.dp))
                        Text("${hour.temperature}°", fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                        if (hour.precipProb > 0) {
                            Text("${hour.precipProb}%", fontSize = 11.sp, color = RainBlue, fontFamily = FontFamily.Cursive)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastSection(
    dailyDetails: List<DailyDetailUiModel>,
    selectedDay: DailyDetailUiModel?,
    onSelectDay: (DailyDetailUiModel?) -> Unit,
) {
    Column {
        Text(
            "DAILY FORECAST",
            fontSize = 18.sp,
            color = OliveDark,
            fontFamily = FontFamily.Cursive,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        val allHighs = dailyDetails.map { it.high }
        val allLows = dailyDetails.map { it.low }
        val globalMin = allLows.minOrNull() ?: 0
        val globalMax = allHighs.maxOrNull() ?: 1
        val range = (globalMax - globalMin).coerceAtLeast(1)
        dailyDetails.forEach { day ->
            DailyForecastRow(
                day = day,
                globalMin = globalMin,
                globalMax = globalMax,
                range = range,
                isExpanded = selectedDay == day,
                onTap = { onSelectDay(if (selectedDay == day) null else day) },
            )
            if (selectedDay == day) {
                DailyDetailExpanded(day = day)
            }
        }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyDetailUiModel,
    globalMin: Int,
    globalMax: Int,
    range: Int,
    isExpanded: Boolean,
    onTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (isExpanded) Modifier.background(PaperSelected.copy(alpha = 0.4f)) else Modifier)
            .olivePress(onTap)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(52.dp)) {
            Text(day.label, fontSize = 15.sp, color = if (day.isToday) OlivePrimary else OliveDark, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
            Text(day.date.format(DateTimeFormatter.ofPattern("d MMM")), fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
        }
        WeatherIcon(day.weatherType, Modifier.size(28.dp))
        Spacer(Modifier.width(6.dp))
        Text(day.condition, fontSize = 13.sp, color = TextBrown, fontFamily = FontFamily.Cursive, modifier = Modifier.weight(1f), maxLines = 1)
        if (day.precipProb >= 0) {
            Text("${day.precipProb}%", fontSize = 12.sp, color = RainBlue, fontFamily = FontFamily.Cursive, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
        } else {
            Spacer(Modifier.width(36.dp))
        }
        Text("${day.low}°", fontSize = 14.sp, color = RainBlue, fontFamily = FontFamily.Cursive, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DividerBeige),
        ) {
            val leftFrac = ((day.low - globalMin).toFloat() / range).coerceIn(0f, 1f)
            val rightFrac = ((day.high - globalMin).toFloat() / range).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = (56.dp * leftFrac), end = (56.dp * (1f - rightFrac)))
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (day.isToday) OlivePrimary else CoralCheek),
            )
        }
        Text("${day.high}°", fontSize = 14.sp, color = WarmOrange, fontFamily = FontFamily.Cursive, modifier = Modifier.width(32.dp), textAlign = TextAlign.Start)
    }
}

@Composable
private fun DailyDetailExpanded(day: DailyDetailUiModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val unit = Prefs.windUnitLabel(context)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard.copy(alpha = 0.7f))
            .handDrawnBorder(cornerRadius = 14.dp)
            .padding(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            if (day.sunrise.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sunrise", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                    Text(day.sunrise, fontSize = 15.sp, color = SunOrange, fontFamily = FontFamily.Cursive)
                }
            }
            if (day.sunset.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sunset", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                    Text(day.sunset, fontSize = 15.sp, color = MustardYellow, fontFamily = FontFamily.Cursive)
                }
            }
            if (day.uvMax.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UV max", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                    Text(day.uvMax, fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                }
            }
        }
        if (day.windMax > 0 || day.windGust > 0) {
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                if (day.windMax > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wind max", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                        Text("${day.windMax} $unit", fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                    }
                }
                if (day.windGust > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Gusts", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                        Text("${day.windGust} $unit", fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                    }
                }
                if (day.precipSum > 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rain", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                        Text("${String.format(java.util.Locale.US, "%.1f", day.precipSum)} mm", fontSize = 15.sp, color = RainBlue, fontFamily = FontFamily.Cursive)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailsGrid(
    currentDetail: CurrentDetailUiModel?,
    todayDetail: DailyDetailUiModel?,
    weather: WeatherUiModel,
) {
    Text(
        "DETAILS",
        fontSize = 18.sp,
        color = OliveDark,
        fontFamily = FontFamily.Cursive,
        modifier = Modifier.padding(bottom = 6.dp),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UvDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail, todayDetail = todayDetail)
            WindDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail, weather = weather)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HumidityDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
            SunriseDetailCard(modifier = Modifier.weight(1f), todayDetail = todayDetail)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeelsLikeDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail, weather = weather)
            VisibilityDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PressureDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
            CloudCoverDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
        }
        if (currentDetail != null && currentDetail.precipitation > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrecipitationDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
                if (currentDetail.dewPoint > -900) {
                    DewPointDetailCard(modifier = Modifier.weight(1f), currentDetail = currentDetail)
                } else {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DetailCardBase(modifier: Modifier = Modifier, title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(PaperCard.copy(alpha = 0.7f))
            .handDrawnBorder(cornerRadius = 14.dp)
            .padding(12.dp),
    ) {
        Text(title, fontSize = 13.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun UvDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?, todayDetail: DailyDetailUiModel?) {
    val uvStr = todayDetail?.uvMax?.takeIf { it.isNotBlank() }
    val uv = uvStr?.toDoubleOrNull() ?: -1.0
    DetailCardBase(modifier = modifier, title = "UV INDEX") {
        if (uvStr == null || uv < 0) {
            Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
            return@DetailCardBase
        }
        Text(uvStr, fontSize = 30.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        val (category, advice) = uvCategory(uv)
        Text(category, fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
        Spacer(Modifier.height(4.dp))
        Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DividerBeige)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth((uv / 11.0).toFloat().coerceIn(0f, 1f)).clip(RoundedCornerShape(2.dp)).background(OlivePrimary))
        }
        Spacer(Modifier.height(4.dp))
        Text(advice, fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive, maxLines = 2)
    }
}

private fun uvCategory(uv: Double): Pair<String, String> = when {
    uv <= 2 -> "Low" to "No protection needed"
    uv <= 5 -> "Moderate" to "Wear sunscreen"
    uv <= 7 -> "High" to "Use SPF 30+, seek shade"
    uv <= 10 -> "Very high" to "Avoid midday sun"
    else -> "Extreme" to "Stay indoors midday"
}

@Composable
private fun WindDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?, weather: WeatherUiModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val unit = Prefs.windUnitLabel(context)
    val wind = weather.wind
    val gust = currentDetail?.windGust?.takeIf { it > 0 }
    DetailCardBase(modifier = modifier, title = "WIND") {
        Text(wind, fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        if (gust != null) {
            Text("Gusts $gust $unit", fontSize = 13.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
        }
        Spacer(Modifier.height(4.dp))
        Canvas(Modifier.size(36.dp)) {
            drawCircle(OliveMuted, 14.dp.toPx(), center, style = Stroke(1.dp.toPx()))
            drawLine(OlivePrimary, center, Offset(center.x, center.y + 10.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
    }
}

@Composable
private fun HumidityDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?) {
    val h = currentDetail?.humidity?.takeIf { it >= 0 }
    DetailCardBase(modifier = modifier, title = "HUMIDITY") {
        if (h == null) { Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive); return@DetailCardBase }
        Text("$h%", fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        val label = when { h < 30 -> "Dry"; h < 55 -> "Comfortable"; h < 75 -> "Humid"; else -> "Very humid" }
        Text(label, fontSize = 13.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
        if (currentDetail.dewPoint > -900) {
            Text("Dew point ${currentDetail.dewPoint.toInt()}°", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
        }
    }
}

@Composable
private fun SunriseDetailCard(modifier: Modifier, todayDetail: DailyDetailUiModel?) {
    val sunrise = todayDetail?.sunrise?.takeIf { it.isNotBlank() }
    val sunset = todayDetail?.sunset?.takeIf { it.isNotBlank() }
    DetailCardBase(modifier = modifier, title = "SUNRISE & SUNSET") {
        if (sunrise == null || sunset == null) { Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive); return@DetailCardBase }
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sunrise", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                Text(sunrise, fontSize = 18.sp, color = SunOrange, fontFamily = FontFamily.Cursive)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sunset", fontSize = 11.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                Text(sunset, fontSize = 18.sp, color = MustardYellow, fontFamily = FontFamily.Cursive)
            }
        }
    }
}

@Composable
private fun FeelsLikeDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?, weather: WeatherUiModel) {
    DetailCardBase(modifier = modifier, title = "FEELS LIKE") {
        val f = currentDetail?.feelsLike ?: weather.temperature
        Text(f, fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        Text("Actual ${weather.temperature}", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
    }
}

@Composable
private fun VisibilityDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?) {
    val v = currentDetail?.visibility?.takeIf { it > 0 }
    DetailCardBase(modifier = modifier, title = "VISIBILITY") {
        if (v == null) { Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive); return@DetailCardBase }
        val km = v / 1000.0
        Text("${String.format(java.util.Locale.US, "%.1f", km)} km", fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        val label = when { km >= 10 -> "Excellent"; km >= 5 -> "Good"; km >= 2 -> "Moderate"; else -> "Poor" }
        Text(label, fontSize = 13.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
    }
}

@Composable
private fun PressureDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?) {
    val p = currentDetail?.pressure?.takeIf { it > 0 }
    DetailCardBase(modifier = modifier, title = "PRESSURE") {
        if (p == null) { Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive); return@DetailCardBase }
        Text("${p.toInt()} hPa", fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CloudCoverDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel?) {
    val c = currentDetail?.cloudCover?.takeIf { it >= 0 }
    DetailCardBase(modifier = modifier, title = "CLOUD COVER") {
        if (c == null) { Text("No data", fontSize = 15.sp, color = TextMuted, fontFamily = FontFamily.Cursive); return@DetailCardBase }
        Text("$c%", fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
        val label = when { c <= 10 -> "Clear"; c <= 40 -> "Partly cloudy"; c <= 80 -> "Mostly cloudy"; else -> "Overcast" }
        Text(label, fontSize = 13.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
    }
}

@Composable
private fun PrecipitationDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel) {
    DetailCardBase(modifier = modifier, title = "PRECIPITATION") {
        Text("${String.format(java.util.Locale.US, "%.1f", currentDetail.precipitation)} mm", fontSize = 26.sp, color = RainBlue, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DewPointDetailCard(modifier: Modifier, currentDetail: CurrentDetailUiModel) {
    DetailCardBase(modifier = modifier, title = "DEW POINT") {
        Text("${currentDetail.dewPoint.toInt()}°", fontSize = 26.sp, color = OlivePrimary, fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// Five-day forecast card
// ---------------------------------------------------------------------------

@Composable
private fun ForecastCard(forecast: List<ForecastUiModel>) {
    if (forecast.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(106.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(PaperCard)
            .handDrawnBorder(cornerRadius = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 6.dp),
        ) {
            itemsIndexed(forecast) { index, day ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .padding(vertical = 12.dp)
                                .background(DividerBeige),
                        )
                    }
                    ForecastDayCell(day)
                }
            }
        }
    }
}

@Composable
private fun ForecastDayCell(day: ForecastUiModel) {
    Column(
        modifier = Modifier.width(70.dp),
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
// Current weather illustration (dynamic, based on real conditions)
// ---------------------------------------------------------------------------

@Composable
private fun DynamicWeatherArtwork(
    weatherCode: Int,
    isNight: Boolean,
    modifier: Modifier = Modifier,
) {
    val type = weatherType(weatherCode)
    val transition = rememberInfiniteTransition(label = "weather")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    val rainPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "rain",
    )
    val snowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing), RepeatMode.Restart),
        label = "snow",
    )
    val flashPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750, easing = LinearEasing), RepeatMode.Restart),
        label = "flash",
    )

    Canvas(modifier) {
        when (type) {
            WeatherType.Clear -> if (isNight) drawClearNightArt(pulse) else drawClearDayArt(pulse)
            WeatherType.PartlyCloudy -> if (isNight) drawNightCloudArt() else drawPartlyCloudyDayArt(pulse)
            WeatherType.Cloudy -> drawCloudyArt()
            WeatherType.Fog -> drawFogArt()
            WeatherType.Drizzle -> drawRainArt(rainPhase, drizzle = true)
            WeatherType.Rain -> drawRainArt(rainPhase, drizzle = false)
            WeatherType.Snow -> drawSnowArt(snowPhase)
            WeatherType.Thunderstorm -> drawStormArt(flashPhase)
        }
    }
}

private fun DrawScope.drawSunRays(c: Offset, r: Float, phase: Float) {
    val ext = 0.1f + phase * 0.12f
    repeat(12) { i ->
        val a = (i * 30f + 3f) * (Math.PI / 180.0)
        val cx = cos(a).toFloat()
        val sy = sin(a).toFloat()
        val inner = r * 1.1f
        val outer = r * (1.38f + ext)
        drawLine(
            SunOrange,
            Offset(c.x + cx * inner, c.y + sy * inner),
            Offset(c.x + cx * outer, c.y + sy * outer),
            r * 0.13f,
            StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawClearDayArt(pulse: Float) {
    val bob = sin(pulse * Math.PI.toFloat() * 2f) * size.height * 0.025f
    val sunC = Offset(size.width * 0.5f, size.height * 0.4f + bob)
    val sunR = size.width * 0.3f
    drawSunRays(sunC, sunR, pulse)
    drawSunBody(sunC, sunR, rays = false)
    drawFace(sunC.x, sunC.y + sunR * 0.05f, sunR * 0.55f, EyeStyle.Open, MouthStyle.Smile, cheeks = true)
}

private fun DrawScope.drawPartlyCloudyDayArt(pulse: Float) {
    val sunC = Offset(size.width * 0.38f, size.height * 0.32f)
    val sunR = size.width * 0.24f
    drawSunRays(sunC, sunR, pulse)
    drawSunBody(sunC, sunR, rays = false)
    drawFace(sunC.x, sunC.y + sunR * 0.05f, sunR * 0.55f, EyeStyle.Open, MouthStyle.Smile, cheeks = true)
    drawCloud(size.width * 0.58f, size.height * 0.6f, size.width * 0.6f, size.height * 0.34f, 1.6.dp.toPx())
}

private fun DrawScope.drawCloudyArt() {
    val cloudC = Offset(size.width * 0.5f, size.height * 0.5f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.85f, size.height * 0.42f, 1.6.dp.toPx())
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.15f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
}

private fun DrawScope.drawFogArt() {
    val cloudC = Offset(size.width * 0.5f, size.height * 0.45f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.8f, size.height * 0.36f, 1.5.dp.toPx())
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.14f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
    val sw = 1.4.dp.toPx()
    listOf(0.14f, 0.2f, 0.12f).forEachIndexed { i, len ->
        val y = size.height * (0.78f + i * 0.06f)
        val path = Path().apply {
            moveTo(size.width * 0.16f, y)
            lineTo(size.width * (0.16f + len * 0.8f), y)
        }
        drawPath(path, CloudBlue, style = Stroke(sw, cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawRainArt(phase: Float, drizzle: Boolean) {
    val cloudC = Offset(size.width * 0.5f, size.height * 0.42f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.78f, size.height * 0.34f, 1.5.dp.toPx())
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.14f, EyeStyle.HalfClosed, MouthStyle.Frown, cheeks = false)
    val dropR = size.width * (if (drizzle) 0.025f else 0.04f)
    val topY = size.height * 0.6f
    val fall = size.height * 0.3f
    val xs = if (drizzle)
        listOf(0.3f, 0.4f, 0.5f, 0.6f, 0.7f)
    else
        listOf(0.26f, 0.34f, 0.42f, 0.5f, 0.58f, 0.66f, 0.74f)
    xs.forEachIndexed { i, fx ->
        val p = (phase + i * 0.13f) % 1f
        drawCircle(RainBlue, dropR, Offset(size.width * fx, topY + p * fall))
    }
}

private fun DrawScope.drawSnowArt(phase: Float) {
    val cloudC = Offset(size.width * 0.5f, size.height * 0.42f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.78f, size.height * 0.34f, 1.5.dp.toPx())
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.14f, EyeStyle.Open, MouthStyle.Smile, cheeks = true)
    val topY = size.height * 0.6f
    val fall = size.height * 0.32f
    listOf(0.28f, 0.38f, 0.5f, 0.62f, 0.72f).forEachIndexed { i, fx ->
        val p = (phase + i * 0.2f) % 1f
        val wobble = sin((phase * 4f + i) * Math.PI).toFloat() * size.width * 0.025f
        drawSnowflake(size.width * fx + wobble, topY + p * fall, size.width * 0.04f)
    }
}

private fun DrawScope.drawStormArt(phase: Float) {
    val cloudC = Offset(size.width * 0.5f, size.height * 0.4f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.8f, size.height * 0.38f, 1.5.dp.toPx(), fill = CloudGray.copy(alpha = 0.5f), outline = CloudGray)
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.15f, EyeStyle.Angry, MouthStyle.Frown, cheeks = false)
    val boltAlpha = if (phase > 0.85f) 1f else 0f
    val bolt = Path().apply {
        moveTo(size.width * 0.48f, size.height * 0.6f)
        lineTo(size.width * 0.42f, size.height * 0.76f)
        lineTo(size.width * 0.5f, size.height * 0.74f)
        lineTo(size.width * 0.44f, size.height * 0.92f)
        lineTo(size.width * 0.6f, size.height * 0.7f)
        lineTo(size.width * 0.52f, size.height * 0.72f)
        lineTo(size.width * 0.58f, size.height * 0.6f)
        close()
    }
    if (boltAlpha > 0f) drawPath(bolt, MustardYellow.copy(alpha = boltAlpha))
}

private fun DrawScope.drawClearNightArt(pulse: Float) {
    val c = Offset(size.width * 0.52f, size.height * 0.42f)
    val r = size.width * 0.3f
    drawCircle(MustardYellow, r, c)
    drawCircle(PaperBackground, r * 0.78f, Offset(c.x - r * 0.38f, c.y - r * 0.2f))
    drawFace(c.x + r * 0.08f, c.y + r * 0.05f, r * 0.52f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
    val starR = size.width * 0.045f
    drawStar(size.width * 0.2f, size.height * 0.24f, starR * (1f + pulse * 0.5f))
    drawStar(size.width * 0.82f, size.height * 0.2f, starR * (1.3f - pulse * 0.5f))
    drawStar(size.width * 0.9f, size.height * 0.52f, starR)
    drawStar(size.width * 0.14f, size.height * 0.56f, starR)
}

private fun DrawScope.drawNightCloudArt() {
    val c = Offset(size.width * 0.72f, size.height * 0.32f)
    val r = size.width * 0.2f
    drawCircle(MustardYellow, r, c)
    drawCircle(PaperBackground, r * 0.78f, Offset(c.x - r * 0.38f, c.y - r * 0.2f))
    drawFace(c.x + r * 0.08f, c.y + r * 0.05f, r * 0.5f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
    drawStar(size.width * 0.2f, size.height * 0.3f, size.width * 0.035f)
    drawStar(size.width * 0.88f, size.height * 0.58f, size.width * 0.035f)
    val cloudC = Offset(size.width * 0.5f, size.height * 0.62f)
    drawCloud(cloudC.x, cloudC.y, size.width * 0.72f, size.height * 0.36f, 1.6.dp.toPx())
    drawFace(cloudC.x, cloudC.y + size.height * 0.02f, size.width * 0.14f, EyeStyle.HalfClosed, MouthStyle.Calm, cheeks = false)
}

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
    onWeatherStateReload: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val prefs = remember(context) { Prefs.values(context) }

    @Suppress("DEPRECATION")
    val lifecycleOwner = LocalLifecycleOwner.current
    var statusTick by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) statusTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val permissionStatusText = remember(statusTick) { permissionStatus(context) }

    var locationQuery by remember {
        mutableStateOf(prefs.getString(Prefs.LOCATION_NAME, "") ?: "")
    }
    var widgetShowAlarms by remember {
        mutableStateOf(prefs.getBoolean(Prefs.WIDGET_SHOW_ALARMS, true))
    }
    fun reloadWeather() {
        onWeatherStateReload()
        WeatherScheduler.refreshNow(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            doGpsLocation(context)
            locationQuery = prefs.getString(Prefs.LOCATION_NAME, "") ?: ""
            reloadWeather()
        } else {
            toast(context, "Location permission denied")
        }
    }

    Spacer(Modifier.height(8.dp))
    Text(
        text = "Settings",
        fontSize = 24.sp,
        color = OliveDark,
        fontFamily = FontFamily.Cursive,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    )
    Card(modifier = Modifier.fillMaxWidth()) {
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
    }
    Spacer(Modifier.height(10.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Weather location",
                fontSize = 17.sp,
                color = TextBrown,
                fontFamily = FontFamily.Cursive,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "City used for the forecast.",
                fontSize = 14.sp,
                color = TextMuted,
                fontFamily = FontFamily.Cursive,
            )
            Spacer(Modifier.height(10.dp))
            var suggestions by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
            var showDropdown by remember { mutableStateOf(false) }
            val cityFieldFocus = remember { androidx.compose.ui.focus.FocusRequester() }

            LaunchedEffect(locationQuery) {
                val q = locationQuery.trim()
                if (q.length < 2) { suggestions = emptyList(); showDropdown = false; return@LaunchedEffect }
                delay(400)
                suggestions = withContext(Dispatchers.IO) {
                    geocode(q, 3) ?: emptyList()
                }
                showDropdown = suggestions.isNotEmpty()
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = { locationQuery = it; showDropdown = true },
                    label = { Text("City") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(cityFieldFocus),
                )
                DropdownMenu(
                    expanded = showDropdown && suggestions.isNotEmpty(),
                    onDismissRequest = { showDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.92f),
                ) {
                    suggestions.forEach { s ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(cityLabel(s), fontSize = 15.sp, color = TextBrown, fontFamily = FontFamily.Cursive)
                                    val country = s.optString("country_code", "")
                                    val admin = s.optString("admin1", "")
                                    if (country.isNotBlank()) Text("$admin, $country", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Cursive)
                                }
                            },
                            onClick = {
                                locationQuery = cityLabel(s)
                                applyLocation(context, s)
                                reloadWeather()
                                showDropdown = false
                                suggestions = emptyList()
                            },
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val activity = context as? android.app.Activity
                        if (activity != null &&
                            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                            activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        } else {
            doGpsLocation(context)
            locationQuery = prefs.getString(Prefs.LOCATION_NAME, "") ?: ""
            reloadWeather()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = OlivePrimary,
                        contentColor = PaperWarmTint,
                    ),
                ) {
                    Text("Use GPS", fontFamily = FontFamily.Cursive)
                }
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Show alarms on widget",
                        fontSize = 17.sp,
                        color = TextBrown,
                        fontFamily = FontFamily.Cursive,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "The home-screen widget lists your next alarms.",
                        fontSize = 14.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Cursive,
                    )
                }
                CustomSwitch(
                    checked = widgetShowAlarms,
                    onCheckedChange = { checked ->
                        widgetShowAlarms = checked
                        prefs.edit().putBoolean(Prefs.WIDGET_SHOW_ALARMS, checked).apply()
                        ClockWeatherWidget.updateAll(context)
                    },
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Permissions",
                fontSize = 17.sp,
                color = TextBrown,
                fontFamily = FontFamily.Cursive,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = permissionStatusText,
                fontSize = 14.sp,
                color = TextMuted,
                fontFamily = FontFamily.Cursive,
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { openAlarmPermissions(context) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = OlivePrimary,
                    contentColor = PaperWarmTint,
                ),
            ) {
                Text("Exact alarms & full-screen alerts", fontFamily = FontFamily.Cursive)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { requestNotificationPermission(context) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = OlivePrimary,
                    contentColor = PaperWarmTint,
                ),
            ) {
                Text("Allow notifications", fontFamily = FontFamily.Cursive)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { requestIgnoreBatteryOptimizations(context) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = OlivePrimary,
                    contentColor = PaperWarmTint,
                ),
            ) {
                Text("Disable battery optimization", fontFamily = FontFamily.Cursive)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { requestOverlayPermission(context) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = OlivePrimary,
                    contentColor = PaperWarmTint,
                ),
            ) {
                Text("Allow overlay (top of screen)", fontFamily = FontFamily.Cursive)
            }
        }
    }
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
    onTest: (SavedAlarm) -> Unit,
) {
    val context = LocalContext.current
    val isNew = existing == null
    val initialHour = existing?.hour ?: 7
    val initialMinute = existing?.minute ?: 0
    val timeState = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }
    var daysMask by remember { mutableIntStateOf(existing?.daysMask ?: AlarmStore.ALL_DAYS) }
    var soundUri by remember { mutableStateOf(existing?.soundUri) }
    var soundName by remember { mutableStateOf(existing?.soundName ?: "Default alarm") }
    val soundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
                // The current read grant still permits playback while the app is running.
            }
            soundUri = uri.toString()
            soundName = displayName(context, uri) ?: "Custom song"
        }
    }
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
            Column(Modifier.verticalScroll(rememberScrollState())) {
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
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Sound: $soundName",
                    fontSize = 15.sp,
                    color = TextBrown,
                    fontFamily = FontFamily.Cursive,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { soundPicker.launch(arrayOf("audio/*")) }) {
                        Text("Choose song", fontFamily = FontFamily.Cursive, color = OlivePrimary)
                    }
                    TextButton(onClick = {
                        soundUri = null
                        soundName = "Default alarm"
                    }) {
                        Text("Default", fontFamily = FontFamily.Cursive, color = TextMuted)
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(
                    onClick = {
                        onTest(
                            SavedAlarm(
                                id = existing?.id ?: AlarmStore.nextId(context),
                                hour = timeState.hour,
                                minute = timeState.minute,
                                enabled = enabled,
                                daysMask = if (daysMask == 0) AlarmStore.ALL_DAYS else daysMask,
                                soundUri = soundUri,
                                soundName = soundName,
                            ),
                        )
                    },
                ) {
                    Text("Test", fontFamily = FontFamily.Cursive, color = TextMuted)
                }
                TextButton(
                    onClick = {
                    val days = if (daysMask == 0) AlarmStore.ALL_DAYS else daysMask
                    val saved = SavedAlarm(
                        id = existing?.id ?: AlarmStore.nextId(context),
                        hour = timeState.hour,
                        minute = timeState.minute,
                        enabled = enabled,
                        daysMask = days,
                        soundUri = soundUri,
                        soundName = soundName,
                    )
                    onSave(saved)
                },
            ) {
                Text("Save", fontFamily = FontFamily.Cursive, color = OlivePrimary)
            }
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
