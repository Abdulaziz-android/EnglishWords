package com.abdulaziz.englishwords.database.dao

import androidx.room.*
import com.abdulaziz.englishwords.database.SearchItem

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(searchItem: SearchItem)

    @Update
    fun updateItem(searchItem: SearchItem)

    @Query("select * from searchitem")
    fun getAllItems():List<SearchItem>

    @Query("select exists(select * from searchitem)")
    fun isExist():Boolean
}