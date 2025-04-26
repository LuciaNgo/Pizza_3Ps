package com.example.pizza3ps.fragment

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pizza3ps.R
import java.util.Calendar

class MonthYearPickerDialogFragment(
    private val listener: (month: Int, year: Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.month_year_picker, null)

        val npMonth = view.findViewById<NumberPicker>(R.id.np_month)
        val npYear = view.findViewById<NumberPicker>(R.id.np_year)

        // Cấu hình Month picker (1 -> 12)
        npMonth.minValue = 1
        npMonth.maxValue = 12
        npMonth.value = Calendar.getInstance().get(Calendar.MONTH) + 1

        // Cấu hình Year picker
        val yearNow = Calendar.getInstance().get(Calendar.YEAR)
        npYear.minValue = yearNow - 50
        npYear.maxValue = yearNow + 50
        npYear.value = yearNow

        return AlertDialog.Builder(requireContext())
            .setTitle("Select month and year")
            .setView(view)
            .setPositiveButton("OK") { _, _ ->
                val selectedMonth = npMonth.value
                val selectedYear = npYear.value
                listener(selectedMonth, selectedYear)
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