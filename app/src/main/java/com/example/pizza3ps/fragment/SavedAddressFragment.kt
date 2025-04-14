package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pizza3ps.R
import com.example.pizza3ps.adapter.AddressAdapter
import com.example.pizza3ps.database.DatabaseHelper
import com.example.pizza3ps.model.AddressData

class SavedAddressFragment : Fragment() {
    private lateinit var addressRecyclerView: RecyclerView
    private lateinit var infoLayout: ConstraintLayout
    private lateinit var addButton: Button
    private lateinit var backButton: ImageView

    private lateinit var dbHelper: DatabaseHelper
    private var addressList = mutableListOf<AddressData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_saved_address, container, false)

        addressRecyclerView = view.findViewById(R.id.addressRecyclerView)
        infoLayout = view.findViewById(R.id.infoLayout)
        addButton = view.findViewById(R.id.addAddressButton)
        backButton = view.findViewById(R.id.backButton)

        dbHelper = DatabaseHelper(requireContext())
        setUpView()

        addButton.setOnClickListener {
            findNavController().navigate(R.id.action_savedAddressFragment_to_addAddressFragment)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        addressList.clear()
        setUpView()
    }

    private fun setUpView() {
        addressList.addAll(dbHelper.getAllAddresses())

        if (addressList.isEmpty()) {
            infoLayout.visibility = View.VISIBLE
            addressRecyclerView.visibility = View.GONE
        } else {
            infoLayout.visibility = View.GONE
            addressRecyclerView.visibility = View.VISIBLE
            addressRecyclerView.adapter = AddressAdapter(addressList)
            addressRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }


}