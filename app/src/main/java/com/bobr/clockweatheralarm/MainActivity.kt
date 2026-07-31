package com.bobr.clockweatheralarm

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.os.PowerManager
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Calendar

class MainActivity : Activity() {
    private lateinit var alarmsContainer: LinearLayout
    private lateinit var alarmEditor: LinearLayout
    private lateinit var editorTitle: TextView
    private lateinit var timePicker: TimePicker
    private lateinit var enabledSwitch: Switch
    private lateinit var soundText: TextView
    private val dayChecks = linkedMapOf<Int, CheckBox>()
    private var editingAlarmId: Int? = null
    private var selectedSoundUri: String? = null
    private var selectedSoundName = DEFAULT_SOUND_NAME

    private lateinit var locationInput: AutoCompleteTextView
    private lateinit var postcodeInput: EditText
    private var citySuggestions: List<JSONObject> = emptyList()
    private var suppressAutocomplete = false
    private lateinit var weatherInterval: SeekBar
    private lateinit var weatherIntervalLabel: TextView
    private lateinit var permissionStatus: TextView
    private lateinit var widgetShowAlarms: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
        loadWeatherValues()
        if (savedInstanceState == null) {
            startNewAlarm(show = false)
        } else {
            restoreEditor(savedInstanceState)
        }
        refreshAlarmList()
        requestNotificationPermission()
        AlarmScheduler.scheduleAll(this)
        WeatherScheduler.ensureScheduled(this)
        updatePermissionStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::permissionStatus.isInitialized) updatePermissionStatus()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_ALARM_ID, editingAlarmId ?: -1)
        outState.putInt(STATE_HOUR, timePicker.hour)
        outState.putInt(STATE_MINUTE, timePicker.minute)
        outState.putBoolean(STATE_ENABLED, enabledSwitch.isChecked)
        outState.putInt(STATE_DAYS, selectedDaysMask())
        outState.putString(STATE_SOUND_URI, selectedSoundUri)
        outState.putString(STATE_SOUND_NAME, selectedSoundName)
        outState.putBoolean(STATE_EDITOR_VISIBLE, alarmEditor.visibility == android.view.View.VISIBLE)
    }

    @Deprecated("The platform result API keeps this app dependency-free.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_AUDIO || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
            // The current read grant still permits previewing; playback falls back if it expires.
        }
        selectedSoundUri = uri.toString()
        selectedSoundName = displayName(uri) ?: getString(R.string.selected_song)
        updateSoundText()
    }

    private fun buildContent(): ScrollView {
        val density = resources.displayMetrics.density
        val padding = (20 * density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }
        root.addView(title(getString(R.string.app_name), 26f))
        root.addView(body(getString(R.string.low_memory_explanation)))

        root.addView(title(getString(R.string.alarms_section), 20f))
        alarmsContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(alarmsContainer)
        root.addView(Button(this).apply {
            text = getString(R.string.add_alarm)
            setOnClickListener { startNewAlarm() }
        })

        alarmEditor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = android.view.View.GONE
        }
        editorTitle = title("", 18f)
        alarmEditor.addView(editorTitle)
        enabledSwitch = Switch(this).apply { text = getString(R.string.alarm_enabled) }
        alarmEditor.addView(enabledSwitch)
        timePicker = TimePicker(this).apply { setIs24HourView(true) }
        alarmEditor.addView(timePicker)
        alarmEditor.addView(body(getString(R.string.repeat_on)))
        alarmEditor.addView(buildDaySelector())

        soundText = body("")
        alarmEditor.addView(soundText)
        alarmEditor.addView(
            buttonRow(
                Button(this).apply {
                    text = getString(R.string.choose_song)
                    setOnClickListener { chooseSong() }
                },
                Button(this).apply {
                    text = getString(R.string.default_sound)
                    setOnClickListener {
                        selectedSoundUri = null
                        selectedSoundName = DEFAULT_SOUND_NAME
                        updateSoundText()
                    }
                },
            ),
        )
        alarmEditor.addView(
            buttonRow(
                Button(this).apply {
                    text = getString(R.string.save_alarm)
                    setOnClickListener { saveAlarm() }
                },
                Button(this).apply {
                    text = getString(R.string.test_alarm)
                    setOnClickListener { testAlarm() }
                },
            ),
        )
        alarmEditor.addView(Button(this).apply {
            text = getString(R.string.close_editor)
            setOnClickListener { startNewAlarm(show = false) }
        })
        root.addView(alarmEditor)

        permissionStatus = body("")
        root.addView(permissionStatus)
        root.addView(Button(this).apply {
            text = getString(R.string.open_alarm_permissions)
            setOnClickListener { openAlarmPermissions() }
        })
        root.addView(Button(this).apply {
            text = getString(R.string.disable_battery_optimization)
            setOnClickListener { requestIgnoreBatteryOptimizations() }
        })

        root.addView(title(getString(R.string.weather_section), 20f))
        locationInput = AutoCompleteTextView(this).apply {
            hint = getString(R.string.location_name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            threshold = Int.MAX_VALUE
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
                override fun afterTextChanged(s: Editable?) {
                    if (!suppressAutocomplete) fetchCitySuggestions(s?.toString() ?: "")
                }
            })
            setOnItemClickListener { _, _, position, _ ->
                val result = citySuggestions.getOrNull(position) ?: return@setOnItemClickListener
                suppressAutocomplete = true
                setText(cityLabel(result))
                postcodeInput.setText(result.optString("postcode"))
            }
            setOnClickListener {
                suppressAutocomplete = false
                fetchCitySuggestions(text.toString())
            }
        }
        postcodeInput = input(getString(R.string.postcode), false)
        root.addView(locationInput)
        root.addView(postcodeInput)
        root.addView(
            buttonRow(
                Button(this).apply {
                    text = getString(R.string.save_weather)
                    setOnClickListener { saveWeather() }
                },
                Button(this).apply {
                    text = getString(R.string.use_my_location)
                    setOnClickListener { useGpsLocation() }
                },
            ),
        )
        weatherIntervalLabel = body("")
        root.addView(weatherIntervalLabel)
        weatherInterval = SeekBar(this).apply {
            max = (
                WeatherScheduler.MAX_INTERVAL_MINUTES -
                    WeatherScheduler.MIN_INTERVAL_MINUTES
                ) / 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    updateWeatherIntervalLabel(progressToMinutes(progress))
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val minutes = progressToMinutes(seekBar?.progress ?: 0)
                    WeatherScheduler.updateInterval(this@MainActivity, minutes)
                    toast(getString(R.string.weather_interval_saved, formatInterval(minutes)))
                }
            })
        }
        root.addView(weatherInterval)
        root.addView(body(getString(R.string.weather_note)))
        root.addView(title(getString(R.string.widget_section), 20f))
        root.addView(body(getString(R.string.widget_instructions)))
        widgetShowAlarms = Switch(this).apply {
            text = getString(R.string.widget_show_alarms)
            isChecked = Prefs.values(this@MainActivity)
                .getBoolean(Prefs.WIDGET_SHOW_ALARMS, true)
            setOnCheckedChangeListener { _, checked ->
                Prefs.values(this@MainActivity).edit()
                    .putBoolean(Prefs.WIDGET_SHOW_ALARMS, checked)
                    .apply()
                ClockWeatherWidget.updateAll(this@MainActivity)
            }
        }
        root.addView(widgetShowAlarms)

        return ScrollView(this).apply { addView(root) }
    }

    private fun buildDaySelector(): LinearLayout {
        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        DAYS.chunked(4).forEach { chunk ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            chunk.forEach { option ->
                val check = CheckBox(this).apply {
                    text = option.shortName
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                dayChecks[option.calendarDay] = check
                row.addView(check)
            }
            outer.addView(row)
        }
        return outer
    }

    private fun refreshAlarmList() {
        alarmsContainer.removeAllViews()
        val alarms = AlarmStore.load(this)
        if (alarms.isEmpty()) {
            alarmsContainer.addView(body(getString(R.string.no_alarms)))
            return
        }
        alarms.forEach { alarm ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 10, 0, 10)
            }
            card.addView(TextView(this).apply {
                text = getString(
                    R.string.alarm_list_item,
                    alarm.hour,
                    alarm.minute,
                    daysLabel(alarm.daysMask),
                )
                textSize = 17f
                setTypeface(typeface, Typeface.BOLD)
                alpha = if (alarm.enabled) 1f else 0.55f
            })
            card.addView(TextView(this).apply {
                text = getString(
                    if (alarm.enabled) R.string.alarm_on_with_sound else R.string.alarm_off_with_sound,
                    alarm.soundName,
                )
            })
            card.addView(
                buttonRow(
                    Button(this).apply {
                        text = getString(if (alarm.enabled) R.string.disable else R.string.enable)
                        setOnClickListener { toggleAlarm(alarm) }
                    },
                    Button(this).apply {
                        text = getString(R.string.edit)
                        setOnClickListener { editAlarm(alarm) }
                    },
                    Button(this).apply {
                        text = getString(R.string.delete)
                        setOnClickListener { confirmDelete(alarm) }
                    },
                ),
            )
            alarmsContainer.addView(card)
        }
    }

    private fun startNewAlarm(show: Boolean = true) {
        editingAlarmId = null
        editorTitle.text = getString(R.string.new_alarm)
        enabledSwitch.isChecked = true
        timePicker.hour = 7
        timePicker.minute = 0
        setSelectedDays(AlarmStore.ALL_DAYS)
        selectedSoundUri = null
        selectedSoundName = DEFAULT_SOUND_NAME
        updateSoundText()
        alarmEditor.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun editAlarm(alarm: SavedAlarm) {
        alarmEditor.visibility = android.view.View.VISIBLE
        editingAlarmId = alarm.id
        editorTitle.text = getString(R.string.edit_alarm)
        enabledSwitch.isChecked = alarm.enabled
        timePicker.hour = alarm.hour
        timePicker.minute = alarm.minute
        setSelectedDays(alarm.daysMask)
        selectedSoundUri = alarm.soundUri
        selectedSoundName = alarm.soundName
        updateSoundText()
    }

    private fun restoreEditor(state: Bundle) {
        editingAlarmId = state.getInt(STATE_ALARM_ID, -1).takeIf { it >= 0 }
        editorTitle.text = getString(
            if (editingAlarmId == null) R.string.new_alarm else R.string.edit_alarm,
        )
        timePicker.hour = state.getInt(STATE_HOUR, 7)
        timePicker.minute = state.getInt(STATE_MINUTE, 0)
        enabledSwitch.isChecked = state.getBoolean(STATE_ENABLED, true)
        setSelectedDays(state.getInt(STATE_DAYS, AlarmStore.ALL_DAYS))
        selectedSoundUri = state.getString(STATE_SOUND_URI)
        selectedSoundName = state.getString(STATE_SOUND_NAME, DEFAULT_SOUND_NAME)
        updateSoundText()
        alarmEditor.visibility = if (state.getBoolean(STATE_EDITOR_VISIBLE, false)) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }

    private fun saveAlarm() {
        val days = selectedDaysMask()
        if (days == 0) {
            toast(getString(R.string.choose_day))
            return
        }
        val id = editingAlarmId ?: AlarmStore.nextId(this)
        AlarmScheduler.cancel(this, id)
        val alarm = SavedAlarm(
            id = id,
            hour = timePicker.hour,
            minute = timePicker.minute,
            enabled = enabledSwitch.isChecked,
            daysMask = days,
            soundUri = selectedSoundUri,
            soundName = selectedSoundName,
        )
        AlarmStore.save(this, alarm)
        val scheduled = !alarm.enabled || (
            ensureExactAlarmPermission() && AlarmScheduler.schedule(context = this, alarm = alarm)
            )
        ClockWeatherWidget.updateAll(this)
        refreshAlarmList()
        startNewAlarm(show = false)
        toast(
            getString(
                if (scheduled) R.string.alarm_saved else R.string.exact_alarm_required,
            ),
        )
    }

    private fun toggleAlarm(alarm: SavedAlarm) {
        AlarmScheduler.cancel(this, alarm.id)
        val updated = alarm.copy(enabled = !alarm.enabled)
        AlarmStore.save(this, updated)
        if (updated.enabled && ensureExactAlarmPermission()) {
            AlarmScheduler.schedule(this, updated)
        }
        ClockWeatherWidget.updateAll(this)
        refreshAlarmList()
    }

    private fun confirmDelete(alarm: SavedAlarm) {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_alarm_question, alarm.hour, alarm.minute))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                AlarmScheduler.cancel(this, alarm.id)
                AlarmStore.delete(this, alarm.id)
                if (editingAlarmId == alarm.id) startNewAlarm(show = false)
                ClockWeatherWidget.updateAll(this)
                refreshAlarmList()
            }
            .show()
    }

    private fun testAlarm() {
        if (!ensureExactAlarmPermission()) return
        if (AlarmScheduler.scheduleTest(this, selectedSoundUri, selectedSoundName)) {
            toast(getString(R.string.test_alarm_scheduled))
        } else {
            toast(getString(R.string.exact_alarm_required))
        }
    }

    private fun chooseSong() {
        @Suppress("DEPRECATION")
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
                )
            },
            REQUEST_AUDIO,
        )
    }

    private fun displayName(uri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(
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

    private fun updateSoundText() {
        soundText.text = getString(R.string.alarm_sound, selectedSoundName)
    }

    private fun selectedDaysMask(): Int = dayChecks.entries.fold(0) { mask, entry ->
        if (entry.value.isChecked) {
            mask or (1 shl (entry.key - Calendar.SUNDAY))
        } else {
            mask
        }
    }

    private fun setSelectedDays(mask: Int) {
        dayChecks.forEach { (day, check) ->
            check.isChecked = mask and (1 shl (day - Calendar.SUNDAY)) != 0
        }
    }

    private fun daysLabel(mask: Int): String {
        if (mask == AlarmStore.ALL_DAYS) return getString(R.string.every_day)
        return DAYS.filter {
            mask and (1 shl (it.calendarDay - Calendar.SUNDAY)) != 0
        }.joinToString(" ") { it.shortName }
    }

    private fun loadWeatherValues() {
        val prefs = Prefs.values(this)
        locationInput.setText(prefs.getString(Prefs.LOCATION_NAME, getString(R.string.your_location)))
        postcodeInput.setText(prefs.getString(Prefs.POSTCODE, ""))
        val interval = WeatherScheduler.intervalMinutes(this)
        weatherInterval.progress = (interval - WeatherScheduler.MIN_INTERVAL_MINUTES) / 10
        updateWeatherIntervalLabel(interval)
    }

    private fun updateWeatherIntervalLabel(minutes: Int) {
        weatherIntervalLabel.text = getString(
            R.string.weather_update_interval,
            formatInterval(minutes),
        )
    }

    private fun formatInterval(minutes: Int): String {
        if (minutes < 60) return getString(R.string.minutes_short, minutes)
        val hours = minutes / 60
        val remainder = minutes % 60
        return if (remainder == 0) {
            getString(R.string.hours_short, hours)
        } else {
            getString(R.string.hours_minutes_short, hours, remainder)
        }
    }

    private fun progressToMinutes(progress: Int): Int =
        WeatherScheduler.MIN_INTERVAL_MINUTES + progress * 10

    private fun saveWeather() {
        val query = postcodeInput.text.toString().trim()
            .ifBlank { locationInput.text.toString().trim().substringBefore(",") }
        if (query.length < 2) {
            toast(getString(R.string.enter_postcode))
            return
        }
        toast(getString(R.string.searching_location))
        Thread {
            val results = geocode(query, 1)
            val result = results?.firstOrNull()
            runOnUiThread {
                if (result == null) {
                    toast(getString(R.string.location_not_found))
                } else {
                    saveLocation(result)
                }
            }
        }.start()
    }

    private fun useGpsLocation() {
        val coords = WeatherJobService.lastKnownLocation(this)
        if (coords == null) {
            toast(getString(R.string.location_not_found))
            return
        }
        suppressAutocomplete = true
        locationInput.setText(getString(R.string.your_location))
        postcodeInput.setText("")
        suppressAutocomplete = false
        Prefs.values(this).edit()
            .putString(Prefs.LOCATION_NAME, getString(R.string.your_location))
            .remove(Prefs.POSTCODE)
            .putString(Prefs.LATITUDE, coords.first)
            .putString(Prefs.LONGITUDE, coords.second)
            .apply()
        WeatherScheduler.refreshNow(this)
        ClockWeatherWidget.updateAll(this)
        toast(getString(R.string.weather_saved))
    }

    private fun saveLocation(result: JSONObject) {
        val name = result.getString("name")
        val country = result.optString("country")
        val lat = result.getDouble("latitude").toString()
        val lon = result.getDouble("longitude").toString()
        val postcode = result.optString("postcode").ifBlank { null }
        suppressAutocomplete = true
        locationInput.setText(cityLabel(result))
        postcodeInput.setText(postcode ?: "")
        suppressAutocomplete = false
        Prefs.values(this).edit()
            .putString(Prefs.LOCATION_NAME, cityLabel(result))
            .putString(Prefs.POSTCODE, postcode ?: "")
            .putString(Prefs.LATITUDE, lat)
            .putString(Prefs.LONGITUDE, lon)
            .apply()
        WeatherScheduler.refreshNow(this)
        ClockWeatherWidget.updateAll(this)
        toast(getString(R.string.weather_saved))
    }

    private fun cityLabel(result: JSONObject): String {
        val name = result.getString("name")
        val country = result.optString("country")
        return if (country.isBlank()) name else getString(R.string.location_with_country, name, country)
    }

    private fun fetchCitySuggestions(query: String) {
        if (query.length < 2 || suppressAutocomplete) return
        Thread {
            val results = geocode(query, 5)
            runOnUiThread {
                if (suppressAutocomplete) return@runOnUiThread
                citySuggestions = results ?: emptyList()
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    citySuggestions.map { cityLabel(it) },
                )
                locationInput.setAdapter(adapter)
                locationInput.showDropDown()
            }
        }.start()
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

    private fun ensureExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = getSystemService(AlarmManager::class.java)
        if (manager.canScheduleExactAlarms()) return true
        startActivity(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:$packageName"),
            ),
        )
        return false
    }

    private fun openAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ensureExactAlarmPermission()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val manager = getSystemService(NotificationManager::class.java)
            if (!manager.canUseFullScreenIntent()) {
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                        Uri.parse("package:$packageName"),
                    ),
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
        }
    }

    private fun updatePermissionStatus() {
        val exact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        val notifications = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val fullScreen = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
        val battery = getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
        permissionStatus.text = getString(
            R.string.permission_status,
            yesNo(exact),
            yesNo(notifications),
            yesNo(fullScreen),
            yesNo(battery),
        )
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)) {
            toast(getString(R.string.already_whitelisted))
            return
        }
        startActivity(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                .setData(Uri.parse("package:$packageName")),
        )
    }

    private fun title(text: String, size: Float) = TextView(this).apply {
        this.text = text
        textSize = size
        setTypeface(typeface, Typeface.BOLD)
        setPadding(0, 20, 0, 8)
    }

    private fun body(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setPadding(0, 4, 0, 12)
    }

    private fun input(hint: String, decimal: Boolean) = EditText(this).apply {
        this.hint = hint
        inputType = if (decimal) {
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }
    }

    private fun buttonRow(vararg buttons: Button) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        buttons.forEach { button ->
            button.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            )
            addView(button)
        }
    }

    private fun yesNo(value: Boolean) = if (value) getString(R.string.yes) else getString(R.string.no)

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private data class DayOption(val shortName: String, val calendarDay: Int)

    companion object {
        private const val REQUEST_AUDIO = 7201
        private const val REQUEST_BATTERY = 7202
        private const val DEFAULT_SOUND_NAME = "Default alarm"
        private const val STATE_ALARM_ID = "editor_alarm_id"
        private const val STATE_HOUR = "editor_hour"
        private const val STATE_MINUTE = "editor_minute"
        private const val STATE_ENABLED = "editor_enabled"
        private const val STATE_DAYS = "editor_days"
        private const val STATE_SOUND_URI = "editor_sound_uri"
        private const val STATE_SOUND_NAME = "editor_sound_name"
        private const val STATE_EDITOR_VISIBLE = "editor_visible"

        private val DAYS = listOf(
            DayOption("Mon", Calendar.MONDAY),
            DayOption("Tue", Calendar.TUESDAY),
            DayOption("Wed", Calendar.WEDNESDAY),
            DayOption("Thu", Calendar.THURSDAY),
            DayOption("Fri", Calendar.FRIDAY),
            DayOption("Sat", Calendar.SATURDAY),
            DayOption("Sun", Calendar.SUNDAY),
        )
    }
}
