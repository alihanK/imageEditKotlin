package com.package.picchhanger.daggerComponents

import android.content.Context
import com.package.picchhanger.MainActivity
import com.package.picchhanger.AppModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}
