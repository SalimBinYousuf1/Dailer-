package com.example.util

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.example.data.model.CountryCode
import java.util.Locale

object PhoneUtils {
    val COUNTRIES = listOf(
        CountryCode("Nepal", "+977", "NP", "🇳🇵"),
        CountryCode("United States", "+1", "US", "🇺🇸"),
        CountryCode("United Kingdom", "+44", "GB", "🇬🇧"),
        CountryCode("India", "+91", "IN", "🇮🇳"),
        CountryCode("Canada", "+1", "CA", "🇨🇦"),
        CountryCode("Australia", "+61", "AU", "🇦🇺"),
        CountryCode("Germany", "+49", "DE", "🇩🇪"),
        CountryCode("France", "+33", "FR", "🇫🇷"),
        CountryCode("Japan", "+81", "JP", "🇯🇵"),
        CountryCode("China", "+86", "CN", "🇨🇳"),
        CountryCode("Brazil", "+55", "BR", "🇧🇷"),
        CountryCode("Mexico", "+52", "MX", "🇲🇽"),
        CountryCode("Spain", "+34", "ES", "🇪🇸"),
        CountryCode("Italy", "+39", "IT", "🇮🇹"),
        CountryCode("Singapore", "+65", "SG", "🇸🇬"),
        CountryCode("United Arab Emirates", "+971", "AE", "🇦🇪")
    )

    fun getDefaultCountryCode(context: Context): CountryCode {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simIso = tm?.simCountryIso?.uppercase(Locale.ROOT)
            val networkIso = tm?.networkCountryIso?.uppercase(Locale.ROOT)
            val detectedIso = when {
                !simIso.isNullOrBlank() -> simIso
                !networkIso.isNullOrBlank() -> networkIso
                else -> Locale.getDefault().country.uppercase(Locale.ROOT)
            }
            COUNTRIES.firstOrNull { it.iso.equals(detectedIso, ignoreCase = true) }
                ?: CountryCode("Nepal", "+977", "NP", "🇳🇵") // Matches reference design visual example if unset
        } catch (_: Exception) {
            CountryCode("Nepal", "+977", "NP", "🇳🇵")
        }
    }

    fun formatDisplayNumber(raw: String): String {
        if (raw.isEmpty()) return ""
        val cleaned = raw.replace(" ", "")
        // Format with clean spacing
        return if (cleaned.length > 7 && !cleaned.startsWith("+")) {
            val sb = StringBuilder()
            for (i in cleaned.indices) {
                if (i == 3 || i == 6 || (i > 6 && (i - 6) % 4 == 0)) {
                    sb.append(" ")
                }
                sb.append(cleaned[i])
            }
            sb.toString()
        } else {
            cleaned
        }
    }

    fun normalizeNumber(raw: String): String {
        return PhoneNumberUtils.stripSeparators(raw) ?: raw.replace("[^0-9+]".toRegex(), "")
    }

    /**
     * T9 mapping: maps letters to dial pad digits
     */
    fun charToT9(c: Char): Char {
        return when (c.lowercaseChar()) {
            'a', 'b', 'c' -> '2'
            'd', 'e', 'f' -> '3'
            'g', 'h', 'i' -> '4'
            'j', 'k', 'l' -> '5'
            'm', 'n', 'o' -> '6'
            'p', 'q', 'r', 's' -> '7'
            't', 'u', 'v' -> '8'
            'w', 'x', 'y', 'z' -> '9'
            else -> c
        }
    }

    fun stringToT9(str: String): String {
        return buildString {
            for (ch in str) {
                if (ch.isLetter()) {
                    append(charToT9(ch))
                } else if (ch.isDigit()) {
                    append(ch)
                }
            }
        }
    }

    fun matchesT9(query: String, name: String, number: String): Boolean {
        if (query.isEmpty()) return true
        val cleanNumber = normalizeNumber(number)
        if (cleanNumber.contains(query)) return true

        // Match name with T9
        val t9Name = stringToT9(name)
        if (t9Name.contains(query)) return true

        // Check word beginnings
        val words = name.split("\\s+".toRegex())
        val initialsT9 = words.mapNotNull { it.firstOrNull()?.let { ch -> charToT9(ch) } }.joinToString("")
        return initialsT9.startsWith(query)
    }
}
