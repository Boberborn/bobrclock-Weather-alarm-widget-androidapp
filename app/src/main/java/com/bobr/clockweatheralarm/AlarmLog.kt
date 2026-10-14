package com.bobr.clockweatheralarm

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmLog {
    private const val KEY = "alarm_log"
    private const val MAX_ENTRIES = 100
    private const val MAX_AGE_DAYS = 3
    private val MAX_AGE_MS = MAX_AGE_DAYS * 24L * 60 * 60 * 1000

    data class Entry(val time: Long, val message: String)

    fun log(context: Context, message: String) {
        val prefs = Prefs.values(context)
        pruneOld(prefs)
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
        val prefs = Prefs.values(context)
        pruneOld(prefs)
        val raw = prefs.getString(KEY, null) ?: return emptyList()
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

    private fun pruneOld(prefs: SharedPreferences) {
        val raw = prefs.getString(KEY, null) ?: return
        val array = try {
            JSONArray(raw)
        } catch (_: Exception) {
            return
        }
        val cutoff = System.currentTimeMillis() - MAX_AGE_MS
        var removed = false
        for (i in array.length() - 1 downTo 0) {
            val o = array.optJSONObject(i) ?: continue
            if (o.optLong("t", 0L) < cutoff) {
                array.remove(i)
                removed = true
            }
        }
        if (removed) prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun formatTime(time: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))
}
