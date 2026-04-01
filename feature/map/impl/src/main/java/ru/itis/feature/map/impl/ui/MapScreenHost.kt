package ru.itis.feature.map.impl.ui

import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MapScreenHost() {
    val context = LocalContext.current
    val fragmentManager = (context as AppCompatActivity).supportFragmentManager
    val containerId = remember { View.generateViewId() }

    LaunchedEffect(Unit) {
        if (fragmentManager.findFragmentById(containerId) == null) {
            fragmentManager.beginTransaction()
                .replace(containerId, MapScreenFragment::class.java, null)
                .commit()
        }
    }

    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply { id = containerId }
        },
    )
}