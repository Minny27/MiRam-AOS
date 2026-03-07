package com.example.miram

import android.app.Application
import com.example.miram.shared.alarm.AlarmNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AlarmNotificationHelper.createChannel(this)
    }
}
