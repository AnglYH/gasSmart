package com.miempresa.gasapp.network

import com.miempresa.gasapp.model.Lectura
import com.miempresa.gasapp.model.RedWifi
import com.miempresa.gasapp.model.Sensor
import com.miempresa.gasapp.model.UserAPI
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("red/")
    suspend fun enviarRedWifi(@Body red: RedWifi): Response<Void>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserAPI

    @POST("users")
    suspend fun createUser(@Body user: UserAPI): UserAPI

    @GET("lecturas/sensor/{id}/")
    suspend fun obtenerLecturasPorSensor(@Path("id") id: String): Response<List<Lectura>>

    @GET("sensores/")
    suspend fun obtenerSensores(): Response<List<Sensor>>

    @GET("sensores/{id}/")
    suspend fun obtenerSensor(@Path("id") id: String): Response<Sensor>

    @GET("lecturas/")
    suspend fun getLecturas(): Response<List<Lectura>>

    @GET("sensores/usuario/{id}/")
    suspend fun obtenerSensoresPorUsuario(@Path("id") id: String): Response<List<Sensor>>

    //@GET("lecturas/sensor/{idSensor}/")
    //suspend fun obtenerLecturasPorSensor(@Path("idSensor") idSensor: String): Response<List<Lectura>>
}