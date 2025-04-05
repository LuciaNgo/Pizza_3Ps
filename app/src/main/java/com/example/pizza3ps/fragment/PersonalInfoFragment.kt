package com.example.pizza3ps.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.pizza3ps.R
import com.example.pizza3ps.database.UserDatabaseHelper

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
        val user = UserDatabaseHelper(requireContext()).getUser()

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
            findNavController().navigate(R.id.action_personalInfoFragment_to_editInfoFragment)

        }
        return view
    }
//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment PersonalInfoFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            PersonalInfoFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}