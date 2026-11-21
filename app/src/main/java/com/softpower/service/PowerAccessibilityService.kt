package com.softpower.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.PowerManager
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class PowerAccessibilityService : AccessibilityService() {

    // ── State ──────────────────────────────────────────
    private var lastVolumeUpTime = 0L

    // ── Lifecycle ──────────────────────────────────────
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    // ── Key Handling ───────────────────────────────────
    override fun onKeyEvent(event: KeyEvent): Boolean {

        if (event.keyCode != KeyEvent.KEYCODE_VOLUME_UP) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (isScreenOn()) return false

        val now = SystemClock.elapsedRealtime()
        val isDoublePress = now - lastVolumeUpTime <= DOUBLE_PRESS_WINDOW_MS
        lastVolumeUpTime = if (isDoublePress) 0L else now

        if (isDoublePress) wakeScreen()
        return isDoublePress
    }

    // ── Actions ────────────────────────────────────────
    private fun isScreenOn(): Boolean =
        (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive

    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        )
        wakeLock.acquire(WAKE_DURATION_MS)
    }

    private fun lockScreen(): Boolean = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)

    // ── Public API ─────────────────────────────────────
    companion object {
        private const val DOUBLE_PRESS_WINDOW_MS = 600L
        private const val WAKE_DURATION_MS = 3000L
        private const val WAKE_LOCK_TAG = "SoftPower:WakeScreen"

        private var instance: PowerAccessibilityService? = null

        fun isRunning(): Boolean = instance != null

        fun requestLockScreen(): Boolean = instance?.lockScreen() ?: false
    }
}