package com.softpower.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.softpower.MainActivity
import com.softpower.R

class SoftPowerService : Service() {

    // ── State ──────────────────────────────────────────
    private lateinit var floatingButton: FloatingButton

    // ── Lifecycle ──────────────────────────────────────
    override fun onCreate() {
        super.onCreate()
        floatingButton = FloatingButton(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        floatingButton.show { PowerAccessibilityService.requestLockScreen() }
        return START_STICKY
    }

    override fun onDestroy() {
        floatingButton.hide()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Foreground Notification ────────────────────────
    private fun startAsForeground() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_power)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(Context.NOTIFICATION_SERVICE)
            .let { it as NotificationManager }
            .createNotificationChannel(channel)
    }

    // ── Constants ──────────────────────────────────────
    companion object {
        private const val CHANNEL_ID = "softpower_service"
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, SoftPowerService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SoftPowerService::class.java))
        }
    }
}