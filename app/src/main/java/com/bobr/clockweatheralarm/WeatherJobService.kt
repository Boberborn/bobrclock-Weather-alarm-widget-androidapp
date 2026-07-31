package com.bobr.clockweatheralarm

import android.app.job.JobParameters
import android.app.job.JobService
import android.location.LocationManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class WeatherJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        Thread {
            val success = refreshWeather()
            ClockWeatherWidget.updateAll(this)
            jobFinished(params, !success)
            if (params.jobId == WeatherScheduler.SCHEDULED_JOB_ID && success) {
                WeatherScheduler.scheduleNext(this)
            }
        }.start()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean = false

    private fun refreshWeather(): Boolean {
        val prefs = Prefs.values(this)
        val coords: Pair<String, String>? = prefs.getString(Prefs.LATITUDE, null)?.let { lat ->
            prefs.getString(Prefs.LONGITUDE, null)?.let { lon -> lat to lon }
        } ?: prefs.getString(Prefs.POSTCODE, null)?.let { query ->
            resolveQuery(query)
        } ?: lastKnownLocation(this)?.let { it.first.toString() to it.second.toString() }
        val (latitude, longitude) = coords ?: return false
        val endpoint = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,weather_code,wind_speed_10m,wind_direction_10m" +
                "&wind_speed_unit=mph" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min&forecast_days=6" +
                "&hourly=temperature_2m&forecast_hours=7&timezone=auto",
        )
        val connection = endpoint.openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "BobrClockWeatherAlarm/1.0")
            if (connection.responseCode !in 200..299) return false
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val current = json.getJSONObject("current")
            val hourly = json.getJSONObject("hourly")
            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val hourlyOut = StringBuilder()
            for (i in 1 until times.length()) {
                if (hourlyOut.isNotEmpty()) hourlyOut.append("|")
                hourlyOut.append(times.getString(i).substring(11, 16))
                    .append(";")
                    .append(temps.getDouble(i).toInt())
            }
            val daily = json.optJSONObject("daily")
            val dailyOut = StringBuilder()
            if (daily != null) {
                val dTimes = daily.getJSONArray("time")
                val dCodes = daily.getJSONArray("weather_code")
                val dMax = daily.getJSONArray("temperature_2m_max")
                val dMin = daily.getJSONArray("temperature_2m_min")
                for (i in 0 until dTimes.length()) {
                    if (dailyOut.isNotEmpty()) dailyOut.append("|")
                    dailyOut.append(dTimes.getString(i))
                        .append(";")
                        .append(dCodes.getInt(i))
                        .append(";")
                        .append(dMax.getDouble(i).toInt())
                        .append(";")
                        .append(dMin.getDouble(i).toInt())
                }
            }
            prefs.edit()
                .putString(Prefs.WEATHER_TEMP, current.getDouble("temperature_2m").toInt().toString())
                .putInt(Prefs.WEATHER_CODE, current.getInt("weather_code"))
                .putInt(
                    Prefs.WEATHER_WIND,
                    current.optDouble("wind_speed_10m", -1.0).toInt(),
                )
                .putInt(
                    Prefs.WEATHER_WIND_DIR,
                    current.optInt("wind_direction_10m", -1),
                )
                .putString(Prefs.WEATHER_DAILY, dailyOut.toString())
                .putLong(Prefs.WEATHER_UPDATED, System.currentTimeMillis())
                .putString(Prefs.WEATHER_HOURLY, hourlyOut.toString())
                .apply()
            true
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun resolveQuery(query: String): Pair<String, String>? {
        val encoded = URLEncoder.encode(query, Charsets.UTF_8.name())
        val endpoint = URL(
            "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$encoded&count=1&language=en&format=json",
        )
        val connection = endpoint.openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "BobrClockWeatherAlarm/1.0")
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val results = JSONObject(body).optJSONArray("results")
            if (results == null || results.length() == 0) return null
            val r = results.getJSONObject(0)
            r.getDouble("latitude").toString() to r.getDouble("longitude").toString()
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        fun lastKnownLocation(context: android.content.Context): Pair<String, String>? {
            val manager = context.getSystemService(LocationManager::class.java)
            return listOf(
                LocationManager.PASSIVE_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.GPS_PROVIDER,
            ).mapNotNull { provider ->
                try {
                    manager.getLastKnownLocation(provider)
                } catch (_: SecurityException) {
                    null
                }
            }.filter {
                System.currentTimeMillis() - it.time < 24 * 60 * 60 * 1000L
            }.maxByOrNull { it.accuracy }?.let {
                it.latitude.toString() to it.longitude.toString()
            }
        }
    }
}
