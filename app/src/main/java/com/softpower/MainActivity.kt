package com.softpower

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.widget.ImageViewCompat
import com.softpower.service.SoftPowerService
import com.softpower.util.PermissionHelper

class MainActivity : AppCompatActivity() {

    // ── Views ──────────────────────────────────────────
    private lateinit var powerButton: FrameLayout
    private lateinit var powerIcon: ImageView
    private lateinit var pulseRing: View

    // ── State ──────────────────────────────────────────
    private var isPowerOn: Boolean = false
    private var colorAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null

    // ── Permission Launchers ───────────────────────────
    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) showToast(R.string.permission_notifications_denied)
            continueEnabling()
        }

    // ── Lifecycle ──────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindViews()
        powerButton.setOnClickListener { onPowerButtonClicked() }
    }

    override fun onResume() {
        super.onResume()
        syncStateWithPermissions()
    }

    override fun onDestroy() {
        colorAnimator?.cancel()
        pulseAnimator?.cancel()
        super.onDestroy()
    }

    // ── View Setup ─────────────────────────────────────
    private fun bindViews() {
        powerButton = findViewById(R.id.powerButton)
        powerIcon = findViewById(R.id.powerIcon)
        pulseRing = findViewById(R.id.pulseRing)
    }

    // ── Click Handling ─────────────────────────────────
    private fun onPowerButtonClicked() {
        if (isPowerOn) {
            setPowerState(false)
            return
        }
        startEnableFlow()
    }

    // ── Enable Flow ────────────────────────────────────
    private fun startEnableFlow() {
        if (!PermissionHelper.hasNotificationPermission(this)) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        continueEnabling()
    }

    private fun continueEnabling() {
        if (!PermissionHelper.canDrawOverlays(this)) {
            showToast(R.string.permission_overlay_needed)
            startActivity(PermissionHelper.overlaySettingsIntent(this))
            return
        }
        if (!PermissionHelper.isAccessibilityEnabled()) {
            showToast(R.string.permission_accessibility_needed)
            startActivity(PermissionHelper.accessibilitySettingsIntent())
            return
        }
        setPowerState(true)
    }

    // ── State Handling ─────────────────────────────────
    private fun setPowerState(enabled: Boolean) {
        isPowerOn = enabled
        savePowerState(enabled)
        if (enabled) SoftPowerService.start(this) else SoftPowerService.stop(this)
        playPressAnimation()
        animateColors(enabled)
        updatePulse(enabled)
    }

    private fun syncStateWithPermissions() {
        val shouldBeOn = loadPowerState() && PermissionHelper.hasAllRequiredPermissions(this)
        isPowerOn = shouldBeOn
        savePowerState(shouldBeOn)
        if (shouldBeOn) SoftPowerService.start(this)
        renderStateInstantly()
    }

    // ── Rendering ──────────────────────────────────────
    private fun renderStateInstantly() {
        applyColors(backgroundColor(isPowerOn), iconColor(isPowerOn))
        updatePulse(isPowerOn)
    }

    private fun applyColors(backgroundColor: Int, iconColor: Int) {
        val background = powerButton.background.mutate() as GradientDrawable
        background.setColor(backgroundColor)
        ImageViewCompat.setImageTintList(powerIcon, ColorStateList.valueOf(iconColor))
    }

    private fun backgroundColor(on: Boolean): Int =
        ContextCompat.getColor(this, if (on) R.color.teal_primary else R.color.power_off_gray)

    private fun iconColor(on: Boolean): Int =
        ContextCompat.getColor(this, if (on) R.color.white else R.color.power_off_icon)

    // ── Animations ─────────────────────────────────────
    private fun playPressAnimation() {
        powerButton.animate().cancel()
        powerButton.scaleX = PRESSED_SCALE
        powerButton.scaleY = PRESSED_SCALE
        powerButton.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(PRESS_DURATION_MS)
            .setInterpolator(OvershootInterpolator(OVERSHOOT_TENSION))
            .start()
    }

    private fun animateColors(on: Boolean) {
        colorAnimator?.cancel()

        val startBackground = backgroundColor(!on)
        val endBackground = backgroundColor(on)
        val startIcon = iconColor(!on)
        val endIcon = iconColor(on)
        val evaluator = ArgbEvaluator()

        colorAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = COLOR_DURATION_MS
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                applyColors(
                    evaluator.evaluate(fraction, startBackground, endBackground) as Int,
                    evaluator.evaluate(fraction, startIcon, endIcon) as Int
                )
            }
            start()
        }
    }

    private fun updatePulse(on: Boolean) {
        pulseAnimator?.cancel()
        if (!on) {
            fadeOutPulse()
            return
        }
        startPulse()
    }

    private fun startPulse() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = PULSE_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                val scale = 1f + (PULSE_MAX_SCALE - 1f) * fraction
                pulseRing.scaleX = scale
                pulseRing.scaleY = scale
                pulseRing.alpha = 1f - fraction
            }
            start()
        }
    }

    private fun fadeOutPulse() {
        pulseRing.animate()
            .alpha(0f)
            .setDuration(PRESS_DURATION_MS)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    pulseRing.scaleX = 1f
                    pulseRing.scaleY = 1f
                }
            })
            .start()
    }

    // ── Persistence ────────────────────────────────────
    private fun loadPowerState(): Boolean =
        getPreferences(Context.MODE_PRIVATE).getBoolean(KEY_POWER_ENABLED, false)

    private fun savePowerState(enabled: Boolean) {
        getPreferences(Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_POWER_ENABLED, enabled)
            .apply()
    }

    // ── Helpers ────────────────────────────────────────
    private fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_LONG).show()
    }

    // ── Constants ──────────────────────────────────────
    private companion object {
        const val KEY_POWER_ENABLED = "softpower_enabled"
        const val PRESSED_SCALE = 0.88f
        const val OVERSHOOT_TENSION = 2.5f
        const val PRESS_DURATION_MS = 350L
        const val COLOR_DURATION_MS = 300L
        const val PULSE_DURATION_MS = 1600L
        const val PULSE_MAX_SCALE = 1.6f
    }
}