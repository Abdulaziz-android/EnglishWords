package com.abdulaziz.englishwords.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WordEntity(
    @PrimaryKey val word:String,
    val phonetic:String?="",
    val audio_link:String?="",
    val definition:String?="",
    val example:String?="",
    var save:Boolean = false,
)
