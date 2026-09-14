package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.DialerDatabase

class DialerApplication : Application() {

    lateinit var database: DialerDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            DialerDatabase::class.java,
            "dialer_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    companion object {
        lateinit var instance: DialerApplication
            private set
    }
}
