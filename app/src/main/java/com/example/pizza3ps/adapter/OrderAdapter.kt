package com.example.pizza3ps.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.model.OrderData

class OrderAdapter(
    private val onCancelClicked: (OrderData) -> Unit,
    private val onNextStatusClicked: (OrderData, String) -> Unit,
    private val onItemClicked: (OrderData) -> Unit
) : ListAdapter<OrderData, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderID = itemView.findViewById<TextView>(R.id.tvOrderID)
        private val tvOrderDate = itemView.findViewById<TextView>(R.id.tvOrderDate)
        private val tvOrderStatus = itemView.findViewById<TextView>(R.id.tvOrderStatus)
        private val tvCustomerName = itemView.findViewById<TextView>(R.id.tvCustomerName)
        private val tvCustomerContact = itemView.findViewById<TextView>(R.id.tvCustomerPhone)
        private val tvCustomerAddress = itemView.findViewById<TextView>(R.id.tvCustomerAddress)
        private val tvTotalQuantity = itemView.findViewById<TextView>(R.id.tvTotalQuantity)
        private val tvTotalAmount = itemView.findViewById<TextView>(R.id.tvTotalAfterDiscount)
        private val tvPaymentMethod = itemView.findViewById<TextView>(R.id.tvPaymentMethod)
        private val btnCancel = itemView.findViewById<Button>(R.id.btnCancel)
        private val btnNextStatus = itemView.findViewById<Button>(R.id.btnNextStatus)

        fun bind(order: OrderData) {
            tvOrderID.text = "Order ID: ${order.orderId}"
            tvOrderDate.text = "Date: ${order.createdDate}"
            tvOrderStatus.text = "Status: ${order.status}"
            tvCustomerName.text = "Receiver: ${order.receiverName}"
            tvCustomerContact.text = "Phone: ${order.phoneNumber}"
            tvCustomerAddress.text = "Address: ${order.address}"
            tvTotalQuantity.text = "Total quantity: ${order.totalQuantity}"
            tvTotalAmount.text = "Total after discount: ${order.totalAfterDiscount}đ"
            tvPaymentMethod.text = "Payment method: ${order.payment}"

            val nextStatus = getNextStatus(order.status)

            if (order.status == "Cancelled" || order.status == "Completed") {
                btnCancel.visibility = View.GONE
                btnNextStatus.visibility = View.GONE
            } else {
                btnCancel.visibility = View.VISIBLE
                btnNextStatus.visibility = View.VISIBLE

                btnNextStatus.text = nextStatus ?: "Done"
                btnNextStatus.setOnClickListener {
                    nextStatus?.let {
                        onNextStatusClicked(order, it)
                    }
                }
                btnCancel.setOnClickListener { onCancelClicked(order) }
            }

            itemView.setOnClickListener { onItemClicked(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_management_item, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    fun getNextStatus(currentStatus: String): String? {
        val statusFlow = listOf("Pending", "Confirmed", "Preparing", "Shipping", "Completed")
        val index = statusFlow.indexOf(currentStatus)
        return if (index != -1 && index < statusFlow.size - 1) {
            statusFlow[index + 1]
        } else {
            null
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderData>() {
        override fun areItemsTheSame(oldItem: OrderData, newItem: OrderData): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderData, newItem: OrderData): Boolean {
            return oldItem == newItem
        }
    }
}
