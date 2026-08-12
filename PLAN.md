# Plan

- [x] Tap the widget's top-right corner (main temperature area) once → open the app on the Weather tab.
- [x] Ensure the app never turns on auto-rotate display.
- [x] Rename the Clock tab to Main.
- [x] Remove the separate Alarms tab and keep alarm features in the Main tab.
- [x] Verify the app works correctly in all countries (units, weather, date/time formats).
- [ ] Make the app multilingual: follow the Android system language by default, with an optional language override in settings.
- [x] System 12/24h time format: follow the system by default, with an optional override in settings.
- [~] Dark/AMOLED widget theme: follow the system dark mode or a per-widget toggle. (added then removed)
- [~] Weather alerts as an option, off by default. (added then removed)
- [x] Alarm in silent mode: option to play alarm even when the phone is in silent/DND mode.
- [x] Save/Load backup buttons live in the Widget editor menu (not the Settings tab).
- [x] Settings backup: "Save backup" and "Restore backup" buttons; if the user resets to defaults, restoring a backup reverts the last saved state.

## Branch

All of the above were prepared on a `features` branch and merged into `master`.

## Follow-up

- Multilingual support (system language + optional override) is the only open item.
- Weather alerts and the dark widget theme were intentionally removed from the final app.
