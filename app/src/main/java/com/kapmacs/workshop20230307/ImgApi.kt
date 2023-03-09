package com.kapmacs.workshop20230307

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming

interface ImgApi {

    @Streaming
    @GET("475403598977695759/1051842700363706368/20221209_134634.jpg")
    suspend fun downloadImg(): Response<ResponseBody>

    companion object{
        val instance by lazy{
            Retrofit.Builder()
                .baseUrl("https://cdn.discordapp.com/attachments/")
                .build()
                .create(ImgApi::class.java)
        }
    }
}