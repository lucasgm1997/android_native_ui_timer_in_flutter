package com.example.native_timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class TimerService : Service() {
    
    companion object {
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"
        private const val NOTIFICATION_CHANNEL_ID = "timer_channel"
        private const val FOREGROUND_NOTIFICATION_ID = 1000
        private const val COMPLETION_NOTIFICATION_ID = 1001
        
        private var isTimerRunning = false
        private var countDownTimer: CountDownTimer? = null
        
        fun isRunning(): Boolean = isTimerRunning
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                val duration = intent.getIntExtra("duration", 60)
                startTimer(duration)
            }
            ACTION_STOP_TIMER -> {
                stopTimer()
            }
        }
        return START_STICKY
    }

    private fun startTimer(durationInSeconds: Int) {
        if (isTimerRunning) {
            stopTimer()
        }

        isTimerRunning = true
        
        // Start foreground service
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification(durationInSeconds))

        countDownTimer = object : CountDownTimer((durationInSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                updateForegroundNotification(secondsRemaining)
            }

            override fun onFinish() {
                onTimerComplete()
            }
        }.start()
    }

    private fun stopTimer() {
        isTimerRunning = false
        countDownTimer?.cancel()
        countDownTimer = null
        stopForeground(true)
        stopSelf()
    }

    private fun onTimerComplete() {
        isTimerRunning = false
        countDownTimer = null
        
        // Show completion notification
        showCompletionNotification()
        
        // Stop foreground service
        stopForeground(true)
        stopSelf()
    }

    private fun createForegroundNotification(totalSeconds: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Ativo")
            .setContentText("Timer de ${totalSeconds}s iniciado")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Parar", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun updateForegroundNotification(secondsRemaining: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP_TIMER
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Ativo")
            .setContentText("Restam ${secondsRemaining}s")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Parar", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(FOREGROUND_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    private fun showCompletionNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Timer Concluído!")
            .setContentText("O timer foi finalizado com sucesso")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        try {
            notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle permission denied
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
} 