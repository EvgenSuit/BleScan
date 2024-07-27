package com.ble.scan.scanner.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanResult.TX_POWER_NOT_PRESENT
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.ble.scan.scanner.data.BLEDevice
import com.ble.scan.scanner.data.Devices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@SuppressLint("MissingPermission")
class BLERepository(
    bluetoothManager: BluetoothManager,
    private val permissionHandler: PermissionHandler,
    bluetoothStateObserver: BluetoothStateObserver
) {
    var scanning = mutableStateOf(false)
    private val handler = Handler(Looper.getMainLooper())
    private val bluetoothAdapter = bluetoothManager.adapter
    private val SCAN_PERIOD: Long = 10_000
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val beaconDistanceProvider = BeaconDistanceProvider()
    val bluetoothState = bluetoothStateObserver.state
    private val _devices = MutableStateFlow(Devices())
    val devices = _devices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val distance = if (result.txPower != TX_POWER_NOT_PRESENT)
                beaconDistanceProvider.calculateDistance(result.txPower, result.rssi)
            else null
            val device = BLEDevice(
                name = result.device.name,
                address = result.device.address,
                rssi = result.rssi,
                txPower = result.txPower,
                isBeacon = distance != null,
                distance = distance
            )
            _devices.update { it.copy((it.devices + device).distinctBy { it.address }
                .sortedByDescending { it.rssi }) }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.d("BLERepository", "BLE Scan Failed with code $errorCode")
        }
    }

    fun startScanning() {
        if (!scanning.value) {
            handler.postDelayed({ stopScanning() }, SCAN_PERIOD)
            if (permissionHandler.checkPermissions() && bluetoothAdapter.isEnabled) {
                scanning.value = true
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                bluetoothLeScanner?.startScan(null, settings, scanCallback)
            }
        } else {
            stopScanning()
        }
    }
    fun clearDevices() {
        _devices.value = Devices()
        scanning.value = false
    }
    fun stopScanning() {
        scanning.value = false
        bluetoothLeScanner.stopScan(scanCallback)
    }
}