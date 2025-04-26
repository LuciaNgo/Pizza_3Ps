package com.example.pizza3ps.firebaseMessage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface FCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=YOUR_SERVER_KEY"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMNotification): Call<Void>
}