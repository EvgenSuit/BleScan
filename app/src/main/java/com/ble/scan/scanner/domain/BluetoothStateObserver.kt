package com.ble.scan.scanner.domain

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class BluetoothStateObserver(
    private val bluetoothManager: BluetoothManager,
    private val context: Context
) {
    val state = callbackFlow {
        handleBluetoothState(bluetoothManager.adapter.state, ::trySend)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d("BLERepository", "received bluetooth state")
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    handleBluetoothState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR), ::trySend)
                }
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        )

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
    private fun handleBluetoothState(state: Int, trySend: (BluetoothState) -> Unit) {
        when (state) {
            BluetoothAdapter.STATE_ON -> trySend(BluetoothState.StateOn)
            BluetoothAdapter.STATE_OFF -> trySend(BluetoothState.StateOff)
        }
    }
}

sealed class BluetoothState {
    data object StateOn: BluetoothState()
    data object StateOff: BluetoothState()
    data object None: BluetoothState()
}
