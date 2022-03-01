package com.example.signaltestapp

import android.app.Application
import timber.log.Timber

class SignalTestApp: Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}