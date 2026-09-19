package com.fit.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.fit.tracker.ui.theme.FitTheme
import com.fit.tracker.ui.tracker.TrackerScreen
import com.fit.tracker.ui.tracker.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TrackerViewModel by viewModels {
        val app = application as FitApplication
        TrackerViewModel.Factory(app.repository)
    }

    private val openFoodDialogState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            FitTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerScreen(
                        viewModel = viewModel,
                        initialOpenFoodDialog = openFoodDialogState.value,
                        onFoodDialogOpened = { openFoodDialogState.value = false }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getStringExtra(EXTRA_ACTION) == ACTION_OPEN_FOOD_DIALOG) {
            openFoodDialogState.value = true
        }
    }

    companion object {
        const val EXTRA_ACTION = "extra_action"
        const val ACTION_OPEN_FOOD_DIALOG = "action_open_food_dialog"
    }
}
