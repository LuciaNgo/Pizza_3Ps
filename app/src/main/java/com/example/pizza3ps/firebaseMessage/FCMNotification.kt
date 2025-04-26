package com.example.pizza3ps.firebaseMessage

data class FCMNotification(
    val to: String,
    val notification: NotificationData
)

data class NotificationData(
    val title: String,
    val body: String
)

