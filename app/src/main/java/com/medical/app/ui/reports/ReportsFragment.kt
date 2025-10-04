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
            showMessage("Abriendo configuración de reporte personalizado...")
            // TODO: Navegar a pantalla de configuración de reporte personalizado
        }

        // Exportar Datos
        binding.cardExportData.setOnClickListener {
            showMessage("Abriendo opciones de exportación...")
            // TODO: Mostrar diálogo de opciones de exportación (PDF, Excel)
        }
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
