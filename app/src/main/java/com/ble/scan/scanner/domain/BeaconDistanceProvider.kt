package com.ble.scan.scanner.domain

import android.bluetooth.le.ScanResult
import android.util.Log
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

class BeaconDistanceProvider {
    fun calculateDistance(txPower: Int, rssi: Int): Double? {
        val ratio = rssi / txPower
        if (rssi == 0) {
            return null
        } else if (ratio < 1.0) {
            return Math.pow(ratio.toDouble(), 10.0)
        }
        return (0.89976) * Math.pow(ratio.toDouble(), 7.7095) + 0.111
    }
}