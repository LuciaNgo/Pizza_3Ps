package com.example.pizza3ps.fragment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pizza3ps.R
import java.util.Calendar

class YearPickerDialogFragment(
    private val listener: (Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.year_picker, null)

        val npYear = view.findViewById<NumberPicker>(R.id.npYear)

        val yearNow = Calendar.getInstance().get(Calendar.YEAR)
        npYear.minValue = yearNow - 50
        npYear.maxValue = yearNow + 50
        npYear.value = yearNow

        return AlertDialog.Builder(requireContext())
            .setTitle("Select year")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = npYear.value
                listener(selectedYear)
            }
            .setNegativeButton("Cancel", null)
            .create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
                    getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
                }
            }
    }
}