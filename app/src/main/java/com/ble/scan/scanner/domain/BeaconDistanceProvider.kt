package com.ble.scan.scanner.domain

class BeaconDistanceProvider {
    fun calculateEddystoneDistance(rssi: Int, txPower: Int, n: Double = 2.0): Double? {
        if (rssi == 0 || txPower == 0) {
            return null
        }
        val adjustedRssi = if (rssi > 0) -rssi else rssi
        return Math.pow(10.0, (txPower - adjustedRssi) / (10 * n))
    }
    fun calculateIBeaconDistance(rssi: Int, txPower: Int): Double? {
        if (rssi == 0 || txPower == 0) {
            return null
        }
        val ratio = (rssi / txPower).toDouble()
        val positiveRatio = Math.abs(ratio)

        return if (positiveRatio < 1.0) {
            Math.pow(positiveRatio, 10.0)
        } else {
            (0.89976) * Math.pow(positiveRatio, 7.7095) + 0.111
        }
    }
}