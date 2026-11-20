package com.softpower.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.softpower.R

class FloatingButton(private val context: Context) {

    // ── Dependencies ───────────────────────────────────
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    // ── State ──────────────────────────────────────────
    private var rootView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // ── Public API ─────────────────────────────────────
    fun show(onClick: () -> Unit) {
        if (rootView != null) return
        val params = createLayoutParams()
        val view = createView(params, onClick)
        runCatching { windowManager.addView(view, params) }
            .onSuccess {
                rootView = view
                layoutParams = params
            }
    }

    fun hide() {
        val view = rootView ?: return
        runCatching { windowManager.removeView(view) }
        rootView = null
        layoutParams = null
    }

    // ── View Creation ──────────────────────────────────
    private fun createView(params: WindowManager.LayoutParams, onClick: () -> Unit): View {
        val sizePx = dpToPx(BUTTON_SIZE_DP)
        val iconPx = dpToPx(ICON_SIZE_DP)

        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_power)
            layoutParams = FrameLayout.LayoutParams(iconPx, iconPx, Gravity.CENTER)
        }

        return FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
            setBackgroundResource(R.drawable.bg_floating_button)
            contentDescription = context.getString(R.string.floating_button_description)
            alpha = ACTIVE_ALPHA
            addView(icon)
            setOnTouchListener(DragTouchListener(params, onClick))
        }
    }

    private fun createLayoutParams() = WindowManager.LayoutParams(
        dpToPx(BUTTON_SIZE_DP),
        dpToPx(BUTTON_SIZE_DP),
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = context.resources.displayMetrics.widthPixels - dpToPx(BUTTON_SIZE_DP)
        y = context.resources.displayMetrics.heightPixels / 3
    }

    // ── Drag Handling ──────────────────────────────────
    private inner class DragTouchListener(
        private val params: WindowManager.LayoutParams,
        private val onClick: () -> Unit
    ) : View.OnTouchListener {

        private var startX = 0
        private var startY = 0
        private var touchX = 0f
        private var touchY = 0f
        private var isDragging = false

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> recordStart(event)
                MotionEvent.ACTION_MOVE -> handleMove(view, event)
                MotionEvent.ACTION_UP -> handleRelease(view)
            }
            return true
        }

        private fun recordStart(event: MotionEvent) {
            startX = params.x
            startY = params.y
            touchX = event.rawX
            touchY = event.rawY
            isDragging = false
        }

        private fun handleMove(view: View, event: MotionEvent) {
            val deltaX = (event.rawX - touchX).toInt()
            val deltaY = (event.rawY - touchY).toInt()
            if (!isDragging && kotlin.math.hypot(deltaX.toFloat(), deltaY.toFloat()) < DRAG_THRESHOLD_PX) return
            isDragging = true
            params.x = startX + deltaX
            params.y = startY + deltaY
            windowManager.updateViewLayout(view, params)
        }

        private fun handleRelease(view: View) {
            if (!isDragging) {
                onClick()
                return
            }
            snapToEdge(view)
        }

        private fun snapToEdge(view: View) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val buttonWidth = dpToPx(BUTTON_SIZE_DP)
            val centerX = params.x + buttonWidth / 2
            params.x = if (centerX < screenWidth / 2) 0 else screenWidth - buttonWidth
            windowManager.updateViewLayout(view, params)
        }
    }

    // ── Helpers ────────────────────────────────────────
    private fun dpToPx(dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()

    // ── Constants ──────────────────────────────────────
    private companion object {
        const val BUTTON_SIZE_DP = 48
        const val ICON_SIZE_DP = 28
        const val ACTIVE_ALPHA = 0.9f
        const val DRAG_THRESHOLD_PX = 12f
    }
}