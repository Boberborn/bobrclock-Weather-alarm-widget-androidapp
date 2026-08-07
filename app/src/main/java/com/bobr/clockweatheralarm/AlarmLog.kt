package com.bobr.clockweatheralarm

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmLog {
    private const val KEY = "alarm_log"
    private const val MAX_ENTRIES = 100

    data class Entry(val time: Long, val message: String)

    fun log(context: Context, message: String) {
        val prefs = Prefs.values(context)
        val raw = prefs.getString(KEY, null)
        val array = try {
            JSONArray(raw ?: "")
        } catch (_: Exception) {
            JSONArray()
        }
        while (array.length() >= MAX_ENTRIES) array.remove(0)
        array.put(
            JSONObject()
                .put("t", System.currentTimeMillis())
                .put("m", message),
        )
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun entries(context: Context): List<Entry> {
        val raw = Prefs.values(context).getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val o = array.getJSONObject(index)
                    add(Entry(o.optLong("t", 0L), o.optString("m", "")))
                }
            }.reversed()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear(context: Context) {
        Prefs.values(context).edit().remove(KEY).apply()
    }

    fun formatTime(time: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))
}
