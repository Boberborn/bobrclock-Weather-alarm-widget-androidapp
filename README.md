# BobrClockWeatherAlarm

A deliberately small Android clock/date/weather widget with reliable recurring alarms.

## Design goals

- No persistent clock process: the launcher renders `TextClock`.
- No continuous location: uses the device's last known location (passive/GPS/cell), or user-set coordinates.
- Weather runs briefly on a user-selected 10-minute to 12-hour interval using
  `JobScheduler`; the default is one hour.
- No ads, analytics, trackers, or third-party runtime libraries.
- The time/day/song editor appears only after **Add alarm** or **Edit**.
- Multiple alarms support selected weekdays and a separate user-selected song.
- Alarms use `AlarmManager.setAlarmClock`, a foreground media-playback service,
  a full-screen ringing activity with Stop and 5-minute Snooze, vibration, and
  boot rescheduling.
- Minimum Android 8.0; target Android 16.

## Expected memory

The app normally has no process while idle. A cached process is expected to use
roughly 20–40 MB, a weather refresh roughly 25–60 MB for a few seconds, and an
active alarm roughly 20–40 MB. Device firmware and Android version affect these
figures.

## Build

```powershell
.\gradlew.bat assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Alarm reliability checklist

1. Open the app and allow notifications.
2. Tap **Review alarm permissions** and allow exact alarms and full-screen alarms.
3. Disable battery optimization for Bobr Clock in the phone's app battery settings.
4. Use **Test alarm in 10 seconds**, lock the phone, and confirm sound, vibration,
   and the full-screen alarm.
5. Reboot once and confirm the alarms remain listed.

Realme/OPlus firmware can aggressively restrict third-party alarm apps. Exact
alarm and notification permissions must remain enabled.

## Weather

Uses a postcode (resolved via Open-Meteo geocoding) or the device's last known location — no active tracking.
