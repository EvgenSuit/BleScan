package com.ble.scan.scanner.domain

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.ble.scan.scanner.data.BLEDevice
import com.ble.scan.scanner.data.Devices
import com.ble.scan.scanner.utils.CustomResult
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser
import com.neovisionaries.bluetooth.ble.advertising.EddystoneEID
import com.neovisionaries.bluetooth.ble.advertising.EddystoneTLM
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL
import com.neovisionaries.bluetooth.ble.advertising.IBeacon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@SuppressLint("MissingPermission")
class BLERepository(
    bluetoothManager: BluetoothManager,
    private val permissionHandler: PermissionHandler,
    bluetoothStateObserver: BluetoothStateObserver
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


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            var iBeacon: IBeacon? = null
            var eddystone: EddystoneUID? = null
            var eddystoneUrl: EddystoneURL? = null
            var eddystoneEid: EddystoneEID? = null
            var distance: Double? = null
            var txPower: Int? = null
            var batteryLevel: Int? = null
            val scanRecordBytes = result.scanRecord?.bytes ?: return
            val structures = ADPayloadParser.getInstance().parse(scanRecordBytes)

            for (structure in structures) {
                when (structure) {
                    is IBeacon -> {
                        iBeacon = structure
                        txPower = structure.power
                        distance = beaconDistanceProvider.calculateIBeaconDistance(result.rssi, structure.power)
                    }
                    is EddystoneUID -> {
                        eddystone = structure
                        txPower = structure.txPower
                        distance = beaconDistanceProvider.calculateEddystoneDistance(result.rssi, structure.txPower)
                    }
                    is EddystoneURL -> {
                        eddystoneUrl = structure
                        txPower = structure.txPower
                        distance = beaconDistanceProvider.calculateEddystoneDistance(result.rssi, structure.txPower)
                    }
                    is EddystoneTLM -> {
                        batteryLevel = calculateBatteryLevel(structure.batteryVoltage)
                    }
                    is EddystoneEID -> {
                        eddystoneEid = structure
                        txPower = structure.txPower
                        distance = beaconDistanceProvider.calculateEddystoneDistance(result.rssi, structure.txPower)
                    }
                }
                if (txPower != null) {
                    break
                }
            }
            if (distance != null) distance = BigDecimal(distance).setScale(2, RoundingMode.HALF_EVEN).toDouble()
            val device = BLEDevice(
                name = result.device.name,
                address = result.device.address,
                rssi = result.rssi,
                txPower = iBeacon?.power ?: eddystone?.txPower ?: eddystoneUrl?.txPower ?: eddystoneEid?.txPower,
                isBeacon = distance != null,
                isConnectable = result.isConnectable,
                distance = distance,
                batteryLevel = batteryLevel
            )
            if (distance != null) {
                Log.d("BLERepository", "Distance: $distance, Battery: ${device.batteryLevel} Power: ${device.txPower}," +
                        " IBeacon: ${iBeacon != null}, Eddystone: ${eddystone != null}")
            }
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

    private fun calculateBatteryLevel(voltage: Int): Int {
        val minVoltage = 1800
        val maxVoltage = 3000
        return ((voltage - minVoltage).toFloat() / (maxVoltage - minVoltage) * 100).toInt().coerceIn(0, 100)
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
                cleanupJob.start()
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
    fun cancelCleanupJob() = cleanupJob.cancel()
}