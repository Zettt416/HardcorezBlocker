package com.hardcoreblocker.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import com.hardcoreblocker.app.HardcoreBlockerApp
import com.hardcoreblocker.app.data.model.BlockedLog
import com.hardcoreblocker.app.data.model.TargetChannel
import com.hardcoreblocker.app.data.repository.BlockRepository
import com.hardcoreblocker.app.ui.MainActivity
import com.hardcoreblocker.app.ui.i18n.Strings
import com.hardcoreblocker.app.ui.i18n.getAppStrings

class HardcoreCallScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        // Cek arah panggilan (hanya proses panggilan masuk)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (details.callDirection != Call.Details.DIRECTION_INCOMING) {
                allowCall(details)
                return
            }
        }

        val repository = BlockRepository.getInstance(applicationContext)
        val rawNumber = extractPhoneNumber(details)

        // Evaluasi panggilan secara instan
        val (shouldBlock, reason) = repository.evaluateIncomingCall(applicationContext, rawNumber)

        if (shouldBlock) {
            val displayNum = rawNumber ?: "Unknown / Hidden"
            
            // Catat ke log riwayat pemblokiran lokal
            repository.addLog(
                BlockedLog(
                    senderOrNumber = displayNum,
                    channel = TargetChannel.CALL_ONLY,
                    reason = reason,
                    contentSnippet = null
                )
            )

            // Kirim notifikasi jika pengguna mengaktifkannya di pengaturan
            val currentSettings = repository.loadSettings()
            if (currentSettings.sendNotificationOnBlock) {
                val strings = getAppStrings(currentSettings.language)
                sendBlockedNotification(displayNum, reason, strings)
            }

            // Eksekusi pemutusan panggilan di level OS Android Telecom
            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)
                .setSkipCallLog(false)
                .build()

            respondToCall(details, response)
        } else {
            allowCall(details)
        }
    }

    private fun allowCall(details: Call.Details) {
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setSkipNotification(false)
            .build()
        respondToCall(details, response)
    }

    private fun extractPhoneNumber(details: Call.Details): String? {
        val handle = details.handle
        if (handle != null) {
            val part = handle.schemeSpecificPart
            if (!part.isNullOrBlank()) return part
        }

        val extras = details.intentExtras
        if (extras != null) {
            val addr = extras.getString("android.telecom.extra.INCOMING_CALL_ADDRESS")
            if (!addr.isNullOrBlank()) return addr
        }

        return null
    }

    private fun sendBlockedNotification(number: String, reason: String, strings: Strings) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, HardcoreBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(android.R.drawable.stat_notify_missed_call)
            .setContentTitle(strings.notifCallBlockedTitle(number))
            .setContentText(strings.notifCallBlockedReason(reason))
            .setStyle(NotificationCompat.BigTextStyle().bigText(strings.notifCallBlockedBigText(number, reason)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
