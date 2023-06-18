package com.aaron.rollpicker

import android.app.Application
import android.content.Context

/**
 * @author aaronzzxup@gmail.com
 * @since 2023/6/18
 */
class App : Application() {

    companion object {
        lateinit var applicationContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        App.applicationContext = applicationContext
    }
}