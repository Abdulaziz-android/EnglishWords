package com.abdulaziz.englishwords.database.dao

import androidx.room.*
import com.abdulaziz.englishwords.database.SeenItem

@Dao
interface SeenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(seenItem: SeenItem)

    @Update
    fun updateItem(seenItem: SeenItem)

    @Query("select * from seenitem")
    fun getAllItems():List<SeenItem>

    @Query("select exists(select * from seenitem)")
    fun isExist():Boolean

    @Delete
    fun deleteAll(list: List<SeenItem>)
}