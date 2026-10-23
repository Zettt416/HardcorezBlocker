package com.hardcorezblocker.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service wajib untuk penanganan quick response SMS ("respond via message") saat panggilan ditolak.
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
