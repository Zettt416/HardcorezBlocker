package com.hardcorezblocker.app.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import com.hardcorezblocker.app.HardcorezBlockerApp
import com.hardcorezblocker.app.R
import com.hardcorezblocker.app.data.model.BlockedLog
import com.hardcorezblocker.app.data.model.TargetChannel
import com.hardcorezblocker.app.data.repository.BlockRepository
import com.hardcorezblocker.app.ui.MainActivity
import com.hardcorezblocker.app.ui.i18n.Strings
import com.hardcorezblocker.app.ui.i18n.getAppStrings

class HardcoreCallScreeningService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (details.callDirection != Call.Details.DIRECTION_INCOMING) {
                allowCall(details)
                return
            }
        }

        val repository = BlockRepository.getInstance(applicationContext)
        val rawNumber = extractPhoneNumber(details)

        val (shouldBlock, reason) = repository.evaluateIncomingCall(applicationContext, rawNumber)

        if (shouldBlock) {
            val displayNum = rawNumber ?: "Unknown / Hidden"

            repository.addLog(
                BlockedLog(
                    senderOrNumber = displayNum,
                    channel = TargetChannel.CALL_ONLY,
                    reason = reason,
                    contentSnippet = null
                )
            )

            val currentSettings = repository.loadSettings()
            if (currentSettings.sendNotificationOnBlock && currentSettings.notifyBlockedCalls) {
                val strings = getAppStrings(currentSettings.language)
                sendBlockedNotification(displayNum, reason, strings)
            }

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

        val notification = NotificationCompat.Builder(this, HardcorezBlockerApp.CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.drawable.ic_app_logo)
            .setContentTitle(strings.notifCallBlockedTitle(number))
            .setContentText(strings.notifCallBlockedReason(reason))
            .setStyle(NotificationCompat.BigTextStyle().bigText(strings.notifCallBlockedBigText(number, reason)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setNumber(1)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .build()

        HardcorezBlockerApp.applyMiuiBadge(notification, 1)
        HardcorezBlockerApp.updateLauncherBadge(this, 1)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
