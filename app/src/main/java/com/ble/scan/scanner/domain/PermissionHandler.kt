package com.ble.scan.scanner.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

class PermissionHandler(private val context: Context) {
    companion object {
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun checkPermissions(): Boolean = REQUIRED_PERMISSIONS.all {
        context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }
}