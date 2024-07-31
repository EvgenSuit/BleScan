package com.ble.scan.scanner.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ble.scan.R
import com.ble.scan.scanner.data.BLEDevice

@Composable
fun TurnOnBluetoothPrompt() {
    ConstrainedElement {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp,
                Alignment.CenterVertically),
            modifier = it.fillMaxHeight()
        ) {
            Image(painter = painterResource(id = R.drawable.bluetooth_disabled),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                contentDescription = "Bluetooth",
                modifier = Modifier.size(70.dp))
            Text(text = stringResource(id = R.string.turn_on_bluetooth),
                style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
fun PermissionRequestComposable(
    modifier: Modifier,
    onRequest: () -> Unit,
) {
    ConstrainedElement {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp,
                Alignment.CenterVertically),
            modifier = it
                .then(modifier)
                .fillMaxSize()
        ) {
            Text(text = stringResource(id = R.string.grant_permissions),
                style = MaterialTheme.typography.displayMedium)
            ElevatedButton(onClick = onRequest) {
                Text(stringResource(id = R.string.grant),
                    style = MaterialTheme.typography.displayMedium)
            }
        }
    }
}

@Composable
fun BleDeviceDetails(device: BLEDevice) {
    ConstrainedElement {
        ElevatedCard(
            modifier = it
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp,
                    Alignment.CenterVertically),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
            ) {
                MainDeviceInfo(device = device)
                MacAddressInfo(device = device)
                if (device.isBeacon) {
                    BeaconInfo(device = device)
                }
                ConnectableInfo(isConnectable = device.isConnectable)
            }
        }
    }
}

@Composable
fun MainDeviceInfo(device: BLEDevice) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.info_spacing)),
            modifier = Modifier.weight(0.7f)
        ) {
            Text(stringResource(id = R.string.name),
                style = MaterialTheme.typography.displayMedium)
            Text(device.name ?: stringResource(id = R.string.unknown),
                style = MaterialTheme.typography.displaySmall)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.info_spacing)),
            modifier = Modifier.weight(0.4f)
        ) {
            Text(stringResource(id = R.string.rssi),
                style = MaterialTheme.typography.displayMedium)
            Text("${device.rssi} ${stringResource(id = R.string.db)}",
                style = MaterialTheme.typography.displaySmall)
        }
    }
}

@Composable
fun ConnectableInfo(isConnectable: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(id = if (isConnectable) R.string.is_connectable else R.string.is_not_connectable),
            style = MaterialTheme.typography.displaySmall.copy(
                color = if (isConnectable) Color.Green else MaterialTheme.colorScheme.error
            ))
    }
}

@Composable
fun MacAddressInfo(device: BLEDevice) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.info_spacing)),
    ) {
        Text(stringResource(id = R.string.address),
            style = MaterialTheme.typography.displayMedium)
        Text(device.address,
            style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
fun BeaconInfo(device: BLEDevice) {
    Column(
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.info_spacing)),
    ) {
        Text(stringResource(id = R.string.distance),
            style = MaterialTheme.typography.displayMedium)
        Text("${device.distance} ${stringResource(id = R.string.meters)}",
            style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
fun Controls(
    scanning: Boolean,
    sortDescending: Boolean,
    filterAll: Boolean,
    onAction: (ControlAction) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp,
            Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(5.dp)
    ) {
        ControlButton(
            text = stringResource(id = if (scanning) R.string.stop else R.string.start),
            onClick = {onAction(ControlAction.StartOrStop(scanning))})
        ControlButton(
            text = stringResource(id = if (sortDescending) R.string.descending else R.string.ascending),
            imageVector = if (sortDescending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            onClick = { onAction(ControlAction.Sort(!sortDescending)) }
        )
        ControlButton(
            text = "${stringResource(id = R.string.filter)} ${stringResource(id = if (filterAll) R.string.all else R.string.beacons)}",
            onClick = { onAction(ControlAction.Filter(!filterAll)) })
    }
}
@Composable
fun SearchIndicator() {
    LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("SearchIndicator")
    )
}
@Composable
fun NothingToShow() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(id = R.string.nothing_to_show),
            style = MaterialTheme.typography.displayMedium)

    }
}

@Composable
fun ControlButton(
    text: String,
    imageVector: ImageVector? = null,
    onClick: () -> Unit
) {
    ElevatedButton(onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth()) {
        Column {
            Text(text, style = MaterialTheme.typography.displayMedium)
            if (imageVector != null) {
                Icon(imageVector = imageVector,
                    contentDescription = imageVector.name)
            }
        }
    }
}

@Composable
fun ConstrainedElement(content: @Composable BoxScope.(Modifier) -> Unit) {
    val maxElementWidth = dimensionResource(id = R.dimen.max_device_details_width)
    BoxWithConstraints {
        content(
            Modifier.then(if (this.maxWidth > maxElementWidth)
            Modifier.width(maxElementWidth)
            else Modifier.fillMaxWidth()))
    }
}