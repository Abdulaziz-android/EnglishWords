package com.abdulaziz.englishwords.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abdulaziz.englishwords.database.dao.ItemDao
import com.abdulaziz.englishwords.database.dao.SeenDao
import com.abdulaziz.englishwords.database.dao.WordDao

@Database(entities = [WordEntity::class, SearchItem::class, SeenItem::class], version = 1)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
    abstract fun itemDao(): ItemDao
    abstract fun seenDao(): SeenDao

    companion object{
        private var db: WordDatabase? = null

        fun getInstance(context: Context): WordDatabase {
            if (db == null) {
                db = Room.databaseBuilder(context.applicationContext, WordDatabase::class.java,
                    "my_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
            }
            return db!!
        }
    }

}