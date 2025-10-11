package com.medical.app.ui.reports

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.medical.app.databinding.DialogCustomReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomReportDialog : DialogFragment() {

    private var _binding: DialogCustomReportBinding? = null
    private val binding get() = _binding!!
    
    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    var onGenerateReport: ((reportType: String, startDate: Long?, endDate: Long?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDatePickers()
        setupButtons()
    }

    private fun setupDatePickers() {
        // Selector de fecha inicial
        binding.btnSelectStartDate.setOnClickListener {
            showDatePicker { selectedDate ->
                startDate = selectedDate
                binding.tvStartDate.text = dateFormat.format(Date(selectedDate))
                binding.tvStartDate.visibility = View.VISIBLE
            }
        }
        
        // Selector de fecha final
        binding.btnSelectEndDate.setOnClickListener {
            showDatePicker { selectedDate ->
                endDate = selectedDate
                binding.tvEndDate.text = dateFormat.format(Date(selectedDate))
                binding.tvEndDate.visibility = View.VISIBLE
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnGenerate.setOnClickListener {
            val selectedType = when (binding.radioGroupReportType.checkedRadioButtonId) {
                binding.radioPatients.id -> "patients_custom"
                binding.radioAppointments.id -> "appointments_custom"
                binding.radioRevenue.id -> "revenue_custom"
                else -> "general_custom"
            }
            
            onGenerateReport?.invoke(selectedType, startDate, endDate)
            dismiss()
        }
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleccionar fecha")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        
        datePicker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }
        
        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CustomReportDialog"
        
        fun newInstance(): CustomReportDialog {
            return CustomReportDialog()
        }
    }
}
