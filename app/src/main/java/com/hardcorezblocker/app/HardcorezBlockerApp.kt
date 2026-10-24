package com.hardcorezblocker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class HardcorezBlockerApp : Application() {

    companion object {
        const val CHANNEL_BLOCKED_ID = "channel_blocked_events"

        /**
         * Sinkronkan badge angka/titik di icon aplikasi launcher (khususnya Xiaomi MIUI/HyperOS)
         */
        fun updateLauncherBadge(context: Context, count: Int) {
            try {
                val miuiIntent = android.content.Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE").apply {
                    putExtra(
                        "android.intent.extra.update_application_component_name",
                        "${context.packageName}/com.hardcorezblocker.app.ui.MainActivity"
                    )
                    putExtra(
                        "android.intent.extra.update_application_message_numbers",
                        if (count < 0) 0 else count
                    )
                }
                context.sendBroadcast(miuiIntent)
            } catch (e: Exception) {
                // Abaikan jika launcher sistem tidak mendukung broadcast badge
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aktivitas Pemblokiran"
            val descriptionText = "Notifikasi saat ada panggilan atau SMS penipuan yang berhasil digagalkan"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_BLOCKED_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
