package com.package.picchhanger

import android.app.Application
import com.package.picchhanger.daggerComponents.AppComponent
import com.package.picchhanger.daggerComponents.DaggerAppComponent

class CropApp : Application() {
    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }
}
