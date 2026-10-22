package com.hardcoreblocker.app.data.model

import java.util.UUID

data class BlockedLog(
    val id: String = UUID.randomUUID().toString(),
    val senderOrNumber: String,
    val channel: TargetChannel,
    val reason: String,
    val contentSnippet: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
