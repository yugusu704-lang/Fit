package com.fit.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fit.tracker.ui.theme.FitTheme
import com.fit.tracker.ui.tracker.TrackerScreen
import com.fit.tracker.ui.tracker.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels {
        val app = application as FitApplication
        TrackerViewModel.Factory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerScreen(viewModel = viewModel)
                }
            }
        }
    }
}
