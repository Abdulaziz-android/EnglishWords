package com.abdulaziz.englishwords.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchItem(
    @PrimaryKey()
    val name: String,
    val position: Int=0,
)