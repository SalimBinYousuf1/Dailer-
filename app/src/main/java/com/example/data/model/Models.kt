package com.example.data.model

data class ContactItem(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val numbers: List<String>,
    val primaryNumber: String,
    val photoUri: String? = null,
    val isStarred: Boolean = false
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    VOICEMAIL,
    BLOCKED,
    UNKNOWN
}

data class CallLogItem(
    val id: Long,
    val number: String,
    val cachedName: String?,
    val type: CallType,
    val timestamp: Long,
    val durationSeconds: Long,
    val subscriptionId: Int = -1,
    val photoUri: String? = null
)

data class CountryCode(
    val name: String,
    val code: String, // e.g. "+977", "+1"
    val iso: String, // e.g. "NP", "US"
    val flag: String
)
