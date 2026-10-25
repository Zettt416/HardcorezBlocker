package com.hardcorezblocker.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Menerima aksi langsung dari notifikasi (misal: Salin Kode OTP).
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            ACTION_COPY_OTP -> {
                val otpCode = intent.getStringExtra(EXTRA_OTP_CODE)
                val notifId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

                if (!otpCode.isNullOrBlank()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Kode OTP", otpCode)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "Kode OTP $otpCode berhasil disalin", Toast.LENGTH_SHORT).show()

                    if (notifId != -1) {
                        val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notifManager.cancel(notifId)
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_COPY_OTP = "com.hardcorezblocker.app.ACTION_COPY_OTP"
        const val EXTRA_OTP_CODE = "extra_otp_code"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
