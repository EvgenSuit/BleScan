package com.ble.scan.scanner.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.ble.scan.scanner.data.BLEDevice
import com.ble.scan.scanner.data.Devices
import com.ble.scan.scanner.utils.CustomResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID


@SuppressLint("MissingPermission")
class BLERepository(
    bluetoothManager: BluetoothManager,
    private val permissionHandler: PermissionHandler,
    bluetoothStateObserver: BluetoothStateObserver,
    private val context: Context
) {
    var scanning = mutableStateOf(false)
    var scanResult = mutableStateOf<CustomResult>(CustomResult.None)
    private val handler = Handler(Looper.getMainLooper())
    private val bluetoothAdapter = bluetoothManager.adapter
    private val SCAN_PERIOD: Long = 10_000
    private val CLEANUP_INTERVAL: Long = 5_000
    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val beaconDistanceProvider = BeaconDistanceProvider()
    val bluetoothState = bluetoothStateObserver.state
    private val _devices = MutableStateFlow(Devices())
    val devices = _devices.asStateFlow()

    private val cleanupJob = CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
            delay(1000)
            if (scanning.value) cleanupOldDevices()
        }
    }
    private fun cleanupOldDevices() {
        val currentTime = System.currentTimeMillis()
        _devices.update {
            val updatedList = it.devices.filter { currentTime - it.lastSeen <= CLEANUP_INTERVAL }
            it.copy(devices = updatedList)
        }
    }
    init {
        cleanupJob.start()
    }
    private val txPowerCharacteristicUUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb")
    private val gattCallback = object : BluetoothGattCallback() {
        var gatt: BluetoothGatt? = null

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("BLERepository", "Connected to GATT server.")
                this.gatt = gatt
                gatt.discoverServices()
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                this.gatt?.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.services.forEach { service ->
                    service.characteristics.forEach { characteristic ->
                        Log.d("BLERepository", "Characteristic found: ${characteristic.uuid}, tx power source: $txPowerCharacteristicUUID")
                        if (characteristic.uuid == txPowerCharacteristicUUID) {
                            gatt.readCharacteristic(characteristic)
                        }
                    }
                }
            } else {
                Log.d("BLERepository", "Failed to discover services.")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.uuid == txPowerCharacteristicUUID) {
                    val txPower = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                    Log.d("BLERepository", "Tx Power Level: $txPower, ${characteristic.uuid}")
                }
            } else {
                Log.d("BLERepository", "Failed to read characteristic: $status")
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            result.device.connectGatt(context, true, gattCallback)
            val device = BLEDevice(
                name = result.device.name,
                address = result.device.address,
                rssi = result.rssi,
                txPower = result.txPower,
                isConnectable = result.isConnectable,
            )
            _devices.update { currentDevices ->
                val updatedDevices = currentDevices.devices.toMutableList()
                val existingDeviceIndex = updatedDevices.indexOfFirst { it.address == device.address }

                if (existingDeviceIndex >= 0) {
                    updatedDevices[existingDeviceIndex] = device
                } else {
                    updatedDevices.add(device)
                }

                currentDevices.copy(devices = updatedDevices.sortedByDescending { it.rssi })
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            scanResult.value = CustomResult.DynamicError("BLE Scan Failed with code $errorCode")
        }
    }

    fun startScanning() {
        if (!scanning.value) {
            handler.postDelayed({ if (scanning.value) stopScanning() }, SCAN_PERIOD)
            if (permissionHandler.checkPermissions() && bluetoothAdapter.isEnabled) {
                scanning.value = true
                handler.postDelayed({
                    val settings = ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build()
                    bluetoothLeScanner.startScan(null, settings, scanCallback)
                }, 1000)
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
        handler.removeCallbacksAndMessages(null)
        bluetoothLeScanner.stopScan(scanCallback)
        bluetoothLeScanner.flushPendingScanResults(scanCallback)
        scanning.value = false

    }
    fun cancelCleanupJob() {
        cleanupJob.cancel()
        gattCallback.gatt?.close()
        gattCallback.gatt?.disconnect()
    }
}