package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import com.example.data.model.ContactItem
import com.example.util.PhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {

    suspend fun getContacts(query: String = ""): List<ContactItem> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<String, ContactBuilder>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"

        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val lookupIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val starredIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val lookupKey = if (lookupIdx >= 0) it.getString(lookupIdx) ?: id.toString() else id.toString()
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "Unknown" else "Unknown"
                    val number = if (numberIdx >= 0) it.getString(numberIdx) ?: "" else ""
                    val photoUri = if (photoIdx >= 0) it.getString(photoIdx) else null
                    val isStarred = if (starredIdx >= 0) it.getInt(starredIdx) == 1 else false

                    if (number.isNotBlank()) {
                        val existing = contactsMap.getOrPut(lookupKey) {
                            ContactBuilder(id, lookupKey, name, photoUri, isStarred)
                        }
                        if (!existing.numbers.contains(number)) {
                            existing.numbers.add(number)
                        }
                    }
                }
            }
        } catch (_: SecurityException) {
            return@withContext emptyList()
        } catch (_: Exception) {
            return@withContext emptyList()
        }

        val allContacts = contactsMap.values.map {
            ContactItem(
                id = it.id,
                lookupKey = it.lookupKey,
                displayName = it.name,
                numbers = it.numbers,
                primaryNumber = it.numbers.firstOrNull() ?: "",
                photoUri = it.photoUri,
                isStarred = it.isStarred
            )
        }

        if (query.isBlank()) {
            allContacts
        } else {
            allContacts.filter { contact ->
                PhoneUtils.matchesT9(query, contact.displayName, contact.primaryNumber) ||
                    contact.numbers.any { PhoneUtils.matchesT9(query, contact.displayName, it) }
            }
        }
    }

    suspend fun deleteContact(lookupKey: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (_: Exception) {
            false
        }
    }

    private data class ContactBuilder(
        val id: Long,
        val lookupKey: String,
        val name: String,
        val photoUri: String?,
        val isStarred: Boolean,
        val numbers: MutableList<String> = mutableListOf()
    )
}
