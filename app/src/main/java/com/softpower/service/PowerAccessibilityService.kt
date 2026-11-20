package com.softpower.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class PowerAccessibilityService : AccessibilityService() {

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

    // ── Actions ────────────────────────────────────────
    private fun lockScreen(): Boolean = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)

    // ── Public API ─────────────────────────────────────
    companion object {
        private var instance: PowerAccessibilityService? = null

        fun isRunning(): Boolean = instance != null

        fun requestLockScreen(): Boolean = instance?.lockScreen() ?: false
    }
}