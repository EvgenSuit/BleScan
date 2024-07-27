package com.ble.scan.scanner.presentation.ui

sealed class ControlAction {
    data class StartOrStop(val stop: Boolean = true): ControlAction()
    data class Sort(val desc: Boolean = true): ControlAction()
    data class Filter(val all: Boolean = true): ControlAction()
}