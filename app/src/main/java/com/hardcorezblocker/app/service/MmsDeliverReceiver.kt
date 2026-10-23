package com.hardcorezblocker.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver wajib untuk penanganan MMS saat aplikasi memegang peran Default SMS App.
 */
class MmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS handler placeholder sesuai spesifikasi Android Default SMS
    }
}
