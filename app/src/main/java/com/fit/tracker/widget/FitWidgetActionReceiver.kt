package com.fit.tracker.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fit.tracker.data.local.db.FitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FitWidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_TOGGLE_EXERCISE) {
            val date = intent.getStringExtra(EXTRA_DATE) ?: return
            val exerciseId = intent.getStringExtra(EXTRA_EXERCISE_ID) ?: return

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FitDatabase.getInstance(context)
                    val allocation = db.exerciseAllocationDao().getAllocation(date, exerciseId)
                    if (allocation != null) {
                        db.exerciseAllocationDao().updateCompletion(date, exerciseId, !allocation.isCompleted)
                        FitWidgetProvider.notifyWidgetsUpdate(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_EXERCISE = "com.fit.tracker.widget.ACTION_TOGGLE_EXERCISE"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_EXERCISE_ID = "extra_exercise_id"
    }
}
