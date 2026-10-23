package com.hardcorezblocker.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver resmi yang dipanggil sistem ketika aplikasi menjadi Default SMS App.
 */
class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val delegate = HardcoreSmsReceiver()
        delegate.onReceive(context, intent)
    }
}
