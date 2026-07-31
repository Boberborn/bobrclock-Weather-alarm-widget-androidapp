package com.bobr.clockweatheralarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AlarmService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var soundName: String = "Default alarm"
    private var soundUri: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        soundName = intent?.getStringExtra(AlarmScheduler.EXTRA_SOUND_NAME)
            ?.ifBlank { "Default alarm" }
            ?: "Default alarm"
        soundUri = intent?.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI)
        createChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startSoundAndVibration(soundUri)
        showAlarmScreen()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.release()
        player = null
        vibrator?.cancel()
        super.onDestroy()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.alarm_channel),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.alarm_channel_description)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val alarmScreen = Intent(this, AlarmActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
            .putExtra(AlarmScheduler.EXTRA_SOUND_URI, soundUri)
            .putExtra(AlarmScheduler.EXTRA_SOUND_NAME, soundName)
        val fullScreen = PendingIntent.getActivity(
            this,
            4201,
            alarmScreen,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getBroadcast(
            this,
            4202,
            Intent(this, AlarmActionReceiver::class.java)
                .setAction(AlarmActionReceiver.ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val snooze = PendingIntent.getBroadcast(
            this,
            4203,
            Intent(this, AlarmActionReceiver::class.java)
                .setAction(AlarmActionReceiver.ACTION_SNOOZE)
                .putExtra(AlarmScheduler.EXTRA_SOUND_URI, soundUri)
                .putExtra(AlarmScheduler.EXTRA_SOUND_NAME, soundName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(getString(R.string.alarm_ringing))
            .setContentText(soundName)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(fullScreen)
            .setFullScreenIntent(fullScreen, true)
            .addAction(
                Notification.Action.Builder(
                    null,
                    getString(R.string.snooze_five_minutes),
                    snooze,
                ).build(),
            )
            .addAction(Notification.Action.Builder(null, getString(R.string.stop_alarm), stop).build())
            .build()
    }

    private fun showAlarmScreen() {
        try {
            startActivity(
                Intent(this, AlarmActivity::class.java)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    )
                    .putExtra(AlarmScheduler.EXTRA_SOUND_URI, soundUri)
                    .putExtra(AlarmScheduler.EXTRA_SOUND_NAME, soundName),
            )
        } catch (_: Exception) {
            // The full-screen notification remains the platform-approved fallback.
        }
    }

    private fun startSoundAndVibration(customUri: String?) {
        player?.release()
        player = createPlayer(customUri?.let(Uri::parse)) ?: createPlayer(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        )

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0),
        )
    }

    private fun createPlayer(uri: Uri?): MediaPlayer? {
        if (uri == null) return null
        return try {
            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                setDataSource(this@AlarmService, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun stopAlarm() {
        player?.release()
        player = null
        vibrator?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        const val ACTION_START = "com.bobr.clockweatheralarm.START_ALARM"
        const val ACTION_STOP = "com.bobr.clockweatheralarm.STOP_ALARM"
        private const val CHANNEL_ID = "alarm"
        private const val NOTIFICATION_ID = 4301
    }
}
