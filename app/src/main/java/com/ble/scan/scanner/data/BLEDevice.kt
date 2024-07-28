package com.ble.scan.scanner.data

data class BLEDevice(
    val name: String? = null,
    val address: String = "",
    val rssi: Int = -1,
    val txPower: Int? = null,
    val isConnectable: Boolean = false,
    val isBeacon: Boolean = false,
    val distance: Double? = null,
    val batteryLevel: Int? = null,
    val lastSeen: Long = System.currentTimeMillis()
)

data class Devices(val devices: List<BLEDevice> = emptyList())