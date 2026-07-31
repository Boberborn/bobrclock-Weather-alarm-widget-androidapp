package com.bobr.clockweatheralarm

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

class AlarmActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }

        val padding = (24 * resources.displayMetrics.density).toInt()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(15, 23, 42))
        }
        layout.addView(TextView(this).apply {
            text = getString(R.string.alarm_ringing)
            textSize = 26f
            setTextColor(Color.WHITE)
        })
        layout.addView(TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "h:mm"
            textSize = 72f
            setTextColor(Color.WHITE)
        })
        layout.addView(Button(this).apply {
            text = getString(R.string.snooze_five_minutes)
            textSize = 22f
            setPadding(40, 30, 40, 30)
            setOnClickListener {
                if (
                    AlarmScheduler.scheduleSnooze(
                        this@AlarmActivity,
                        intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI),
                        intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_NAME) ?: "Default alarm",
                    )
                ) {
                    stopRinging()
                } else {
                    Toast.makeText(
                        this@AlarmActivity,
                        R.string.exact_alarm_required,
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
        })
        layout.addView(Button(this).apply {
            text = getString(R.string.stop_alarm)
            textSize = 22f
            setPadding(40, 30, 40, 30)
            setOnClickListener { stopRinging() }
        })
        setContentView(layout)
        hideSystemBars()
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    private fun stopRinging() {
        startService(
            Intent(this, AlarmService::class.java).setAction(AlarmService.ACTION_STOP),
        )
        finish()
    }
}
