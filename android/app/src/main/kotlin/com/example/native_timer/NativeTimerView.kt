package com.example.native_timer

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class NativeTimerView(context: Context, private val channel: MethodChannel) : PlatformView {

    private val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme {
                TimerControls(channel = channel)
            }
        }
    }

    override fun getView(): View {
        return composeView
    }

    override fun dispose() {}
}

@Composable
fun TimerControls(channel: MethodChannel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
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