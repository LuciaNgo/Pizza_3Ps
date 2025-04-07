package com.example.pizza3ps.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.andremion.counterfab.CounterFab
import com.example.pizza3ps.R

class AccountFragment : Fragment() {
    private lateinit var btnCustomerService: ConstraintLayout
    private lateinit var fab: CounterFab

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        fab = requireActivity().findViewById(R.id.cart_fab)
        fab.visibility = View.GONE

        // Lấy tham chiếu đến nút Customer Service
        btnCustomerService = view.findViewById(R.id.btnCustomerService)

        // Thiết lập sự kiện nhấn nút
        btnCustomerService.setOnClickListener {
            // Dùng NavController để điều hướng tới CustomerServiceFragment
            findNavController().navigate(R.id.action_accountFragment_to_customerServiceFragment)
        }

        // Cập nhật padding cho view khi có thay đổi từ system bars (mặc định dùng EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        return view
    }
}
