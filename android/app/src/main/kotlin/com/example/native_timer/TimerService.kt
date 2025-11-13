package com.example.native_timer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import java.util.*
import kotlin.collections.ArrayList

class TimerService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "timer_channel"
        private const val CHANNEL_NAME = "Timer Service"
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_GET_STATUS = "ACTION_GET_STATUS"
        
        @Volatile
        private var instance: TimerService? = null
        
        fun getInstance(): TimerService? = instance
    }

    private val binder = TimerBinder()
    private var timer: Timer? = null
    private var timerHandler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private var isRunning = false
    private val listeners = ArrayList<TimerListener>()

    interface TimerListener {
        fun onTimerUpdate(seconds: Int, isRunning: Boolean)
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESET -> resetTimer()
        }
        
        updateNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        timer?.cancel()
    }

    fun addListener(listener: TimerListener) {
        listeners.add(listener)
        // Immediately notify the listener of current state
        listener.onTimerUpdate(elapsedSeconds, isRunning)
    }

    fun removeListener(listener: TimerListener) {
        listeners.remove(listener)
    }

    fun startTimer() {
        if (!isRunning) {
            isRunning = true
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    elapsedSeconds++
                    timerHandler.post {
                        notifyListeners()
                        updateNotification()
                    }
                }
            }, 1000, 1000)
            
            startForeground(NOTIFICATION_ID, createNotification())
            notifyListeners()
        }
    }

    fun pauseTimer() {
        if (isRunning) {
            isRunning = false
            timer?.cancel()
            timer = null
            notifyListeners()
            updateNotification()
        }
    }

    fun resetTimer() {
        timer?.cancel()
        timer = null
        isRunning = false
        elapsedSeconds = 0
        notifyListeners()
        updateNotification()
    }

    fun getElapsedSeconds(): Int = elapsedSeconds
    fun isTimerRunning(): Boolean = isRunning

    private fun notifyListeners() {
        listeners.forEach { it.onTimerUpdate(elapsedSeconds, isRunning) }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = formatTime(elapsedSeconds)
        val statusText = if (isRunning) "Running" else "Paused"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Native Timer - $statusText")
            .setContentText("Time: $timeText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
} 