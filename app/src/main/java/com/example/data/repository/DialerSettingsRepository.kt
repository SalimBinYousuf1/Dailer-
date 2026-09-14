package com.example.data.repository

import com.example.data.local.BlockedNumberEntity
import com.example.data.local.DialerDao
import com.example.data.local.DialerSettingsEntity
import com.example.data.local.FavoriteContactEntity
import com.example.util.PhoneUtils
import kotlinx.coroutines.flow.Flow

class DialerSettingsRepository(private val dao: DialerDao) {

    val settings: Flow<DialerSettingsEntity?> = dao.getSettings()
    val blockedNumbers: Flow<List<BlockedNumberEntity>> = dao.getAllBlockedNumbers()
    val favorites: Flow<List<FavoriteContactEntity>> = dao.getAllFavorites()

    suspend fun saveSettings(settings: DialerSettingsEntity) {
        dao.saveSettings(settings)
    }

    suspend fun blockNumber(number: String, reason: String = "Spam / Unwanted") {
        val normalized = PhoneUtils.normalizeNumber(number)
        dao.blockNumber(
            BlockedNumberEntity(
                number = number,
                normalizedNumber = normalized,
                reason = reason
            )
        )
    }

    suspend fun unblockNumber(number: String) {
        val normalized = PhoneUtils.normalizeNumber(number)
        dao.unblockNumber(number, normalized)
    }

    suspend fun isBlocked(number: String): Boolean {
        val normalized = PhoneUtils.normalizeNumber(number)
        return dao.isNumberBlocked(number, normalized)
    }

    suspend fun addFavorite(lookupKey: String, name: String, number: String, photoUri: String?) {
        dao.addFavorite(
            FavoriteContactEntity(
                lookupKey = lookupKey,
                name = name,
                number = number,
                photoUri = photoUri
            )
        )
    }

    suspend fun removeFavorite(lookupKey: String) {
        dao.removeFavorite(lookupKey)
    }

    suspend fun isFavorite(lookupKey: String): Boolean {
        return dao.isFavorite(lookupKey)
    }
}
