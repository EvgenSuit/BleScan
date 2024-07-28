package com.ble.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ble.scan.scanner.presentation.ui.BleScannerScreen
import com.ble.scan.scanner.utils.ui.CustomSnackbar
import com.ble.scan.scanner.utils.ui.LocalSnackbarController
import com.ble.scan.scanner.utils.ui.SnackbarController
import com.ble.scan.ui.theme.BleScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarController by remember(snackbarHostState) {
                mutableStateOf(SnackbarController(snackbarHostState, lifecycleScope, applicationContext))
            }
            BleScanTheme {
                Surface {
                    Scaffold(
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) {
                                CustomSnackbar(snackbarData = it) {
                                    snackbarController.dismissSnackbar()
                                }
                            }
                        }
                    ) { padding ->
                        CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
                            BleScannerScreen(
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }
                }
            }
        }
    }
}
