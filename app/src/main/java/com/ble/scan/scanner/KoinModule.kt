package com.ble.scan.scanner

import android.bluetooth.BluetoothManager
import com.ble.scan.scanner.data.BleScanDataStore
import com.ble.scan.scanner.data.dataStore
import com.ble.scan.scanner.domain.BLERepository
import com.ble.scan.scanner.domain.BluetoothStateObserver
import com.ble.scan.scanner.domain.PermissionHandler
import com.ble.scan.scanner.presentation.BLEViewModel
import com.ble.scan.scanner.utils.CoroutineScopeProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { PermissionHandler(androidContext()) }
    single { androidContext().getSystemService(BluetoothManager::class.java) as BluetoothManager }
    single { BluetoothStateObserver(get(), androidContext()) }
    single { BLERepository(get(), get(), get()) }
}

val utilsModule = module {
    single { BleScanDataStore(androidContext().dataStore) }
    single { CoroutineScopeProvider() }
}

val viewModelModule = module {
    single { BLEViewModel(get(), get(), get()) }
}

val appModule = module {
    includes(
        repositoryModule,
        utilsModule,
        viewModelModule)
}