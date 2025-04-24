package com.example.pizza3ps.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.DeliveryAdapter
import com.example.pizza3ps.model.DeliveryData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.shuhart.stepview.StepView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale


class DeliveryActivity : AppCompatActivity() {
    private lateinit var stepView: StepView
    private var listenerRegistration: ListenerRegistration? = null
    private lateinit var orderId: String

    private lateinit var orderID: TextView
    private lateinit var orderDate: TextView
    private lateinit var totalAmount: TextView
    private lateinit var subtotalAmount: TextView
    private lateinit var discountAmount: TextView
    private lateinit var paymentMethod: TextView

    private lateinit var customerName: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerAddress: TextView

    private lateinit var discountLayout: ConstraintLayout
    private lateinit var subtotalLayout: ConstraintLayout
    private lateinit var parentLayout: LinearLayout

    private lateinit var backButton: ImageView
    private lateinit var downChevron: ImageView
    private lateinit var recyclerView: RecyclerView

    private lateinit var deliveryAdapter: DeliveryAdapter
    private val deliveryList = mutableListOf<DeliveryData>()

    private val orderStates = listOf(
        "Pending",
        "Confirmed",
        "Preparing",
        "Shipping",
        "Completed"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_delivery)

        orderId = intent.getStringExtra("order_id") ?: return
        Log.d("DeliveryActivityOrderID", "Order ID: $orderId")

        orderID = findViewById(R.id.order_id)
        orderDate = findViewById(R.id.order_date)
        totalAmount = findViewById(R.id.total_amount)
        subtotalAmount = findViewById(R.id.subtotal_amount)
        discountAmount = findViewById(R.id.discount_amount)
        paymentMethod = findViewById(R.id.payment_method)
        customerName = findViewById(R.id.customer_name)
        customerPhone = findViewById(R.id.customer_phone)
        customerAddress = findViewById(R.id.customer_address)
        discountLayout = findViewById(R.id.discount_amount_layout)
        subtotalLayout = findViewById(R.id.subtotal_amount_layout)
        parentLayout = findViewById(R.id.order_amount_container)
        downChevron = findViewById(R.id.down_chevron)
        backButton = findViewById(R.id.back_button)

        stepView = findViewById(R.id.step_view)
        stepView.setSteps(orderStates)

        recyclerView = findViewById(R.id.recycler_view)
        deliveryAdapter = DeliveryAdapter(this, deliveryList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deliveryAdapter

        discountLayout.visibility = ConstraintLayout.GONE
        subtotalLayout.visibility = ConstraintLayout.GONE

        observeOrderStatus()
        loadOrderItems(orderId)

        downChevron.setOnClickListener {
            val transition = AutoTransition()
            transition.duration = 200
            TransitionManager.beginDelayedTransition(parentLayout, transition)

            if (discountLayout.visibility == ConstraintLayout.GONE) {
                discountLayout.visibility = ConstraintLayout.VISIBLE
                subtotalLayout.visibility = ConstraintLayout.VISIBLE
                downChevron.setImageResource(R.drawable.up_chevron)
            } else {
                discountLayout.visibility = ConstraintLayout.GONE
                subtotalLayout.visibility = ConstraintLayout.GONE
                downChevron.setImageResource(R.drawable.down_chevron)
            }
        }

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun observeOrderStatus() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("Orders").document(orderId)

        listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

            orderID.text = orderId

            val timestamp = snapshot.getTimestamp("createdDate")
            orderDate.text = timestamp?.toDate()?.let { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it) } ?: ""

            val total = snapshot.getDouble("totalAfterDiscount") ?: 0.0
            val discount = snapshot.getDouble("discount") ?: 0.0
            val subtotal = total + discount

            totalAmount.text = DecimalFormat("#,###").format(total) + " VND"
            discountAmount.text = DecimalFormat("#,###").format(discount) + " VND"
            subtotalAmount.text = DecimalFormat("#,###").format(subtotal) + " VND"

            paymentMethod.text = snapshot.getString("payment")
            customerName.text = snapshot.getString("receiverName")
            customerPhone.text = snapshot.getString("phoneNumber")
            customerAddress.text = snapshot.getString("address")

            val status = snapshot.getString("status") ?: return@addSnapshotListener
            val stepIndex = orderStates.indexOf(status)

            if (stepIndex != -1) {
                stepView.go(stepIndex, true)
            }
        }
    }

    private fun loadOrderItems(orderId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Orders")
            .document(orderId)
            .collection("OrderDetails")
            .get()
            .addOnSuccessListener { documents ->

                deliveryList.clear()

                for (document in documents) {
                    val item = DeliveryData(
                        foodId = document.getLong("foodId")?.toInt() ?: 0,
                        price = document.getLong("price") ?.toInt() ?: 0,
                        quantity = document.getLong("quantity")?.toInt() ?: 0,
                        size = document.getString("size") ?: "",
                        crust = document.getString("crust") ?: "",
                        crustBase = document.getString("crustBase") ?: "",
                        ingredients = document.getString("ingredients") ?: ""
                    )
                    Log.d("DeliveryActivityItem", "Item: $item")
                    deliveryList.add(item)
                }
                deliveryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load order items", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy lắng nghe Firestore khi Activity bị huỷ
        listenerRegistration?.remove()
    }
}