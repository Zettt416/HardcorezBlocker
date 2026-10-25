package com.hardcorezblocker.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.hardcorezblocker.app.HardcorezBlockerApp
import com.hardcorezblocker.app.R
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.data.repository.BlockRepository
import com.hardcorezblocker.app.ui.MainActivity
import com.hardcorezblocker.app.ui.i18n.Strings
import com.hardcorezblocker.app.ui.i18n.getAppStrings

class HardcoreSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION &&
            action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
            return
        }

        val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

        if (action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION && isDefaultSmsApp) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val repository = BlockRepository.getInstance(context)

        val fullBody = StringBuilder()
        var sender = ""
        var timestamp = System.currentTimeMillis()

        for (sms in messages) {
            if (sender.isEmpty()) {
                sender = sms.displayOriginatingAddress ?: sms.originatingAddress ?: "Unknown"
                timestamp = sms.timestampMillis
            }
            fullBody.append(sms.displayMessageBody ?: sms.messageBody ?: "")
        }

        val completeMessage = fullBody.toString()

        val (shouldBlock, reason) = repository.evaluateIncomingSms(context, sender, completeMessage)

        val cleanSender = sender.replace("\\s".toRegex(), "")
        val isContact = repository.isNumberInContacts(context, cleanSender)
        val isPromoCustom = shouldBlock && reason.contains("Promo", ignoreCase = true)
        val (category, _) = BlockRepository.classifySafetyCategory(
            cleanSender = cleanSender,
            body = completeMessage,
            isContact = isContact,
            isCustomThreat = shouldBlock && !isPromoCustom,
            customReason = reason
        )

        val currentSettings = repository.loadSettings()
        val strings = getAppStrings(currentSettings.language)

        val snippet = if (completeMessage.length > 80) completeMessage.take(77) + "..." else completeMessage

        if (shouldBlock) {
            repository.addLog(
                BlockedLog(
                    senderOrNumber = sender,
                    channel = TargetChannel.SMS_ONLY,
                    reason = reason,
                    contentSnippet = snippet
                )
            )
        }

        // Semua SMS tetap disimpan ke inbox (terlihat saat app dibuka), yang diatur hanya push-nya.
        if (action == Telephony.Sms.Intents.SMS_DELIVER_ACTION || isDefaultSmsApp) {
            repository.writeSmsToInbox(context, sender, completeMessage, timestamp)
        }

        if (shouldBlock && isOrderedBroadcast) {
            abortBroadcast()
        }

        val notifyEnabled = currentSettings.sendNotificationOnBlock
        when (category) {
            com.hardcorezblocker.app.data.model.SmsSafetyCategory.SPAM_OR_PHISHING -> {
                if (shouldBlock) {
                    if (notifyEnabled && currentSettings.notifyThreatSms) {
                        sendSmsAlertNotification(context, sender, reason, snippet, strings)
                    }
                } else if (isDefaultSmsApp) {
                    sendStandardIncomingSmsNotification(context, sender, completeMessage)
                }
            }
            com.hardcorezblocker.app.data.model.SmsSafetyCategory.OTP_OFFICIAL -> {
                if (notifyEnabled && currentSettings.notifyOtpSms) {
                    val otpCode = BlockRepository.OtpDetector.extractOtp(completeMessage)
                    sendOtpNotification(context, sender, completeMessage, otpCode, strings)
                }
            }
            com.hardcorezblocker.app.data.model.SmsSafetyCategory.PROMO_MARKETING -> {
                if (notifyEnabled && currentSettings.notifyPromoSms) {
                    sendPromoNotification(context, sender, completeMessage, strings)
                }
            }
            else -> {
                if (isDefaultSmsApp) {
                    sendStandardIncomingSmsNotification(context, sender, completeMessage)
                }
            }
        }
    }

    private fun sendSmsAlertNotification(context: Context, sender: String, reason: String, snippet: String, strings: Strings) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TAB", "INBOX")
            putExtra("FILTER_THREATS", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(sender)
            .setContentText("⚠️ $reason — $snippet")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("⚠️ SMS Diblokir ($reason)\n$snippet")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        HardcorezBlockerApp.applyMiuiBadge(notification, 1)
        HardcorezBlockerApp.updateLauncherBadge(context, 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notification)
    }

    private fun sendOtpNotification(context: Context, sender: String, body: String, otpCode: String?, strings: Strings) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TAB", "INBOX")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(sender)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)

        if (!otpCode.isNullOrBlank()) {
            val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_COPY_OTP
                putExtra(NotificationActionReceiver.EXTRA_OTP_CODE, otpCode)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                notifId,
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val copyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_save,
                "Salin $otpCode",
                copyPendingIntent
            ).build()
            builder.addAction(copyAction)
        }

        val notification = builder.build()
        HardcorezBlockerApp.applyMiuiBadge(notification, 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notification)
    }

    private fun sendPromoNotification(context: Context, sender: String, body: String, strings: Strings) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TAB", "INBOX")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(sender)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notification)
    }

    private fun sendStandardIncomingSmsNotification(context: Context, sender: String, body: String) {
        val notifId = (System.currentTimeMillis() % 100000).toInt()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_TAB", "INBOX")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val otpCode = BlockRepository.OtpDetector.extractOtp(body)

        val builder = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(sender)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)

        if (!otpCode.isNullOrBlank()) {
            val copyIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_COPY_OTP
                putExtra(NotificationActionReceiver.EXTRA_OTP_CODE, otpCode)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notifId)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                notifId,
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val copyAction = NotificationCompat.Action.Builder(
                android.R.drawable.ic_menu_save,
                "Salin $otpCode",
                copyPendingIntent
            ).build()
            builder.addAction(copyAction)
        }

        val notification = builder.build()
        HardcorezBlockerApp.applyMiuiBadge(notification, 1)
        HardcorezBlockerApp.updateLauncherBadge(context, 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifId, notification)
    }
}
