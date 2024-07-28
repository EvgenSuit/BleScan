package com.ble.scan.scanner.utils

sealed class CustomResult {
    data object None: CustomResult()
    data object InProgress: CustomResult()
    data object Success: CustomResult()
    data class DynamicError(val error: String): CustomResult()
}