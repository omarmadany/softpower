package com.softpower

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.ImageViewCompat
import android.content.res.ColorStateList

class MainActivity : AppCompatActivity() {

    // ── Views ──────────────────────────────────────────
    private lateinit var powerButton: FrameLayout
    private lateinit var powerIcon: ImageView

    // ── State ──────────────────────────────────────────
    private var isEnabled: Boolean = false

    // ── Lifecycle ──────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        isEnabled = loadEnabledState()
        renderState()
        powerButton.setOnClickListener { toggleState() }
    }

    // ── View Setup ─────────────────────────────────────
    private fun bindViews() {
        powerButton = findViewById(R.id.powerButton)
        powerIcon = findViewById(R.id.powerIcon)
    }

    // ── State Handling ─────────────────────────────────
    private fun toggleState() {
        isEnabled = !isEnabled
        saveEnabledState(isEnabled)
        renderState()
    }

    private fun renderState() {
        val backgroundRes = if (isEnabled) R.drawable.bg_power_on else R.drawable.bg_power_off
        val iconColorRes = if (isEnabled) R.color.white else R.color.power_off_icon

        powerButton.setBackgroundResource(backgroundRes)
        ImageViewCompat.setImageTintList(
            powerIcon,
            ColorStateList.valueOf(ContextCompat.getColor(this, iconColorRes))
        )
    }

    // ── Persistence ────────────────────────────────────
    private fun loadEnabledState(): Boolean =
        getPreferences(Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    private fun saveEnabledState(enabled: Boolean) {
        getPreferences(Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private companion object {
        const val KEY_ENABLED = "softpower_enabled"
    }
}