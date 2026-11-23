package com.vibhuti.orion

import android.app.Application
import com.vibhuti.orion.di.AppContainer

class OrionApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
