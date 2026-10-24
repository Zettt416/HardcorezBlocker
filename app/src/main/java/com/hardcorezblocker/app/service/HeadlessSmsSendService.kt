package com.hardcorezblocker.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hardcorezblocker.app.data.repository.BlockRepository

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action == "android.intent.action.RESPOND_VIA_MESSAGE") {
            val uri = intent.data
            val recipient = uri?.schemeSpecificPart
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!recipient.isNullOrBlank() && !text.isNullOrBlank()) {
                BlockRepository.getInstance(applicationContext).sendSms(applicationContext, recipient, text)
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
