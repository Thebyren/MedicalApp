package com.medical.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {
        // Estadísticas Generales - Con Gemini AI
        binding.cardGeneralStats.setOnClickListener {
            navigateToAiReport("general_stats")
        }

        // Pacientes por Mes - Con Gemini AI
        binding.cardPatientsByMonth.setOnClickListener {
            navigateToAiReport("patients_trend")
        }

        // Citas por Estado - Con Gemini AI
        binding.cardAppointmentsByStatus.setOnClickListener {
            navigateToAiReport("appointments_status")
        }

        // Ingresos Mensuales
        binding.cardMonthlyRevenue.setOnClickListener {
            showMessage("Generando reporte de ingresos mensuales...")
            // TODO: Implementar análisis de ingresos
        }

        // Reporte Personalizado
        binding.cardCustomReport.setOnClickListener {
            showCustomReportDialog()
        }

        // Exportar Datos
        binding.cardExportData.setOnClickListener {
            showExportDialog()
        }
    }
    
    private fun showCustomReportDialog() {
        val dialog = CustomReportDialog.newInstance()
        dialog.onGenerateReport = { reportType, startDate, endDate ->
            // Navegar al reporte con los parámetros personalizados
            navigateToAiReport(reportType)
            showMessage("Generando reporte personalizado...")
        }
        dialog.show(childFragmentManager, CustomReportDialog.TAG)
    }
    
    private fun showExportDialog() {
        val dialog = ExportDialog.newInstance()
        dialog.onExport = { format, dataType, startDate, endDate ->
            handleExport(format, dataType, startDate, endDate)
        }
        dialog.show(childFragmentManager, ExportDialog.TAG)
    }
    
    private fun handleExport(
        format: ExportDialog.ExportFormat,
        dataType: ExportDialog.ExportDataType,
        startDate: Long?,
        endDate: Long?
    ) {
        val formatName = when (format) {
            ExportDialog.ExportFormat.PDF -> "PDF"
            ExportDialog.ExportFormat.EXCEL -> "Excel"
            ExportDialog.ExportFormat.CSV -> "CSV"
        }
        
        val dataTypeName = when (dataType) {
            ExportDialog.ExportDataType.PATIENTS -> "Pacientes"
            ExportDialog.ExportDataType.APPOINTMENTS -> "Citas"
            ExportDialog.ExportDataType.ALL -> "Todos los datos"
        }
        
        showMessage("Exportando $dataTypeName en formato $formatName...")
        // TODO: Implementar la lógica de exportación real
    }

    private fun navigateToAiReport(reportType: String) {
        val action = ReportsFragmentDirections.actionReportsFragmentToAiReportFragment(reportType)
        findNavController().navigate(action)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
