package com.example.pizza3ps.FCM

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.pizza3ps.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.RemoteMessage
import android.content.Context
import android.util.Log
import com.example.pizza3ps.activity.DeliveryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            val orderId = remoteMessage.data["orderId"] ?: ""
            val status = remoteMessage.data["status"] ?: ""

            Log.d("FirebaseMessaging", "Order ID: $orderId, Status: $status")

            val title = "Order Update"
            val message = when (status) {
                "Shipping" -> "Your order is being delivered!"
                "Cancelled" -> "Your order has been cancelled!"
                else -> "Your order has been updated!"
            }

            Log.d("FirebaseMessaging", "Notification Title: $title, Message: $message")
            sendNotification(title, message, orderId)
        }
    }

    override fun onNewToken(token: String) {
        saveTokenToFirestore(token)
    }

    private fun saveTokenToFirestore(token: String) {
        val userId = getCurrentUserId()
        if (userId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Users").document(userId)
                .update("deviceToken", token)
                .addOnSuccessListener {
                    Log.d("FirebaseMessaging", "Device Token updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("FirebaseMessaging", "Error updating device token", e)
                }
        }
    }

    private fun getCurrentUserId(): String {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid ?: ""
    }

    private fun sendNotification(title: String, messageBody: String, orderId: String) {
        val intent = Intent(this, DeliveryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("order_id", orderId) // Gửi orderId qua Intent
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "order_notification_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.pizzaa_128)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo notification channel cho Android O trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}