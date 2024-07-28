package com.ble.scan.scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ble.scan.scanner.data.BleScanDataStore
import com.ble.scan.scanner.domain.BLERepository
import com.ble.scan.scanner.domain.BluetoothState
import com.ble.scan.scanner.presentation.ui.ControlAction
import com.ble.scan.scanner.utils.CoroutineScopeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BLEViewModel(
    private val bleRepository: BLERepository,
    private val bleScanDataStore: BleScanDataStore,
    coroutineScopeProvider: CoroutineScopeProvider
): ViewModel() {
    private val scope = coroutineScopeProvider(viewModelScope)
    val devices = bleRepository.devices
    val scanning = bleRepository.scanning
    val scanResult = bleRepository.scanResult
    private val bluetoothState = bleRepository.bluetoothState
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    init {
        runBlocking {
            _uiState.update { it.copy(
                sortDesc = bleScanDataStore.getSortDesc(),
                filterAll = bleScanDataStore.getFilterAll()
            ) }
        }
    }

    fun onControlAction(action: ControlAction) = scope.launch {
        when (action) {
            is ControlAction.StartOrStop -> {
                bleRepository.apply { if (action.stop) stopScanning() else startScanning() }
            }
            is ControlAction.Sort -> {
                bleScanDataStore.setSort(action.desc)
                _uiState.update { it.copy(sortDesc = action.desc) }
            }
            is ControlAction.Filter -> {
                bleScanDataStore.setFilter(action.all)
                _uiState.update { it.copy(filterAll = action.all) }
            }
        }
    }
    fun clearDevices() = bleRepository.clearDevices()

    fun observeBluetoothState() = scope.launch {
        bluetoothState.collectLatest { bluetoothState ->
            _uiState.update { it.copy(bluetoothState = bluetoothState) }
            if (bluetoothState is BluetoothState.StateOn) {
                bleRepository.startScanning()
            } else {
                bleRepository.clearDevices()
                bleRepository.stopScanning()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleRepository.stopScanning()
        bleRepository.cancelCleanupJob()
    }
}
data class UiState(
    val sortDesc: Boolean = true,
    val filterAll: Boolean = true,
    val bluetoothState: BluetoothState = BluetoothState.None
)