package com.medical.app.ui.reports

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.medical.app.databinding.DialogExportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportDialog : DialogFragment() {

    private var _binding: DialogExportBinding? = null
    private val binding get() = _binding!!
    
    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    var onExport: ((format: ExportFormat, dataType: ExportDataType, startDate: Long?, endDate: Long?) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogExportBinding.inflate(inflater, container, false)
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
        
        binding.btnExport.setOnClickListener {
            val format = when (binding.radioGroupFormat.checkedRadioButtonId) {
                binding.radioPdf.id -> ExportFormat.PDF
                binding.radioExcel.id -> ExportFormat.EXCEL
                binding.radioCsv.id -> ExportFormat.CSV
                else -> ExportFormat.PDF
            }
            
            val dataType = when (binding.radioGroupDataType.checkedRadioButtonId) {
                binding.radioPatients.id -> ExportDataType.PATIENTS
                binding.radioAppointments.id -> ExportDataType.APPOINTMENTS
                binding.radioAll.id -> ExportDataType.ALL
                else -> ExportDataType.ALL
            }
            
            onExport?.invoke(format, dataType, startDate, endDate)
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

    enum class ExportFormat {
        PDF, EXCEL, CSV
    }

    enum class ExportDataType {
        PATIENTS, APPOINTMENTS, ALL
    }

    companion object {
        const val TAG = "ExportDialog"
        
        fun newInstance(): ExportDialog {
            return ExportDialog()
        }
    }
}
