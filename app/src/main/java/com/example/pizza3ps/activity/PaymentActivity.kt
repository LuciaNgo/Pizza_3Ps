package com.example.pizza3ps.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.PaymentCartAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.fragment.SavedAddressFragment
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var addressDetail: ImageView

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

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var fragmentContainerView : FrameLayout

    private val momoClientId = "MOMO"
    private val momoSecret = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private var lastGeneratedOrderId: String? = null
    private var orderId: String? = null

    private val REQUEST_CODE = 101
    private val sandboxToken = "sandbox_zq6cwtqf_23pxw29xrgphjwfp"
    private val CLIENT_ID = "ASursRuZJ17ROQ3HKBNtGjjVRcnHHNz46eb920Q4f9i9UnQQHBinAGJ0UNFKBNGMIoqQam2bN9aYOgu0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT)


        dbHelper = DatabaseHelper(this)
        cartList = dbHelper.getAllCartItems()

        customerName = findViewById(R.id.customerName)
        customerPhone = findViewById(R.id.customerPhoneNumber)
        customerAddress = findViewById(R.id.customerAddress)
        addressDetail = findViewById(R.id.addressDetailIcon)

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

        val defaultAddress = getDefaultAddress()
        if (defaultAddress.name == "" && defaultAddress.phone == "" && defaultAddress.address == "") {
            customerName.text = "No address added"
            customerPhone.visibility = ImageView.GONE
            customerAddress.visibility = ImageView.GONE
        } else {
            customerName.text = defaultAddress.name
            customerPhone.text = defaultAddress.phone
            customerAddress.text = defaultAddress.address
        }

        addressDetail.setOnClickListener {
            //fragmentContainerView.visibility = FrameLayout.VISIBLE

            val fragment = SavedAddressFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()

        }

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
                    "paypal" ->  Toast.makeText(this, "huhu", Toast.LENGTH_SHORT).show()
                }
            }
        }
        addressDetail.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.edti_info_temp, null)
            val nameInput = dialogView.findViewById<EditText>(R.id.etTempName)
            val phoneInput = dialogView.findViewById<EditText>(R.id.etTempPhone)
            val addressInput = dialogView.findViewById<EditText>(R.id.etTempAddress)
            val applyBtn = dialogView.findViewById<Button>(R.id.btnApply)
            val cancelBtn = dialogView.findViewById<Button>(R.id.btnCancel)

            // Pre-fill with current values
            nameInput.setText(customerName.text)
            phoneInput.setText(customerPhone.text)
            addressInput.setText(customerAddress.text)


            val dialog = AlertDialog.Builder(this)
                .setTitle("Edit Delivery Information")
                .setView(dialogView)
                .create()

            applyBtn.setOnClickListener {
                customerName.text = nameInput.text.toString()
                customerPhone.text = phoneInput.text.toString()
                customerAddress.text = addressInput.text.toString()
                dialog.dismiss()
            }

            cancelBtn.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == RESULT_OK && data != null) {
            val status = data.getIntExtra("status", -1)
            if (status == 0) {
                // Success
                Toast.makeText(this, "MoMo payment successful", Toast.LENGTH_SHORT).show()
                createOrderInFirestore(orderId!!, "momo")
            } else {
                val message = data.getStringExtra("message") ?: "Payment failed"
                Toast.makeText(this, "MoMo payment failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getDefaultAddress(): AddressData {
        val defaultAddress = dbHelper.getDefaultAddress()
        return if (defaultAddress != null) {
            defaultAddress
        } else {
            AddressData("", "", "", false)
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

        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);
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
        //val user = dbHelper.getUser()

        val order = hashMapOf(
            "userID" to FirebaseAuth.getInstance().uid,
            "receiverName" to customerName.text.toString(),
            "phoneNumber" to customerPhone.text.toString(),
            "address" to customerAddress.text.toString(),
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
                "price" to item.price
            )

            if (category == "pizza") {
                detail["size"] = item.size
                detail["crust"] = item.crust
                detail["crustBase"] = item.crustBase
                detail["ingredients"] = item.ingredients.joinToString(", ")

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