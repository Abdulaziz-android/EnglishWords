package com.abdulaziz.englishwords.models

data class WordItem(
    val meanings: List<Meaning>?=null,
    val origin: String="",
    val phonetic: String="",
    val phonetics: List<Phonetic>?=null,
    val word: String=""
)