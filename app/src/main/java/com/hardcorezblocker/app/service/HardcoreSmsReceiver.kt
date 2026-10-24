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
        val (category, _) = BlockRepository.classifySafetyCategory(
            cleanSender = cleanSender,
            body = completeMessage,
            isContact = isContact,
            isCustomThreat = shouldBlock,
            customReason = reason
        )

        val currentSettings = repository.loadSettings()
        val strings = getAppStrings(currentSettings.language)

        if (shouldBlock) {
            val snippet = if (completeMessage.length > 80) completeMessage.take(77) + "..." else completeMessage

            repository.addLog(
                BlockedLog(
                    senderOrNumber = sender,
                    channel = TargetChannel.SMS_ONLY,
                    reason = reason,
                    contentSnippet = snippet
                )
            )

            if (currentSettings.sendNotificationOnBlock && currentSettings.notifyThreatSms) {
                sendSmsAlertNotification(context, sender, reason, snippet, strings)
            }

            if (isOrderedBroadcast) {
                abortBroadcast()
            }
        } else {
            if (action == Telephony.Sms.Intents.SMS_DELIVER_ACTION || isDefaultSmsApp) {
                repository.writeSmsToInbox(context, sender, completeMessage, timestamp)
            }

            when (category) {
                com.hardcorezblocker.app.data.model.SmsSafetyCategory.OTP_OFFICIAL -> {
                    if (currentSettings.sendNotificationOnBlock && currentSettings.notifyOtpSms) {
                        val otpCode = BlockRepository.OtpDetector.extractOtp(completeMessage)
                        sendOtpNotification(context, sender, completeMessage, otpCode, strings)
                    } else if (isDefaultSmsApp) {
                        sendStandardIncomingSmsNotification(context, sender, completeMessage)
                    }
                }
                com.hardcorezblocker.app.data.model.SmsSafetyCategory.PROMO_MARKETING -> {
                    if (currentSettings.sendNotificationOnBlock && currentSettings.notifyPromoSms) {
                        sendPromoNotification(context, sender, completeMessage, strings)
                    } else if (isDefaultSmsApp) {
                        sendStandardIncomingSmsNotification(context, sender, completeMessage)
                    }
                }
                else -> {
                    if (isDefaultSmsApp) {
                        sendStandardIncomingSmsNotification(context, sender, completeMessage)
                    }
                }
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

        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(strings.notifSmsScamTitle(sender))
            .setContentText(strings.notifSmsScamReason(reason))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(strings.notifSmsScamBigText(sender, reason, snippet))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setNumber(1)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        HardcorezBlockerApp.applyMiuiBadge(notification, 1)
        HardcorezBlockerApp.updateLauncherBadge(context, 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() + 1).toInt(), notification)
    }

    private fun sendOtpNotification(context: Context, sender: String, body: String, otpCode: String?, strings: Strings) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (!otpCode.isNullOrBlank()) "🔑 Kode OTP: $otpCode ($sender)" else "🔑 Kode OTP Resmi: $sender"
        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    private fun sendPromoNotification(context: Context, sender: String, body: String, strings: Strings) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle("📢 Promo: $sender")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }

    private fun sendStandardIncomingSmsNotification(context: Context, sender: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % 10000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HardcorezBlockerApp.CHANNEL_MESSAGES_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(sender)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        HardcorezBlockerApp.applyMiuiBadge(notification, 1)
        HardcorezBlockerApp.updateLauncherBadge(context, 1)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}
