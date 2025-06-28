
package com.example.native_timer

import android.content.Context
import android.view.View
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

class NativeTimerView(context: Context, channel: MethodChannel) : PlatformView {
    private val composeView = ComposeView(context)

    init {
        composeView.setContent {
            TimerControls(channel)
        }
    }

    override fun getView(): View {
        return composeView
    }

    override fun dispose() {}
}

@Composable
fun TimerControls(channel: MethodChannel) {
    Row {
        Button(onClick = { channel.invokeMethod("start", null) }) {
            Text("Start")
        }
        Button(onClick = { channel.invokeMethod("pause", null) }) {
            Text("Pause")
        }
        Button(onClick = { channel.invokeMethod("reset", null) }) {
            Text("Reset")
        }
    }
}
