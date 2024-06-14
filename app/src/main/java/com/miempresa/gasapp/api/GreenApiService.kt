package com.miempresa.gasapp.api
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

data class MessageBody(
    val chatId: String,
    val message: String
)
interface GreenApiService {
    @Headers("Content-Type: application/json")
    @POST("waInstance{idInstance}/SendMessage/{apiTokenInstance}")
    fun sendMessage(
        @Path("idInstance") idInstance: String,
        @Path("apiTokenInstance") apiTokenInstance: String,
        @Body messageBody: MessageBody
    ): Call<Any>
}