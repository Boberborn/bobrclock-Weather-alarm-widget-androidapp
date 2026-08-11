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
            AlarmScheduler.ensureScheduled(this)
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
        val windUnit = Prefs.windUnit(this)
        val endpoint = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,apparent_temperature,relative_humidity_2m," +
                "precipitation,weather_code,cloud_cover,surface_pressure," +
                "wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index" +
                "&hourly=temperature_2m,precipitation_probability,weather_code," +
                "relative_humidity_2m,dew_point_2m,visibility,cloud_cover," +
                "surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
                "apparent_temperature_max,apparent_temperature_min,sunrise,sunset," +
                "uv_index_max,precipitation_sum,precipitation_probability_max," +
                "wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant" +
                "&wind_speed_unit=$windUnit" +
                "&forecast_days=7&timezone=auto",
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
            val hTimes = hourly.getJSONArray("time")
            val hTemps = hourly.getJSONArray("temperature_2m")
            val hCodes = hourly.getJSONArray("weather_code")
            val hPrecipProb = hourly.optJSONArray("precipitation_probability")
            val hHumidity = hourly.optJSONArray("relative_humidity_2m")
            val hDewPoint = hourly.optJSONArray("dew_point_2m")
            val hVisibility = hourly.optJSONArray("visibility")
            val hCloud = hourly.optJSONArray("cloud_cover")
            val hPressure = hourly.optJSONArray("surface_pressure")
            val hWindSpeed = hourly.optJSONArray("wind_speed_10m")
            val hWindDir = hourly.optJSONArray("wind_direction_10m")
            val hWindGust = hourly.optJSONArray("wind_gusts_10m")
            val hUv = hourly.optJSONArray("uv_index")

            val hourlyOut = StringBuilder()
            val hourlyAllOut = StringBuilder()
            val nowHour = java.time.LocalTime.now().withMinute(0).toString()
            var startIdx = 0
            while (startIdx < hTimes.length() &&
                hTimes.getString(startIdx).substring(11, 16) < nowHour
            ) {
                startIdx++
            }
            if (startIdx < hTimes.length()) {
                val limit = minOf(startIdx + 7, hTimes.length())
                for (i in startIdx until limit) {
                    if (hourlyOut.isNotEmpty()) hourlyOut.append("|")
                    hourlyOut.append(hTimes.getString(i).substring(11, 16))
                        .append(";")
                        .append(hTemps.getDouble(i).toInt())
                        .append(";")
                        .append(hCodes.getInt(i))
                }
            }
            for (i in 0 until hTimes.length()) {
                if (hourlyAllOut.isNotEmpty()) hourlyAllOut.append("|")
                hourlyAllOut.append(hTimes.getString(i).substring(0, 10))
                    .append(";")
                    .append(hTimes.getString(i).substring(11, 16))
                    .append(";")
                    .append(hTemps.getDouble(i).toInt())
                    .append(";")
                    .append(hCodes.getInt(i))
                    .append(";")
                    .append(optInt(hPrecipProb, i, -1))
                    .append(";")
                    .append(optInt(hHumidity, i, -1))
                    .append(";")
                    .append(optDoubleStr(hDewPoint, i))
                    .append(";")
                    .append(optDoubleStr(hVisibility, i))
                    .append(";")
                    .append(optInt(hCloud, i, -1))
                    .append(";")
                    .append(optDoubleStr(hPressure, i))
                    .append(";")
                    .append(optDoubleStr(hWindSpeed, i))
                    .append(";")
                    .append(optInt(hWindDir, i, -1))
                    .append(";")
                    .append(optDoubleStr(hWindGust, i))
                    .append(";")
                    .append(optDoubleStr(hUv, i))
            }

            val daily = json.optJSONObject("daily")
            val dailyOut = StringBuilder()
            if (daily != null) {
                val dTimes = daily.getJSONArray("time")
                val dCodes = daily.getJSONArray("weather_code")
                val dMax = daily.getJSONArray("temperature_2m_max")
                val dMin = daily.getJSONArray("temperature_2m_min")
                val dAppMax = daily.optJSONArray("apparent_temperature_max")
                val dAppMin = daily.optJSONArray("apparent_temperature_min")
                val dSunrise = daily.optJSONArray("sunrise")
                val dSunset = daily.optJSONArray("sunset")
                val dUvMax = daily.optJSONArray("uv_index_max")
                val dPrecipSum = daily.optJSONArray("precipitation_sum")
                val dPrecipProb = daily.optJSONArray("precipitation_probability_max")
                val dWindMax = daily.optJSONArray("wind_speed_10m_max")
                val dWindGust = daily.optJSONArray("wind_gusts_10m_max")
                val dWindDir = daily.optJSONArray("wind_direction_10m_dominant")
                for (i in 0 until dTimes.length()) {
                    if (dailyOut.isNotEmpty()) dailyOut.append("|")
                    dailyOut.append(dTimes.getString(i))
                        .append(";").append(dCodes.getInt(i))
                        .append(";").append(dMax.getDouble(i).toInt())
                        .append(";").append(dMin.getDouble(i).toInt())
                        .append(";").append(optInt(dAppMax, i, dMax.getDouble(i).toInt()))
                        .append(";").append(optInt(dAppMin, i, dMin.getDouble(i).toInt()))
                        .append(";").append(optString(dSunrise, i))
                        .append(";").append(optString(dSunset, i))
                        .append(";").append(optDoubleStr(dUvMax, i))
                        .append(";").append(optDoubleStr(dPrecipSum, i))
                        .append(";").append(optInt(dPrecipProb, i, -1))
                        .append(";").append(optDoubleStr(dWindMax, i))
                        .append(";").append(optDoubleStr(dWindGust, i))
                        .append(";").append(optInt(dWindDir, i, -1))
                }
            }

            val uv = current.optDouble("uv_index", -1.0)
            val uvText = if (uv >= 0) {
                val s = String.format(java.util.Locale.US, "%.1f", uv)
                if (s.endsWith(".0")) s.dropLast(2) else s
            } else null

            val visibility = if (hVisibility != null && hVisibility.length() > 0) {
                val v = hVisibility.getDouble(0)
                String.format(java.util.Locale.US, "%.0f", v)
            } else null

            val dewPoint = if (hDewPoint != null && hDewPoint.length() > 0) {
                val dp = hDewPoint.getDouble(0)
                String.format(java.util.Locale.US, "%.1f", dp)
            } else null

            prefs.edit()
                .putString(Prefs.WEATHER_TEMP, current.getDouble("temperature_2m").toInt().toString())
                .putInt(Prefs.WEATHER_CODE, current.getInt("weather_code"))
                .putString(Prefs.WEATHER_FEELS_LIKE, current.getDouble("apparent_temperature").toInt().toString())
                .putString(Prefs.WEATHER_HUMIDITY, current.getDouble("relative_humidity_2m").toInt().toString())
                .putString(Prefs.WEATHER_PRESSURE, current.getDouble("surface_pressure").toString())
                .putString(Prefs.WEATHER_CLOUD_COVER, current.getDouble("cloud_cover").toInt().toString())
                .putInt(Prefs.WEATHER_WIND, current.optDouble("wind_speed_10m", -1.0).toInt())
                .putInt(Prefs.WEATHER_WIND_DIR, current.optInt("wind_direction_10m", -1))
                .putString(Prefs.WEATHER_WIND_GUST, current.getDouble("wind_gusts_10m").toInt().toString())
                .putString(Prefs.WEATHER_PRECIPITATION, current.getDouble("precipitation").toString())
                .putString(Prefs.WEATHER_VISIBILITY, visibility)
                .putString(Prefs.WEATHER_DEW_POINT, dewPoint)
                .putString(Prefs.WEATHER_DAILY, dailyOut.toString())
                .putString(Prefs.WEATHER_UV, uvText)
                .putLong(Prefs.WEATHER_UPDATED, System.currentTimeMillis())
                .putString(Prefs.WEATHER_HOURLY, hourlyOut.toString())
                .putString(Prefs.WEATHER_HOURLY_ALL, hourlyAllOut.toString())
                .apply()
            true
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun optInt(arr: org.json.JSONArray?, idx: Int, fallback: Int): Int {
        if (arr == null || idx >= arr.length()) return fallback
        return try { arr.getDouble(idx).toInt() } catch (_: Exception) { fallback }
    }

    private fun optDoubleStr(arr: org.json.JSONArray?, idx: Int): String {
        if (arr == null || idx >= arr.length()) return ""
        return try { arr.getDouble(idx).toString() } catch (_: Exception) { "" }
    }

    private fun optString(arr: org.json.JSONArray?, idx: Int): String {
        if (arr == null || idx >= arr.length()) return ""
        return try { arr.getString(idx) } catch (_: Exception) { "" }
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
            val cc = r.optString("country_code", null)
            if (cc != null) {
                Prefs.values(this).edit().putString(Prefs.COUNTRY_CODE, cc).apply()
            }
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
