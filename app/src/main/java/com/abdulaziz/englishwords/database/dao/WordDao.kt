package com.abdulaziz.englishwords.database.dao

import androidx.room.*
import com.abdulaziz.englishwords.database.WordEntity

@Dao
interface WordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWord(wordEntity: WordEntity)

    @Insert
    fun insertAll(listEntity: List<WordEntity>)

    @Query("select * from wordentity where word in (:wordS)")
    fun getWordByName(wordS: String): WordEntity

    @Query("select * from wordentity")
    fun getAllWords(): List<WordEntity>

    @Query("select * from wordentity where save = 1")
    fun getAllSavedWords(): List<WordEntity>

    @Query("select exists(select * from wordentity)")
    fun isExist():Boolean

    @Update
    fun updateWord(wordEntity: WordEntity)

}