package com.example.pizza3ps.activity

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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
import java.text.DecimalFormat

class PaymentActivity : AppCompatActivity() {
    private lateinit var backButton: ImageView
    private lateinit var checkoutButton : Button
    private lateinit var subtotalPrice : TextView
    private lateinit var deliveryCharge : TextView
    private lateinit var discount : TextView
    private lateinit var totalPrice : TextView
    private lateinit var customerName: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerAddress: TextView
    private lateinit var addressDetail: ImageView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        dbHelper = DatabaseHelper(this)
        cartList = dbHelper.getAllCartItems()

        //fragmentContainerView = findViewById(R.id.fragment_container)
        //fragmentContainerView.visibility = FrameLayout.GONE

        redeemPoints = findViewById(R.id.pointsContainer)

        backButton = findViewById(R.id.back_button)
        checkoutButton = findViewById(R.id.checkout_button)
        subtotalPrice = findViewById(R.id.subtotal_price)
        deliveryCharge = findViewById(R.id.delivery_charge_price)
        discount = findViewById(R.id.discount)
        totalPrice = findViewById(R.id.total_price)
        customerName = findViewById(R.id.customerName)
        customerPhone = findViewById(R.id.customerPhoneNumber)
        customerAddress = findViewById(R.id.customerAddress)
        addressDetail = findViewById(R.id.addressDetailIcon)

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

        checkoutButton.setOnClickListener {
            if (selectedPaymentMethod == "paypal") {
                // Khởi tạo DropInRequest

            }
        }


        updateTotalPrice()

        redeemPoints.setOnClickListener {

        }

        backButton.setOnClickListener {
            onBackPressed()
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
}