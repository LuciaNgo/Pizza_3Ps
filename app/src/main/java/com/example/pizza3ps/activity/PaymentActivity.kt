package com.example.pizza3ps.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.PaymentCartAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.CartData
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.text.DecimalFormat
import vn.momo.momo_partner.AppMoMoLib
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaymentActivity : AppCompatActivity() {
    private lateinit var customerName : TextView
    private lateinit var customerPhone : TextView
    private lateinit var customerAddress: TextView

    private lateinit var backButton: ImageView
    private lateinit var checkoutButton : Button
    private lateinit var subtotalPrice : TextView
    private lateinit var deliveryCharge : TextView
    private lateinit var discount : TextView
    private lateinit var totalPrice : TextView

    private lateinit var cashContainer : ConstraintLayout
    private lateinit var paypalContainer : ConstraintLayout
    private lateinit var momoContainer : ConstraintLayout
    private lateinit var cashText : TextView
    private lateinit var paypalText : TextView
    private lateinit var momoText : TextView
    private lateinit var cashCheck : ImageView
    private lateinit var paypalCheck : ImageView
    private lateinit var momoCheck : ImageView

    private lateinit var redeemPoints : ConstraintLayout

    private lateinit var cartRecyclerView: RecyclerView
    private var cartList = listOf<CartData>()
    private var subtotalValue: Int = 0
    private var deliveryValue: Int = 0
    private var discountValue: Int = 0
    private var totalValue: Int = 0
    private var selectedPaymentMethod = ""

    private val momoClientId = "MOMO"
    private val momoSecret = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private var lastGeneratedOrderId: String? = null
    private var orderId: String? = null

    private val momoPaymentLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val status = data?.getIntExtra("status", -1) ?: -1

        if (result.resultCode == RESULT_OK && status == 0) {
            Toast.makeText(this, "MoMo payment successful", Toast.LENGTH_SHORT).show()
            orderId?.let {
                createOrderInFirestore(it, "momo")
            }
        } else {
            val message = data?.getStringExtra("message") ?: "Payment failed or cancelled"
            Toast.makeText(this, "MoMo payment failed: $message", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        val dbHelper = DatabaseHelper(this)

        customerName = findViewById(R.id.customerName)
        customerPhone = findViewById(R.id.customerPhoneNumber)
        customerAddress = findViewById(R.id.customerAddress)

        cartList = dbHelper.getAllCartItems()

        redeemPoints = findViewById(R.id.pointsContainer)

        backButton = findViewById(R.id.back_button)
        checkoutButton = findViewById(R.id.checkout_button)
        subtotalPrice = findViewById(R.id.subtotal_price)
        deliveryCharge = findViewById(R.id.delivery_charge_price)
        discount = findViewById(R.id.discount)
        totalPrice = findViewById(R.id.total_price)

        cartRecyclerView = findViewById(R.id.cart_recyclerView)
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = PaymentCartAdapter(this, cartList)

        cashContainer = findViewById(R.id.cashContainer)
        paypalContainer = findViewById(R.id.paypalContainer)
        momoContainer = findViewById(R.id.momoContainer)
        cashText = findViewById(R.id.cashText)
        paypalText = findViewById(R.id.paypalText)
        momoText = findViewById(R.id.momoText)
        cashCheck = findViewById(R.id.cashCheck)
        paypalCheck = findViewById(R.id.paypalCheck)
        momoCheck = findViewById(R.id.momoCheck)

        cashCheck.visibility = ImageView.INVISIBLE
        paypalCheck.visibility = ImageView.INVISIBLE
        momoCheck.visibility = ImageView.INVISIBLE

        val user = DatabaseHelper(this).getUser()
        customerName.text = user?.name ?: "Guest"
        customerPhone.text = user?.phone?: "Phone"
        customerAddress.text = user?.address?: "Address"

        cashContainer.setOnClickListener {
            updateSelectedPaymentMethod("cash")
        }

        paypalContainer.setOnClickListener {
            updateSelectedPaymentMethod("paypal")
        }

        momoContainer.setOnClickListener {
            updateSelectedPaymentMethod("momo")
        }


        updateTotalPrice()


        redeemPoints.setOnClickListener {

        }

        backButton.setOnClickListener {
            onBackPressed()
        }

        checkoutButton.setOnClickListener {
            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Please choose a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateOrderId { generatedId ->
                orderId = generatedId

                when (selectedPaymentMethod) {
                    "momo" -> requestMoMoPayment(totalValue)
                    "cash" -> orderId?.let { createOrderInFirestore(it, selectedPaymentMethod) }
                    "paypal" -> Toast.makeText(this, "Paypal is not supported right now.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    fun updateSelectedPaymentMethod(method: String) {
        cashCheck.visibility = ImageView.INVISIBLE
        paypalCheck.visibility = ImageView.INVISIBLE
        momoCheck.visibility = ImageView.INVISIBLE

        cashText.typeface = Typeface.DEFAULT
        paypalText.typeface = Typeface.DEFAULT
        momoText.typeface = Typeface.DEFAULT

        when (method) {
            "cash" -> {
                cashCheck.visibility = ImageView.VISIBLE
                cashText.setTypeface(cashText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "cash"
            }
            "paypal" -> {
                paypalCheck.visibility = ImageView.VISIBLE
                paypalText.setTypeface(paypalText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "paypal"
            }
            "momo" -> {
                momoCheck.visibility = ImageView.VISIBLE
                momoText.setTypeface(momoText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "momo"
            }
        }
    }

    fun updateTotalPrice() {
        // Sub total
        val dbHelper = DatabaseHelper(this)
        subtotalValue = dbHelper.calculateTotalPrice()
        this.subtotalPrice.text = DecimalFormat("#,###").format(subtotalValue) + " VND"

        // Delivery charge
        if (subtotalValue < 200000) {
            deliveryValue = 20000
            this.deliveryCharge.text = DecimalFormat("#,###").format(deliveryValue) + " VND"
        } else {
            deliveryValue = 0
            this.deliveryCharge.text = "Free"
        }

        // Discount
        this.discount.text = DecimalFormat("#,###").format(discountValue) + " VND"

        // Total price
        totalValue = subtotalValue + deliveryValue - discountValue
        this.totalPrice.text = DecimalFormat("#,###").format(totalValue) + " VND"
    }

    fun requestMoMoPayment(amount: Int) {
        if (orderId == null) {
            Toast.makeText(this, "Order ID is not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val momoParams = HashMap<String, Any>()
        momoParams["merchantname"] = "Pizza3Ps"
        momoParams["merchantcode"] = momoClientId
        momoParams["amount"] = amount
        momoParams["orderId"] = orderId!!
        momoParams["orderLabel"] = "Food Order"
        momoParams["merchantnamelabel"] = "Pizza 3Ps Order"
        momoParams["fee"] = 0
        momoParams["description"] = "Payment for Pizza Order"
        momoParams["requestId"] = "ORDER_$orderId"
        momoParams["partnerCode"] = momoClientId
        momoParams["extraData"] = ""
        momoParams["environment"] = 0 // 0: sandbox, 1: production

        val momoIntent = Intent() // this is a placeholder; SDK should provide this
        momoIntent.putExtra("action", "momo_payment") // and all params like orderId, amount, etc.

        momoPaymentLauncher.launch(momoIntent)


        lastGeneratedOrderId = orderId
    }


    fun generateOrderId(callback: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val today = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date()) // e.g. 250414

        val ordersRef = db.collection("Orders")
        ordersRef
            .whereGreaterThanOrEqualTo("createdDate", getStartOfToday())
            .get()
            .addOnSuccessListener { documents ->
                val orderCountToday = documents.size() + 1
                val formattedNumber = String.format("%04d", orderCountToday)
                val orderId = "$today$formattedNumber"
                callback(orderId)
            }
    }

    fun getStartOfToday(): Timestamp {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(calendar.time)
    }

    fun createOrderInFirestore(orderId: String, paymentMethod: String) {
        val db = FirebaseFirestore.getInstance()
        val dbHelper = DatabaseHelper(this)
        val user = dbHelper.getUser()

        val order = hashMapOf(
            "orderId" to orderId,
            "userID" to FirebaseAuth.getInstance().uid,
            "receiverName" to user?.name,
            "phoneNumber" to user?.phone,
            "address" to user?.address,
            "payment" to paymentMethod,
            "discount" to 0,
            "totalAfterDiscount" to totalValue,
            "totalQuantity" to dbHelper.getAllCartItems().size,
            "status" to "Awaiting confirmation",
            "createdDate" to com.google.firebase.Timestamp.now()
        )

        db.collection("Orders").document(orderId)
            .set(order)
            .addOnSuccessListener {
                uploadOrderDetails(orderId)
            }
    }

    fun uploadOrderDetails(orderId: String) {
        val db = FirebaseFirestore.getInstance()
        val dbHelper = DatabaseHelper(this)
        val cartItems = dbHelper.getAllCartItems()

        val batch = db.batch()
        val orderDetailsRef = db.collection("Orders").document(orderId).collection("OrderDetails")

        cartItems.forEachIndexed { index, item ->
            val foodInfo = dbHelper.getFoodById(item.food_id)  // Get full food info
            val category = foodInfo.category                   // Get category based on food_id

            val detail = mutableMapOf<String, Any>(
                "foodId" to item.food_id,
                "quantity" to item.quantity,
                "unitPrice" to item.price,
                "totalPrice" to item.quantity * item.price
            )

            if (category == "pizza") {
                detail["size"] = item.size
                detail["crust"] = item.crust
                detail["crustBase"] = item.crustBase
                detail["ingredients"] = item.ingredients ?: listOf<String>()
            }

            val docRef = orderDetailsRef.document((index + 1).toString())
            batch.set(docRef, detail)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show()
            dbHelper.deleteAllCart()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to place order: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }




}