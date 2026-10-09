package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        XtreamProfileEntity::class,
        FavoriteChannelEntity::class,
        RecentStreamEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MaZzeDatabase : RoomDatabase() {
    abstract fun dao(): MaZzeDao

    companion object {
        @Volatile
        private var INSTANCE: MaZzeDatabase? = null

        fun getDatabase(context: Context): MaZzeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaZzeDatabase::class.java,
                    "mazze_iptv_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
