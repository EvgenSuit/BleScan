package com.ble.scan.scanner.domain

class BeaconDistanceProvider {
    fun calculateDistance(rssi: Int, txPower: Int, n: Double = 2.0): Double? {
        if (rssi == 0 || txPower == 0) {
            return null
        }
        val adjustedRssi = if (rssi > 0) -rssi else rssi
        return Math.pow(10.0, (txPower - adjustedRssi) / (10 * n))
    }
}