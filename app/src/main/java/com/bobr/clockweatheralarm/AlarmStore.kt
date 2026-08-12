package com.bobr.clockweatheralarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class SavedAlarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val daysMask: Int,
    val soundUri: String?,
    val soundName: String,
)

object AlarmStore {
    const val ALL_DAYS = 0b1111111

    fun load(context: Context): List<SavedAlarm> {
        migrateLegacyAlarm(context)
        val raw = Prefs.values(context).getString(Prefs.ALARMS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val value = array.getJSONObject(index)
                    add(
                        SavedAlarm(
                            id = value.getInt("id"),
                            hour = value.getInt("hour").coerceIn(0, 23),
                            minute = value.getInt("minute").coerceIn(0, 59),
                            enabled = value.optBoolean("enabled", true),
                            daysMask = value.optInt("days", ALL_DAYS) and ALL_DAYS,
                            soundUri = value.optString("soundUri").ifBlank { null },
                            soundName = value.optString("soundName", "Default alarm"),
                        ),
                    )
                }
            }.sortedWith(compareBy(SavedAlarm::hour, SavedAlarm::minute, SavedAlarm::id))
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun find(context: Context, id: Int): SavedAlarm? = load(context).firstOrNull { it.id == id }

    fun nextId(context: Context): Int {
        val prefs = Prefs.values(context)
        val next = prefs.getInt(Prefs.NEXT_ALARM_ID, 1).coerceAtLeast(1)
        prefs.edit().putInt(Prefs.NEXT_ALARM_ID, next + 1).apply()
        return next
    }

    fun save(context: Context, alarm: SavedAlarm) {
        val alarms = load(context).filterNot { it.id == alarm.id } + alarm
        write(context, alarms)
    }

    fun delete(context: Context, id: Int) {
        write(context, load(context).filterNot { it.id == id })
    }

    fun nextEnabled(context: Context): Pair<SavedAlarm, Long>? =
        load(context)
            .filter { it.enabled && it.daysMask != 0 }
            .map { it to nextTrigger(it) }
            .minByOrNull { it.second }

    fun nextTrigger(alarm: SavedAlarm, nowMillis: Long = System.currentTimeMillis()): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMillis }
        for (offset in 0..7) {
            val candidate = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                add(Calendar.DAY_OF_YEAR, offset)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val dayBit = 1 shl (candidate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)
            if (alarm.daysMask and dayBit != 0 && candidate.timeInMillis > now.timeInMillis) {
                return candidate.timeInMillis
            }
        }
        return nowMillis + 7L * 24L * 60L * 60L * 1000L
    }

    private fun write(context: Context, alarms: List<SavedAlarm>) {
        val array = JSONArray()
        alarms.forEach { alarm ->
            array.put(
                JSONObject()
                    .put("id", alarm.id)
                    .put("hour", alarm.hour)
                    .put("minute", alarm.minute)
                    .put("enabled", alarm.enabled)
                    .put("days", alarm.daysMask)
                    .put("soundUri", alarm.soundUri ?: "")
                    .put("soundName", alarm.soundName),
            )
        }
        Prefs.values(context).edit().putString(Prefs.ALARMS, array.toString()).commit()
    }

    private fun migrateLegacyAlarm(context: Context) {
        val prefs = Prefs.values(context)
        if (prefs.contains(Prefs.ALARMS) || !prefs.contains(Prefs.ALARM_ENABLED)) return

        val migrated = if (prefs.getBoolean(Prefs.ALARM_ENABLED, false)) {
            listOf(
                SavedAlarm(
                    id = 1,
                    hour = prefs.getInt(Prefs.ALARM_HOUR, 7),
                    minute = prefs.getInt(Prefs.ALARM_MINUTE, 0),
                    enabled = true,
                    daysMask = ALL_DAYS,
                    soundUri = null,
                    soundName = "Default alarm",
                ),
            )
        } else {
            emptyList()
        }
        write(context, migrated)
        prefs.edit()
            .putInt(Prefs.NEXT_ALARM_ID, if (migrated.isEmpty()) 1 else 2)
            .remove(Prefs.ALARM_ENABLED)
            .remove(Prefs.ALARM_HOUR)
            .remove(Prefs.ALARM_MINUTE)
            .apply()
    }
}
