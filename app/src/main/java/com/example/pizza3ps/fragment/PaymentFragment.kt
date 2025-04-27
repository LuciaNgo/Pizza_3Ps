package com.example.pizza3ps.fragment

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.PaymentCartAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.AddressData
import com.example.pizza3ps.model.CartData
import java.text.DecimalFormat
import androidx.navigation.fragment.findNavController
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pizza3ps.activity.DeliveryActivity
import com.google.firebase.firestore.FirebaseFirestore
import vn.momo.momo_partner.AppMoMoLib
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PaymentFragment : Fragment() {
    private lateinit var customerName: TextView
    private lateinit var customerPhone: TextView
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

    private var currentSelectedAddressId : Int = 0

    private val momoClientId = "MOMO"
    private val momoSecret = "K951B6PE1waDMi640xX08PD3vg6EkVlz"
    private var lastGeneratedOrderId: String? = null
    private var orderId: String? = null

    private val REQUEST_CODE = 101
    private val sandboxToken = "sandbox_zq6cwtqf_23pxw29xrgphjwfp"
    private val CLIENT_ID = "ASursRuZJ17ROQ3HKBNtGjjVRcnHHNz46eb920Q4f9i9UnQQHBinAGJ0UNFKBNGMIoqQam2bN9aYOgu0"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        dbHelper = DatabaseHelper(requireContext())
        cartList = dbHelper.getAllCartItems()

        customerName = view.findViewById(R.id.customerName)
        customerPhone = view.findViewById(R.id.customerPhoneNumber)
        customerAddress = view.findViewById(R.id.customerAddress)
        addressDetail = view.findViewById(R.id.addressDetailIcon)

        redeemPoints = view.findViewById(R.id.pointsContainer)

        backButton = view.findViewById(R.id.back_button)
        checkoutButton = view.findViewById(R.id.checkout_button)
        subtotalPrice = view.findViewById(R.id.subtotal_price)
        deliveryCharge = view.findViewById(R.id.delivery_charge_price)
        discount = view.findViewById(R.id.discount)
        totalPrice = view.findViewById(R.id.total_price)

        cartRecyclerView = view.findViewById(R.id.cart_recyclerView)

        cashContainer = view.findViewById(R.id.cashContainer)
        paypalContainer = view.findViewById(R.id.paypalContainer)
        momoContainer = view.findViewById(R.id.momoContainer)
        cashText = view.findViewById(R.id.cashText)
        paypalText = view.findViewById(R.id.paypalText)
        momoText = view.findViewById(R.id.momoText)
        cashCheck = view.findViewById(R.id.cashCheck)
        paypalCheck = view.findViewById(R.id.paypalCheck)
        momoCheck = view.findViewById(R.id.momoCheck)

        cashCheck.visibility = ImageView.INVISIBLE
        paypalCheck.visibility = ImageView.INVISIBLE
        momoCheck.visibility = ImageView.INVISIBLE

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT)

        dbHelper = DatabaseHelper(requireContext())
        cartList = dbHelper.getAllCartItems()

        setupCartRecyclerView()
        setUpAddress()
        updateTotalPrice()

        checkoutButton.setOnClickListener {
            if (customerName.text.toString() == "No address added" && customerPhone.text.toString() == "" && customerAddress.text.toString() == "") {
                Toast.makeText(requireContext(), "Please add an address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(requireContext(), "Please choose a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateOrderId { generatedId ->
                orderId = generatedId

                when (selectedPaymentMethod) {
                    "Momo" -> requestMoMoPayment(totalValue)
                    "Cash" -> orderId?.let { createOrderInFirestore(it, selectedPaymentMethod) }
                    "Paypal" -> Toast.makeText(requireContext(), "huhu", Toast.LENGTH_SHORT).show()
                }
            }
        }

        addressDetail.setOnClickListener {
            findNavController().navigate(
                R.id.action_paymentFragment_to_savedAddressFragment,
                bundleOf("source" to "payment", "selectedAddressId" to currentSelectedAddressId)
            )
        }

        cashContainer.setOnClickListener {
            updateSelectedPaymentMethod("Cash")
        }

        paypalContainer.setOnClickListener {
            updateSelectedPaymentMethod("Paypal")
        }

        momoContainer.setOnClickListener {
            updateSelectedPaymentMethod("Momo")
        }

        redeemPoints.setOnClickListener {
            showRedeemPointsDialog()
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        parentFragmentManager.setFragmentResultListener("selected_address", viewLifecycleOwner) { _, bundle ->
            currentSelectedAddressId = bundle.getInt("selectedAddressId")

            val dbHelper = DatabaseHelper(requireContext())
            val selecetedAddress = dbHelper.getAddressById(currentSelectedAddressId)

            customerName.text = selecetedAddress?.name ?: ""
            customerPhone.text = selecetedAddress?.phone ?: ""
            customerAddress.text = selecetedAddress?.address ?: ""

            customerPhone.visibility = ImageView.VISIBLE
            customerAddress.visibility = ImageView.VISIBLE
        }
    }

    private fun setupCartRecyclerView() {
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = PaymentCartAdapter(this, cartList)
    }

    private fun setUpAddress() {
        val defaultAddress = getDefaultAddress()
        if (defaultAddress.name == "" && defaultAddress.phone == "" && defaultAddress.address == "") {
            customerName.text = "No address added"
            customerPhone.visibility = ImageView.GONE
            customerAddress.visibility = ImageView.GONE
        } else {
            customerName.text = defaultAddress.name
            customerPhone.text = defaultAddress.phone
            customerAddress.text = defaultAddress.address
            currentSelectedAddressId = dbHelper.getAddressId(defaultAddress)
        }
    }

    fun getDefaultAddress(): AddressData {
        val defaultAddress = dbHelper.getDefaultAddress()
        return if (defaultAddress != null) {
            Log.d("DefaultAddress", "Name: ${defaultAddress.name}, Phone: ${defaultAddress.phone}, Address: ${defaultAddress.address}")
            defaultAddress
        } else {
            Log.d("DefaultAddress", "No default address found")
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
            "Cash" -> {
                cashCheck.visibility = ImageView.VISIBLE
                cashText.setTypeface(cashText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "Cash"
            }
            "Paypal" -> {
                paypalCheck.visibility = ImageView.VISIBLE
                paypalText.setTypeface(paypalText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "Paypal"
            }
            "Momo" -> {
                momoCheck.visibility = ImageView.VISIBLE
                momoText.setTypeface(momoText.typeface, android.graphics.Typeface.BOLD)
                selectedPaymentMethod = "Momo"
            }
        }
    }

    fun updateTotalPrice() {
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

    private fun showRedeemPointsDialog() {
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

        val dialog = AlertDialog.Builder(requireContext())
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1 && data != null) {
            val status = data.getIntExtra("status", -1)
            if (status == 0) {
                // Success
                Toast.makeText(requireContext(), "MoMo payment successful", Toast.LENGTH_SHORT).show()
                createOrderInFirestore(orderId!!, "Momo")
            } else {
                val message = data.getStringExtra("message") ?: "Payment failed"
                Toast.makeText(requireContext(), "MoMo payment failed: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun requestMoMoPayment(amount: Int) {
        if (orderId == null) {
            Toast.makeText(requireContext(), "Order ID is not ready", Toast.LENGTH_SHORT).show()
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

        AppMoMoLib.getInstance().requestMoMoCallBack(requireActivity(), momoParams)
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
        val dbHelper = DatabaseHelper(requireContext())

        val order = hashMapOf(
            "userID" to FirebaseAuth.getInstance().uid,
            "receiverName" to customerName.text.toString(),
            "phoneNumber" to customerPhone.text.toString(),
            "address" to customerAddress.text.toString(),
            "payment" to paymentMethod,
            "discount" to 0,
            "totalAfterDiscount" to totalValue,
            "totalQuantity" to dbHelper.getAllCartItems().size,
            "status" to "Pending",
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
        val dbHelper = DatabaseHelper(requireContext())
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

            if (category == "pizza" || item.food_id == 0) {
                detail["size"] = item.size
                detail["crust"] = item.crust
                detail["crustBase"] = item.crustBase
                detail["ingredients"] = item.ingredients.joinToString(", ")
            }

            val docRef = orderDetailsRef.document((index + 1).toString())
            batch.set(docRef, detail)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(requireContext(), "Order placed!", Toast.LENGTH_SHORT).show()
            deleteAllCart()

            val intent = Intent(requireContext(), DeliveryActivity::class.java)
            intent.putExtra("order_id", orderId)
            startActivity(intent)

        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to place order: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun removeSyncToFirebase(dbHelper: DatabaseHelper, cartId: Int) {
        val userId = dbHelper.getUser()!!.id

        FirebaseFirestore.getInstance()
            .collection("Cart")
            .document(userId)
            .collection("items")
            .document(cartId.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseSync", "Synced item: $cartId")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseSync", "Failed to sync item: $cartId", e)
            }
    }

    fun deleteAllCart() {
        val dbHelper = DatabaseHelper(requireContext())

        for (cart in cartList) {
            val cartId = dbHelper.getIdOfCartItem(cart)
            removeSyncToFirebase(dbHelper, cartId)
        }

        dbHelper.deleteAllCart()
    }

}