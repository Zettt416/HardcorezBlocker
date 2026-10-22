package com.hardcoreblocker.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.hardcoreblocker.app.HardcoreBlockerApp
import com.hardcoreblocker.app.data.model.BlockedLog
import com.hardcoreblocker.app.data.model.TargetChannel
import com.hardcoreblocker.app.data.repository.BlockRepository
import com.hardcoreblocker.app.ui.MainActivity
import com.hardcoreblocker.app.ui.i18n.Strings
import com.hardcoreblocker.app.ui.i18n.getAppStrings

class HardcoreSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val repository = BlockRepository.getInstance(context)

        // Kelompokkan SMS jika ada pesan panjang (multi-part SMS)
        val fullBody = StringBuilder()
        var sender = ""

        for (sms in messages) {
            if (sender.isEmpty()) {
                sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: "Unknown"
            }
            fullBody.append(sms.displayMessageBody ?: sms.messageBody ?: "")
        }

        val completeMessage = fullBody.toString()

        // Evaluasi isi pesan
        val (shouldBlock, reason) = repository.evaluateIncomingSms(context, sender, completeMessage)

        if (shouldBlock) {
            val snippet = if (completeMessage.length > 80) completeMessage.take(77) + "..." else completeMessage

            // Simpan riwayat SMS spam ke DB
            repository.addLog(
                BlockedLog(
                    senderOrNumber = sender,
                    channel = TargetChannel.SMS_ONLY,
                    reason = reason,
                    contentSnippet = snippet
                )
            )

            // Kirim notifikasi peringatan spam/phishing
            val currentSettings = repository.loadSettings()
            if (currentSettings.sendNotificationOnBlock) {
                val strings = getAppStrings(currentSettings.language)
                sendSmsAlertNotification(context, sender, reason, snippet, strings)
            }

            // Jika memungkinkan di broadcast prioritaskan penghentian (legacy)
            if (isOrderedBroadcast) {
                abortBroadcast()
            }
        }
    }

    private fun sendSmsAlertNotification(context: Context, sender: String, reason: String, snippet: String, strings: Strings) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HardcoreBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(strings.notifSmsScamTitle(sender))
            .setContentText(strings.notifSmsScamReason(reason))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(strings.notifSmsScamBigText(sender, reason, snippet))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() + 1).toInt(), notification)
    }
}
