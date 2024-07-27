package com.ble.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ble.scan.scanner.presentation.ui.BleScannerScreen
import com.ble.scan.ui.theme.BleScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        setContent {
            BleScanTheme {
                Surface {
                    BleScannerScreen()
                }
            }
        }
    }
}
