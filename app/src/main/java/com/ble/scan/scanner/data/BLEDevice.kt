package com.ble.scan.scanner.data

data class BLEDevice(
    val name: String? = null,
    val address: String = "",
    val rssi: Int = -1,
    val txPower: Int? = null,
    val isBeacon: Boolean = false,
    val distance: Double? = null
)

data class Devices(val devices: List<BLEDevice> = emptyList())