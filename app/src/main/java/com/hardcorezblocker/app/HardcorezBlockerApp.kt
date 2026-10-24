package com.hardcorezblocker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class HardcorezBlockerApp : Application() {

    companion object {
        const val CHANNEL_BLOCKED_ID = "channel_blocked_events"
        const val CHANNEL_MESSAGES_ID = "channel_sms_messages"

        fun applyMiuiBadge(notification: android.app.Notification, count: Int) {
            try {
                val field = notification.javaClass.getDeclaredField("extraNotification")
                field.isAccessible = true
                val extraNotification = field.get(notification) ?: return
                val method = extraNotification.javaClass.getDeclaredMethod("setMessageCount", Int::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(extraNotification, if (count < 0) 0 else count)
            } catch (_: Throwable) {
            }
        }

        fun updateLauncherBadge(context: Context, count: Int) {
            val badgeCount = if (count < 0) 0 else count
            val componentName = "${context.packageName}/com.hardcorezblocker.app.ui.MainActivity"

            try {
                val miuiIntent = android.content.Intent("android.intent.action.APPLICATION_MESSAGE_UPDATE").apply {
                    putExtra("android.intent.extra.update_application_component_name", componentName)
                    putExtra("android.intent.extra.update_application_message_numbers", if (badgeCount == 0) "" else badgeCount.toString())
                    putExtra("message_count", badgeCount)
                }
                context.sendBroadcast(miuiIntent)
            } catch (_: Throwable) {
            }

            try {
                val bundle = android.os.Bundle().apply {
                    putString("package", context.packageName)
                    putString("class", "com.hardcorezblocker.app.ui.MainActivity")
                    putInt("badgenumber", badgeCount)
                    putInt("badge_count", badgeCount)
                }
                context.contentResolver.call(
                    android.net.Uri.parse("content://com.miui.launcher.badge.provider/badge"),
                    "setBadge",
                    null,
                    bundle
                )
            } catch (_: Throwable) {
            }
        }

        fun sendTestNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val intent = android.content.Intent(context, com.hardcorezblocker.app.ui.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                999,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val appLogoBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
            } catch (_: Throwable) {
                null
            }

            val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_BLOCKED_ID)
                .setSmallIcon(R.drawable.ic_app_logo)
                .apply { if (appLogoBitmap != null) setLargeIcon(appLogoBitmap) }
                .setContentTitle("🛡️ HardcorezBlocker Aktif")
                .setContentText("Uji coba lencana notifikasi berhasil.")
                .setStyle(
                    androidx.core.app.NotificationCompat.BigTextStyle()
                        .bigText("Uji coba lencana notifikasi dan proteksi aktif. Icon di layar utama akan menampilkan titik/angka selama notifikasi ini ada.")
                )
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setNumber(1)
                .setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_SMALL)

            val notification = builder.build()
            applyMiuiBadge(notification, 1)
            notificationManager.notify(777, notification)
            updateLauncherBadge(context, 1)
        }

        const val THREAT_NOTIFICATION_ID = 8888

        fun syncThreatNotification(context: Context, unreadThreats: List<com.hardcorezblocker.app.data.model.SmsMessageItem>) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (unreadThreats.isEmpty()) {
                notificationManager.cancel(THREAT_NOTIFICATION_ID)
                updateLauncherBadge(context, 0)
                return
            }

            val count = unreadThreats.size
            val firstThreat = unreadThreats.first()
            val title = "⚠️ $count Pesan Bahaya / Scam Terdeteksi"
            val content = if (count == 1) {
                "${firstThreat.sender}: ${firstThreat.threatDetail ?: "Potensi phishing atau tautan berbahaya"}"
            } else {
                "Ada $count pesan dicurigai penipuan/phishing di kotak masuk yang perlu diperiksa."
            }

            val intent = android.content.Intent(context, com.hardcorezblocker.app.ui.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("FILTER_THREATS", true)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                THREAT_NOTIFICATION_ID,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val appLogoBitmap = try {
                android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.ic_app_logo)
            } catch (_: Throwable) {
                null
            }

            val builder = androidx.core.app.NotificationCompat.Builder(context, CHANNEL_BLOCKED_ID)
                .setSmallIcon(R.drawable.ic_app_logo)
                .apply { if (appLogoBitmap != null) setLargeIcon(appLogoBitmap) }
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setNumber(count)
                .setBadgeIconType(androidx.core.app.NotificationCompat.BADGE_ICON_SMALL)

            val notification = builder.build()
            applyMiuiBadge(notification, count)
            notificationManager.notify(THREAT_NOTIFICATION_ID, notification)
            updateLauncherBadge(context, count)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val blockedChannel = NotificationChannel(
                CHANNEL_BLOCKED_ID,
                "Aktivitas Pemblokiran",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi saat ada panggilan atau SMS penipuan yang berhasil digagalkan"
                setShowBadge(true)
            }

            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES_ID,
                "Pesan SMS Masuk",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pesan SMS yang diterima perangkat"
                setShowBadge(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(blockedChannel)
            notificationManager.createNotificationChannel(messagesChannel)
        }
    }
}
