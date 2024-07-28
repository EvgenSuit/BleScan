package com.ble.scan.scanner.presentation.ui

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ble.scan.scanner.data.BLEDevice
import com.ble.scan.scanner.domain.BluetoothState
import com.ble.scan.scanner.presentation.BLEViewModel
import com.ble.scan.scanner.presentation.UiState
import com.ble.scan.scanner.utils.ui.LocalSnackbarController
import com.ble.scan.ui.theme.BleScanTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleScannerScreen(viewModel: BLEViewModel = koinViewModel(),
                     modifier: Modifier) {
    val devices by viewModel.devices.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = LocalSnackbarController.current
    LaunchedEffect(viewModel.scanResult.value) {
        snackbar.showSnackbar(viewModel.scanResult.value)
    }
    var permissions = listOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions = permissions.plus(
            listOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
            ),
        )
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions)
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (!permissionsState.allPermissionsGranted) {
            viewModel.clearDevices()
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    if (permissionsState.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            viewModel.observeBluetoothState()
        }
        BleScannerContent(
            scanning = viewModel.scanning.value,
            uiState = uiState,
            devices = devices.devices,
            onAction = viewModel::onControlAction,
            modifier = modifier)
    } else {
        PermissionRequestComposable(modifier = modifier) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
}

@Composable
fun BleScannerContent(
    scanning: Boolean,
    uiState: UiState,
    devices: List<BLEDevice>,
    onAction: (ControlAction) -> Unit,
    modifier: Modifier
) {
    val lazyColumnState = rememberLazyListState()
    val sortDesc = uiState.sortDesc
    val filterAll = uiState.filterAll
    LaunchedEffect(filterAll) {
        lazyColumnState.animateScrollToItem(0)
    }
    val transformedDevices by remember(devices, sortDesc, filterAll) {
        mutableStateOf(devices
            .let { if (filterAll) devices else it.filter { it.isBeacon } }
            .let { if (sortDesc) it.sortedByDescending { it.rssi } else it.sortedBy { it.rssi } })
    }
    val isBluetoothOn = uiState.bluetoothState is BluetoothState.StateOn
    val isBluetoothNone = uiState.bluetoothState is BluetoothState.None
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Controls(
            scanning =  scanning,
            sortDescending = uiState.sortDesc,
            filterAll = uiState.filterAll,
            onAction = onAction)
        if (scanning) SearchIndicator()
        if (isBluetoothOn) {
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                items(transformedDevices, key = {it.address}) { device ->
                    BleDeviceDetails(device = device)
                }
            }
            if (transformedDevices.isEmpty() && !scanning) NothingToShow()
        } else if (!isBluetoothNone) {
            TurnOnBluetoothPrompt()
        }
    }
}


@Composable
@Preview
fun BleScannerScreenPreview() {
    BleScanTheme(darkTheme = true) {
        Surface {
            BleScannerContent(
                scanning = true,
                uiState = UiState(bluetoothState = BluetoothState.StateOn),
                devices = listOf(BLEDevice(
                    name = "dfdf".repeat(10),
                    isBeacon = true
                )),
                onAction = {},
                modifier = Modifier
            )
        }
    }
}