package com.hardcorezblocker.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hardcorezblocker.app.data.repository.BlockRepository

/**
 * Membatalkan notifikasi SMS spam/promo yang dimunculkan aplikasi pesan lain
 * (Google Messages, Pesan MIUI, dll) agar toggle notifikasi di app ini berlaku
 * walaupun HardcorezBlocker belum jadi aplikasi SMS utama.
 */
class SmsNotificationFilterService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName !in MESSAGING_PACKAGES) return
        val n = sbn.notification
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val extras = n.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val body = big.ifEmpty { extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty() }
        if (title.isEmpty() || body.isEmpty()) return

        val repo = BlockRepository.getInstance(applicationContext)
        if (repo.shouldSuppressMessagingNotification(applicationContext, title, body)) {
            cancelNotification(sbn.key)
        }
    }

    companion object {
        private val MESSAGING_PACKAGES = setOf(
            "com.google.android.apps.messaging",
            "com.android.mms",
            "com.miui.mms",
            "com.samsung.android.messaging"
        )
    }
}
