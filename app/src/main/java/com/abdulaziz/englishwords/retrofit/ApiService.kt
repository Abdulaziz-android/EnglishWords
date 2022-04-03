package com.abdulaziz.englishwords.retrofit

import com.abdulaziz.englishwords.models.Word
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("{word_name}")
    suspend fun getWord(@Path("word_name") word:String):Word

}