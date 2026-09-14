package com.example.telecom

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

object DialerCaller {

    fun placeCall(context: Context, number: String, accountHandle: PhoneAccountHandle? = null) {
        val cleanNumber = number.trim()
        if (cleanNumber.isEmpty()) return

        val hasCallPhonePermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager

        if (hasCallPhonePermission && telecomManager != null) {
            try {
                val uri = Uri.fromParts("tel", cleanNumber, null)
                val extras = android.os.Bundle()
                if (accountHandle != null) {
                    extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
                }
                telecomManager.placeCall(uri, extras)
                return
            } catch (_: SecurityException) {
                // Fallback to ACTION_CALL or ACTION_DIAL
            } catch (_: Exception) {
                // Fallback
            }
        }

        val intentAction = if (hasCallPhonePermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
        val callIntent = Intent(intentAction, Uri.parse("tel:${Uri.encode(cleanNumber)}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (accountHandle != null) {
                putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
            }
        }

        try {
            context.startActivity(callIntent)
        } catch (_: Exception) {
            // Absolute fallback to dialer
            try {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(cleanNumber)}")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            } catch (_: Exception) {
            }
        }
    }

    fun sendSms(context: Context, number: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${Uri.encode(number)}")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openAddContact(context: Context, number: String = "", name: String = "") {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.RawContacts.CONTENT_TYPE
                if (number.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.PHONE, number)
                if (name.isNotEmpty()) putExtra(ContactsContract.Intents.Insert.NAME, name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openEditContact(context: Context, lookupKey: String) {
        try {
            val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
            val intent = Intent(Intent.ACTION_EDIT).apply {
                setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun shareContact(context: Context, name: String, number: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Contact: $name")
                putExtra(Intent.EXTRA_TEXT, "$name: $number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share contact via").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(shareIntent)
        } catch (_: Exception) {
        }
    }

    fun isDefaultDialer(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_DIALER) == true
        } else {
            val tm = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            tm?.defaultDialerPackage == context.packageName
        }
    }

    fun getAvailableSims(context: Context): List<SubscriptionInfo> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return emptyList()

        return try {
            val sm = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            sm?.activeSubscriptionInfoList ?: emptyList()
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
