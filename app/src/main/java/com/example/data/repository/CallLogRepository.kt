package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.example.data.model.CallLogItem
import com.example.data.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CallLogRepository(private val context: Context) {

    suspend fun getCallLogs(limit: Int = 100): List<CallLogItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogItem>()

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.PHONE_ACCOUNT_ID
        )

        val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT $limit"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(CallLog.Calls._ID)
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)
                val subIdx = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val number = if (numberIdx >= 0) it.getString(numberIdx) ?: "" else ""
                    val cachedName = if (nameIdx >= 0) it.getString(nameIdx) else null
                    val rawType = if (typeIdx >= 0) it.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE
                    val date = if (dateIdx >= 0) it.getLong(dateIdx) else System.currentTimeMillis()
                    val duration = if (durationIdx >= 0) it.getLong(durationIdx) else 0L
                    val subId = if (subIdx >= 0) it.getString(subIdx)?.toIntOrNull() ?: -1 else -1

                    val callType = when (rawType) {
                        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
                        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
                        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
                        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
                        CallLog.Calls.VOICEMAIL_TYPE -> CallType.VOICEMAIL
                        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
                        else -> CallType.UNKNOWN
                    }

                    list.add(
                        CallLogItem(
                            id = id,
                            number = number,
                            cachedName = cachedName,
                            type = callType,
                            timestamp = date,
                            durationSeconds = duration,
                            subscriptionId = subId
                        )
                    )
                }
            }
        } catch (_: SecurityException) {
            return@withContext emptyList()
        } catch (_: Exception) {
            return@withContext emptyList()
        }

        list
    }

    suspend fun deleteCallLog(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(CallLog.Calls.CONTENT_URI, id)
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun clearAllCallLogs(): Boolean = withContext(Dispatchers.IO) {
        try {
            val rows = context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
            rows > 0
        } catch (_: Exception) {
            false
        }
    }
}
