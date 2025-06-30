package com.example.native_timer

import android.content.Context
import android.view.View
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class NativeTimerView(context: Context, private val channel: MethodChannel) : PlatformView {

    // Use a regular Android LinearLayout instead of ComposeView for PlatformView
    private val linearLayout = android.widget.LinearLayout(context).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER
        
        // Create buttons manually without Compose
        val startButton = android.widget.Button(context).apply {
            text = "Start"
            setOnClickListener { channel.invokeMethod("start", null) }
        }
        
        val pauseButton = android.widget.Button(context).apply {
            text = "Pause"
            setOnClickListener { channel.invokeMethod("pause", null) }
        }
        
        val resetButton = android.widget.Button(context).apply {
            text = "Reset"
            setOnClickListener { channel.invokeMethod("reset", null) }
        }
        
        addView(startButton)
        addView(pauseButton)
        addView(resetButton)
    }

    override fun getView(): View {
        return linearLayout
    }

    override fun dispose() {}
}