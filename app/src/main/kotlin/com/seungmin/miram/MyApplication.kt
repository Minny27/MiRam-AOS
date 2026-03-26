package com.seungmin.miram

import android.app.Application
import android.content.Context
import com.seungmin.miram.shared.alarm.AlarmNotificationHelper
import com.seungmin.miram.shared.style.withFixedFontScale
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.withFixedFontScale())
    }

    override fun onCreate() {
        super.onCreate()
        AlarmNotificationHelper.createChannel(this)
    }
}
