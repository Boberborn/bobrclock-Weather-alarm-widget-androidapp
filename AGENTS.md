# BobrClockWeatherAlarm — Agent Context

## Project
Minimal Android clock/weather/alarm app. Zero dependencies. Kotlin + Gradle.

**Repo:** https://github.com/Boberborn/tinyClock-Weather-alarm

## Setup on new machine
```bash
git clone https://github.com/Boberborn/tinyClock-Weather-alarm.git
cd tinyClock-Weather-alarm
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```
Needs Android SDK 36, Java 17, Gradle 8.13 (wrapper included).

## Architecture
- **MainActivity** — single Activity, programmatic UI. Alarm CRUD, location config (autocomplete city / postcode / GPS), permissions, battery whitelist.
- **AlarmStore** — JSON-in-SharedPreferences, multi-alarm with weekday masks + custom sounds.
- **AlarmScheduler** — `AlarmManager.setAlarmClock` exact scheduling.
- **AlarmReceiver** → **AlarmService** (foreground mediaPlayback) → **AlarmActivity** (full-screen stop/snooze).
- **BootReceiver** — reschedules alarms + weather on boot/timezone change/app update.
- **WeatherJobService** — `JobScheduler` periodic weather fetch from Open-Meteo API. Resolves location query (city/postcode) to coords. Falls back to device last-known GPS.
- **ClockWeatherWidget** — home-screen widget with TextClock + weather + hourly forecast row.
- **Prefs** — single SharedPreferences key constants.

## Key files
| File | Purpose |
|---|---|
| `app/src/main/java/.../MainActivity.kt` | All UI: alarms, location (AutoCompleteTextView + 5 suggestions), save weather, GPS button, permission status |
| `app/src/main/java/.../AlarmStore.kt` | Alarm persistence (JSON array in SharedPreferences) |
| `app/src/main/java/.../AlarmScheduler.kt` | AlarmManager exact scheduling |
| `app/src/main/java/.../AlarmService.kt` | Foreground service: sound, vibration, notification |
| `app/src/main/java/.../AlarmActivity.kt` | Full-screen alarm UI (Stop + Snooze buttons) |
| `app/src/main/java/.../WeatherJobService.kt` | Weather fetch, location resolve, hourly data |
| `app/src/main/java/.../ClockWeatherWidget.kt` | Widget with hourly forecast rendering |
| `app/src/main/res/layout/widget_clock_weather.xml` | Widget layout (clock | weather + hourly row) |
| `app/src/main/res/layout/widget_hour_item.xml` | Single hour slot layout |

## Location flow
1. User types city → AutoCompleteTextView shows 5 suggestions via Open-Meteo geocoding API
2. User picks suggestion → fills city name + postcode (if available) + saves coords
3. User enters postcode → on save, resolves to city name via geocoding API
4. User taps "Use my GPS location" → uses device last-known location (passive/network/GPS)
5. Weather refresh resolves stored query or coords via geocoding, fetches from Open-Meteo

## Build
```powershell
.\gradlew.bat assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`

## After any code change
1. Build: `./gradlew.bat assembleDebug`
2. Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. **Do NOT commit or push** — only push when the user explicitly says to push to GitHub.

## Ponytail rules (always follow)
- No abstractions not requested. No new dependencies. Deletion over addition.
- YAGNI: 1) skip if not needed 2) reuse existing 3) stdlib 4) platform feature 5) installed dep 6) one line 7) minimum that works
- Bug fix = root cause. Fix shared code once, not per-caller.
- Non-trivial logic leaves one runnable check (assert/self-check/test).

## Known issues fixed
- AlarmActivity crash: `hideSystemBars()` moved after `setContentView()`
- WeatherJobService double-scheduling: `scheduleNext()` only on success, not in finally
- Widget top free space removed (gravity top)
- Widget wider (targetCellWidth 5)
- Widget hourly forecast row added (next 6 hours)
- Choceň hardcoded default removed → uses GPS/city/postcode
- Battery optimization whitelist button added

## Known issues open
- Alarm reliability on OPlus/Realme firmware (OEM kills 3rd-party alarms) — mitigated by battery whitelist prompt
