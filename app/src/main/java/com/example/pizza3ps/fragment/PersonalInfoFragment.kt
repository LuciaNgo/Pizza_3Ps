package com.example.pizza3ps.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.DatabaseHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PersonalInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalInfoFragment : Fragment() {
    private lateinit var closeBtn : Button
    private lateinit var editBtn : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_personal_info, container, false)
        val user = DatabaseHelper(requireContext()).getUser()

        view.findViewById<TextView>(R.id.nameHolder).text = "${user?.name}"
        view.findViewById<TextView>(R.id.emailHolder).text = "${user?.email}"
        view.findViewById<TextView>(R.id.phoneHolder).text = "${user?.phone}"
        view.findViewById<TextView>(R.id.addressHolder).text = "${user?.address}"
        view.findViewById<TextView>(R.id.pointHolder).text = "${user?.points}"

        closeBtn = view.findViewById(R.id.closeBtn)
        editBtn = view.findViewById(R.id.editBtn)

        closeBtn.setOnClickListener{
            findNavController().navigate(R.id.action_personalInfoFragment_to_navigation_account)
        }

        editBtn.setOnClickListener {
            Log.d("Edit", "Begin sending")
            val action = PersonalInfoFragmentDirections
                .actionPersonalInfoFragmentToEditInfoFragment(
                    name = user?.name ?: "",
                    email = user?.email ?: "",
                    phone = user?.phone ?: "",
                    address = user?.address ?: "",
                    points = user?.points ?: 0
                )

            findNavController().navigate(action)
            Log.d("Edit_done", "Done sending")
        }
        return view
    }
}