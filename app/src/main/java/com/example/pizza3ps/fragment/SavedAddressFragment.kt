package com.example.pizza3ps.fragment

import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
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
            //findNavController().navigate(R.id.action_savedAddressFragment_to_addAddressFragment)

            val fragment = AddAddressFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main, fragment)
                .addToBackStack(null)
                .commit()

        }

        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
            //addressRecyclerView.visibility = View.GONE
        } else {
            infoLayout.visibility = View.GONE
            //addressRecyclerView.visibility = View.VISIBLE
            addressRecyclerView.adapter = AddressAdapter(this, addressList)
            addressRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
            itemTouchHelper.attachToRecyclerView(addressRecyclerView)
        }
    }

    val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.bindingAdapterPosition
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Delete address confirmation")
                .setMessage("Do you want to delete this address?")
                .setPositiveButton("Delete") { _, _ ->
                    val addressId = dbHelper.getAddressId(addressList[position])
                    val adapter = addressRecyclerView.adapter as AddressAdapter
                    adapter.deleteItem(position)
                    adapter.removeSyncToFirebase(addressId)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                    addressRecyclerView.adapter?.notifyItemChanged(position)
                }
                .setCancelable(false)
                .create()

            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.orange)
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
            }
            dialog.show()
        }


        override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val paint = Paint()
            paint.color = ContextCompat.getColor(recyclerView.context, R.color.orange)
            val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.delete_white)!!
            val iconMargin = (itemView.height - icon.intrinsicHeight) / 2

            val maxSwipeDistance = -itemView.width / 4f - 16
            val clampedDX = if (dX < maxSwipeDistance) maxSwipeDistance else dX

            if (clampedDX < 0) {
                // Nền đỏ
                c.drawRect(
                    itemView.right + clampedDX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                )

                // Vẽ icon
                val iconTop = itemView.top + iconMargin
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + icon.intrinsicHeight
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
            }

            // Vẽ với clampedDX
            super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
        }
    }
}