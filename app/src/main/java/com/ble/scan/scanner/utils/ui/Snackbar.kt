package com.ble.scan.scanner.utils.ui

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ble.scan.scanner.utils.CustomResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalSnackbarController = compositionLocalOf<SnackbarController> {
    error("No snackbar controller provided")
}
class SnackbarController(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope,
    private val context: Context,
) {
    fun showSnackbar(result: CustomResult) {
        if (result is CustomResult.DynamicError) {
            coroutineScope.launch {
                // give time for fetch result to update to "InProgress" if retrying
                delay(100)
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(result.error)
            }
        }
    }
    fun dismissSnackbar() {
        snackbarHostState.currentSnackbarData?.dismiss()
    }
}
@Composable
fun CustomSnackbar(
    snackbarData: SnackbarData,
    onDismiss: () -> Unit
) {
    Snackbar(
        action = {
            IconButton(onClick = onDismiss) {
                val icon = Icons.Filled.Clear
                Icon(imageVector = icon,
                    contentDescription = icon.name)
            }
        },
        modifier = Modifier.padding(10.dp)
    ) {
        Text(snackbarData.visuals.message)
    }
}