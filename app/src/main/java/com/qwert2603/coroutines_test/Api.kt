package com.qwert2603.coroutines_test

import android.util.Log
import kotlinx.coroutines.flow.Flow
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    companion object {
        fun create(): Api = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addNetworkInterceptor(
                        HttpLoggingInterceptor(
                            object : HttpLoggingInterceptor.Logger {
                                override fun log(message: String) {
                                    Log.d("okhttp", message)
                                }
                            }).setLevel(HttpLoggingInterceptor.Level.BODY)
                    )
                    .build()
            )
            .build()
            .create(Api::class.java)
    }

    @GET("posts")
    suspend fun getPosts(@Query("userId") userId: Long): List<Post>
}