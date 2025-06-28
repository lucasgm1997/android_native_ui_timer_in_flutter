package com.example.native_timer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.native_timer/timer"
    private val NOTIFICATION_CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1001

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        createNotificationChannel()
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startTimer" -> {
                    val duration = call.argument<Int>("duration") ?: 60
                    startNativeTimer(duration)
                    result.success(true)
                }
                "stopTimer" -> {
                    stopNativeTimer()
                    result.success(true)
                }
                "isTimerRunning" -> {
                    val isRunning = TimerService.isRunning()
                    result.success(isRunning)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun startNativeTimer(duration: Int) {
        val intent = Intent(this, TimerService::class.java).apply {
            putExtra("duration", duration)
            action = TimerService.ACTION_START_TIMER
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopNativeTimer() {
        val intent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        startService(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Notifications"
            val descriptionText = "Notificações do Timer Nativo"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
