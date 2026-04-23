package com.openclaw.companions

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenClawApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide services
    }
}
