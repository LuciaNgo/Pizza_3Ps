package com.example.pizza3ps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import vn.momo.momo_partner.AppMoMoLib

class MomoPaymentActivity : AppCompatActivity() {
    private val momoClientId = "MOMO"
    private var orderId: String? = null
    private var amount: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_momo_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        orderId = intent.getStringExtra("orderId")
        amount = intent.getIntExtra("amount", 0)

        if (orderId == null) {
            Toast.makeText(this, "Order ID is not ready", Toast.LENGTH_SHORT).show()
            return
        }

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)

        val momoParams = HashMap<String, Any>().apply {
            put("merchantname", "Pizza3Ps") // Your MoMo merchant name
            put("merchantcode", momoClientId) // Your MoMo merchant code
            put("amount", amount) // Must be Int
            put("orderId", orderId!!) // Unique per order
            put("orderLabel", "Pizza Order") // Custom label

            // Optional
            put("merchantnamelabel", "Pizza3Ps Order")
            put("fee", 0)
            put("description", "Thanh toán đơn hàng Pizza")

            // Required for verifying transaction
            put("requestId", momoClientId + "_billId_" + System.currentTimeMillis())
            put("partnerCode", momoClientId)

            // Optional extra info
            put("extraData", "")
            put("extra", "")
        }

        AppMoMoLib.getInstance().requestMoMoCallBack(this, momoParams)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO) {
            val resultIntent = Intent()
            resultIntent.putExtras(data?.extras ?: Bundle())
            setResult(Activity.RESULT_OK, resultIntent)
            finish() // <-- VERY IMPORTANT: finish this activity after passing result
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}