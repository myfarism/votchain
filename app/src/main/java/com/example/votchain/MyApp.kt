package com.example.votchain

import android.app.Application
import com.google.android.play.core.splitcompat.SplitCompat

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SplitCompat.install(this)
    }
}