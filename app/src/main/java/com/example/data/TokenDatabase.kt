package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TokenEntity::class], version = 1, exportSchema = false)
abstract class TokenDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: TokenDatabase? = null

        fun getDatabase(context: Context): TokenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TokenDatabase::class.java,
                    "token_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
