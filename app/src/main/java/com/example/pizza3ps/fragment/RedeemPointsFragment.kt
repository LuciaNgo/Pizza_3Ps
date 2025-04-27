package com.example.pizza3ps.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.OrderAdapter
import com.example.pizza3ps.adapter.RedeemAdapter
import com.example.pizza3ps.model.EventData
import com.example.pizza3ps.viewModel.CustomerOrderViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat


class RedeemPointsFragment : Fragment() {
    private lateinit var backButton: ImageView
    private lateinit var pointsValue: TextView
    private lateinit var redeemAdapter: RedeemAdapter
    private lateinit var redeemHistoryRecyclerView: RecyclerView
    private lateinit var viewModel: CustomerOrderViewModel
    private var points: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_redeem_points, container, false)

        backButton = view.findViewById(R.id.backButton)
        pointsValue = view.findViewById(R.id.pointsValue)
        redeemHistoryRecyclerView = view.findViewById(R.id.redeemHistoryRecyclerView)
        redeemHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        loadPointsValue()
        loadRedeemHistory()


    }

    private fun loadPointsValue() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    points = (document.getLong("points") ?: 0).toInt()
                    pointsValue.text = DecimalFormat("#,###").format(points)
                }
            }
    }

    private fun loadRedeemHistory() {
        viewModel = ViewModelProvider(requireActivity())[CustomerOrderViewModel::class.java]
        viewModel.listenToOrdersRealtime("Completed")
        viewModel.getOrdersByStatus("Completed").observe(viewLifecycleOwner) { orders ->
            redeemAdapter = RedeemAdapter(requireContext(), orders)
            redeemHistoryRecyclerView.adapter = redeemAdapter
        }
    }



}