package com.ble.scan

import android.app.Application
import com.ble.scan.scanner.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BleScanApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BleScanApplication)
            modules(appModule)
        }
    }
}