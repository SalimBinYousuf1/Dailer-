package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "blocked_numbers")
data class BlockedNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val normalizedNumber: String,
    val reason: String = "Spam / Unwanted",
    val blockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_contacts")
data class FavoriteContactEntity(
    @PrimaryKey val lookupKey: String,
    val name: String,
    val number: String,
    val photoUri: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dialer_settings")
data class DialerSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val hapticsEnabled: Boolean = true,
    val dtmfToneEnabled: Boolean = true,
    val countryCode: String = "+1",
    val defaultSimId: Int = -1
)

@Dao
interface DialerDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY blockedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockNumber(blockedNumber: BlockedNumberEntity): Long

    @Query("DELETE FROM blocked_numbers WHERE number = :number OR normalizedNumber = :normalized")
    suspend fun unblockNumber(number: String, normalized: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_numbers WHERE number = :number OR normalizedNumber = :normalized)")
    suspend fun isNumberBlocked(number: String, normalized: String): Boolean

    @Query("SELECT * FROM favorite_contacts ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteContactEntity)

    @Query("DELETE FROM favorite_contacts WHERE lookupKey = :lookupKey")
    suspend fun removeFavorite(lookupKey: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_contacts WHERE lookupKey = :lookupKey)")
    suspend fun isFavorite(lookupKey: String): Boolean

    @Query("SELECT * FROM dialer_settings WHERE id = 1")
    fun getSettings(): Flow<DialerSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: DialerSettingsEntity)
}

@Database(
    entities = [
        BlockedNumberEntity::class,
        FavoriteContactEntity::class,
        DialerSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DialerDatabase : RoomDatabase() {
    abstract fun dialerDao(): DialerDao
}
