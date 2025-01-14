package com.skripsi.jalu.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    private const val BASE_URL: String = "http://192.168.94.213:3000"

    fun getApiService(token: String? = null): ApiService {
        val client = OkHttpClient.Builder().apply {
            if (token != null) {
                addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(request)
                }
            }
            addInterceptor(HttpLoggingInterceptor().apply {
                level = Level.BODY
            })
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
