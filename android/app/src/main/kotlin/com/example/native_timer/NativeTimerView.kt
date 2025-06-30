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
import androidx.lifecycle.*
import androidx.savedstate.*
import androidx.activity.ComponentActivity

import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class NativeTimerView(context: Context, private val channel: MethodChannel) : PlatformView {

    private val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        
        // For PlatformView, we need to handle the lifecycle properly
        // Try to get the activity from context and set up the owners
        if (context is ComponentActivity) {
            setViewTreeLifecycleOwner(context)
            setViewTreeViewModelStoreOwner(context)
            setViewTreeSavedStateRegistryOwner(context)
        } else {
            // Fallback: try to find existing owners from the parent view hierarchy
            findViewTreeLifecycleOwner()?.let { setViewTreeLifecycleOwner(it) }
            findViewTreeViewModelStoreOwner()?.let { setViewTreeViewModelStoreOwner(it) }
            findViewTreeSavedStateRegistryOwner()?.let { setViewTreeSavedStateRegistryOwner(it) }
        }

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