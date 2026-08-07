package com.bobr.clockweatheralarm

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val soundUri = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_URI)
        val soundName = intent.getStringExtra(AlarmScheduler.EXTRA_SOUND_NAME) ?: "Default alarm"
        setContent {
            AlarmRingScreen(
                title = getString(R.string.alarm_ringing),
                onSnooze = {
                    AlarmLog.log(this, "snoozed from alarm screen (5 min)")
                    if (AlarmScheduler.scheduleSnooze(this, soundUri, soundName)) {
                        stopRinging()
                    } else {
                        Toast.makeText(this, R.string.exact_alarm_required, Toast.LENGTH_LONG).show()
                    }
                },
                onStop = {
                    AlarmLog.log(this, "alarm stopped from alarm screen")
                    stopRinging()
                },
            )
        }
    }

    private fun stopRinging() {
        startService(Intent(this, AlarmService::class.java).setAction(AlarmService.ACTION_STOP))
        finish()
    }
}

@Composable
private fun AlarmRingScreen(
    title: String,
    onSnooze: () -> Unit,
    onStop: () -> Unit,
) {
    val bg = Color(0xFF0F172A)
    var now by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            now = LocalTime.now()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            color = Color.White,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
            fontSize = 72.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onSnooze,
            modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
        ) {
            Text("Snooze 5 minutes", fontSize = 20.sp)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onStop,
            modifier = Modifier.height(56.dp).padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
        ) {
            Text("Stop", fontSize = 20.sp)
        }
    }
}
