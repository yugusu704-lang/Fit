package com.fit.tracker

import android.app.Application
import com.fit.tracker.data.local.db.FitDatabase
import com.fit.tracker.data.repository.FitRepository
import com.fit.tracker.widget.FitWidgetRefreshReceiver

class FitApplication : Application() {
    val database: FitDatabase by lazy { FitDatabase.getInstance(this) }
    val repository: FitRepository by lazy { FitRepository(database, this) }

    override fun onCreate() {
        super.onCreate()
        FitWidgetRefreshReceiver.scheduleNextMidnightAlarm(this)
    }
}
