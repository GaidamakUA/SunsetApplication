package com.blogspot.androidgaidamak.sunsetapplication

import com.blogspot.androidgaidamak.sunsetapplication.data.DateJsonAdapter
import com.blogspot.androidgaidamak.sunsetapplication.data.SunsetRestApi
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

object ServiceLocator {
    val sunsetApi: SunsetRestApi

    init {
        val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                })
                .build()
        val moshi = Moshi.Builder().add(Date::class.javaObjectType, DateJsonAdapter()).build()
        sunsetApi = Retrofit.Builder()
                .baseUrl("https://api.sunrise-sunset.org")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()
                .create(SunsetRestApi::class.java)
    }
}