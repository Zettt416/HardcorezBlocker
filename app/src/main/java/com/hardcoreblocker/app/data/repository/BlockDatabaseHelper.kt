package com.hardcoreblocker.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hardcoreblocker.app.data.model.BlockRule
import com.hardcoreblocker.app.data.model.BlockedLog
import com.hardcoreblocker.app.data.model.RuleType
import com.hardcoreblocker.app.data.model.TargetChannel

class BlockDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "hardcore_blocker.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_RULES = "block_rules"
        const val TABLE_LOGS = "block_logs"

        // Rules Columns
        const val COL_RULE_ID = "id"
        const val COL_RULE_PATTERN = "pattern"
        const val COL_RULE_TYPE = "type"
        const val COL_RULE_CHANNEL = "channel"
        const val COL_RULE_LABEL = "label"
        const val COL_RULE_ENABLED = "is_enabled"
        const val COL_RULE_CREATED_AT = "created_at"

        // Logs Columns
        const val COL_LOG_ID = "id"
        const val COL_LOG_SENDER = "sender_number"
        const val COL_LOG_CHANNEL = "channel"
        const val COL_LOG_REASON = "reason"
        const val COL_LOG_SNIPPET = "snippet"
        const val COL_LOG_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createRulesTable = """
            CREATE TABLE $TABLE_RULES (
                $COL_RULE_ID TEXT PRIMARY KEY,
                $COL_RULE_PATTERN TEXT NOT NULL,
                $COL_RULE_TYPE TEXT NOT NULL,
                $COL_RULE_CHANNEL TEXT NOT NULL,
                $COL_RULE_LABEL TEXT,
                $COL_RULE_ENABLED INTEGER NOT NULL DEFAULT 1,
                $COL_RULE_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        val createLogsTable = """
            CREATE TABLE $TABLE_LOGS (
                $COL_LOG_ID TEXT PRIMARY KEY,
                $COL_LOG_SENDER TEXT NOT NULL,
                $COL_LOG_CHANNEL TEXT NOT NULL,
                $COL_LOG_REASON TEXT NOT NULL,
                $COL_LOG_SNIPPET TEXT,
                $COL_LOG_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createRulesTable)
        db.execSQL(createLogsTable)

        // Seed initial default scam & fraud defense rules
        insertDefaultRule(db, "+234", RuleType.PREFIX, TargetChannel.CALL_ONLY, "Panggilan Internasional Spam (+234)")
        insertDefaultRule(db, "+1", RuleType.PREFIX, TargetChannel.CALL_ONLY, "Nomor VoIP Asing (+1)")
        insertDefaultRule(db, "+44", RuleType.PREFIX, TargetChannel.CALL_ONLY, "Nomor Asing (+44)")
        insertDefaultRule(db, "pinjol", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Penawaran Pinjaman Online")
        insertDefaultRule(db, "pinjaman online", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Spam Pinjol")
        insertDefaultRule(db, "menang undian", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Modus Undian Palsu")
        insertDefaultRule(db, "selamat anda mendapatkan", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Modus Hadiah Palsu")
        insertDefaultRule(db, "surat tilang", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Modus Malware APK Surat Tilang")
        insertDefaultRule(db, "paket kurir", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Modus Malware APK Kurir Paket")
        insertDefaultRule(db, "klaim hadiah", RuleType.KEYWORD, TargetChannel.SMS_ONLY, "Phishing Klaim Hadiah")
    }

    private fun insertDefaultRule(
        db: SQLiteDatabase,
        pattern: String,
        type: RuleType,
        channel: TargetChannel,
        label: String
    ) {
        val cv = ContentValues().apply {
            put(COL_RULE_ID, java.util.UUID.randomUUID().toString())
            put(COL_RULE_PATTERN, pattern)
            put(COL_RULE_TYPE, type.name)
            put(COL_RULE_CHANNEL, channel.name)
            put(COL_RULE_LABEL, label)
            put(COL_RULE_ENABLED, 1)
            put(COL_RULE_CREATED_AT, System.currentTimeMillis())
        }
        db.insert(TABLE_RULES, null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RULES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGS")
        onCreate(db)
    }
}
