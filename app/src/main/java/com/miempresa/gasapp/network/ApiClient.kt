package com.miempresa.gasapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var retrofit: Retrofit? = null

    fun getClient(): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            val httpClient = OkHttpClient.Builder()
            httpClient.connectTimeout(15, TimeUnit.SECONDS)
            httpClient.readTimeout(15, TimeUnit.SECONDS)
            httpClient.addInterceptor(logging)

            retrofit = Retrofit.Builder()
                .baseUrl("https://gas-bxm2.onrender.com/")
                //.baseUrl("http://192.168.1.40:8000/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
        }
        return retrofit!!
    }
}