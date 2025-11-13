package com.example.native_timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class NativeTimerView(private val context: Context, private val channel: MethodChannel) : PlatformView, TimerService.TimerListener {

    private var timerService: TimerService? = null
    private var bound: Boolean = false
    private lateinit var timeDisplay: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.TimerBinder
            timerService = binder.getService()
            bound = true
            timerService?.addListener(this@NativeTimerView)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            timerService?.removeListener(this@NativeTimerView)
            timerService = null
        }
    }

    // Create the native Android layout
    private val mainLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = android.view.Gravity.CENTER
        setPadding(16, 16, 16, 16)
        
        // Time display
        timeDisplay = TextView(context).apply {
            text = "00:00:00"
            textSize = 24f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 16)
        }
        addView(timeDisplay)
        
        // Buttons container
        val buttonsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            
            startButton = Button(context).apply {
                text = "Start"
                setOnClickListener { 
                    startTimerService()
                }
            }
            
            pauseButton = Button(context).apply {
                text = "Pause"
                setOnClickListener { 
                    pauseTimerService()
                }
            }
            
            resetButton = Button(context).apply {
                text = "Reset"
                setOnClickListener { 
                    resetTimerService()
                }
            }
            
            addView(startButton)
            addView(pauseButton)
            addView(resetButton)
        }
        addView(buttonsLayout)
    }

    init {
        // Bind to TimerService
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun startTimerService() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
        }
        context.startService(intent)
    }

    private fun pauseTimerService() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    private fun resetTimerService() {
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        context.startService(intent)
    }

    override fun onTimerUpdate(seconds: Int, isRunning: Boolean) {
        // Update the time display
        val timeText = formatTime(seconds)
        timeDisplay.text = timeText
        
        // Update button states
        startButton.isEnabled = !isRunning
        pauseButton.isEnabled = isRunning
        resetButton.isEnabled = true
        
        // Notify Flutter if needed (optional)
        channel.invokeMethod("onTimerUpdate", mapOf(
            "seconds" to seconds,
            "isRunning" to isRunning,
            "timeText" to timeText
        ))
    }

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun getView(): View {
        return mainLayout
    }

    override fun dispose() {
        if (bound) {
            timerService?.removeListener(this)
            context.unbindService(connection)
            bound = false
        }
    }
}