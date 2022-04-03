package com.abdulaziz.englishwords.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SeenItem(
    @PrimaryKey()
    val name: String,
    val position: Int=0,
)