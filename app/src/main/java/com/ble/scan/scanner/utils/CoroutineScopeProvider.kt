package com.ble.scan.scanner.utils

import kotlinx.coroutines.CoroutineScope

class CoroutineScopeProvider(private val coroutineScope: CoroutineScope? = null) {
    operator fun invoke(inputCoroutineScope: CoroutineScope) = coroutineScope ?: inputCoroutineScope
}