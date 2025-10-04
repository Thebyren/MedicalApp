package com.medical.app.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.medical.app.databinding.FragmentAiReportBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AiReportFragment : Fragment() {

    private var _binding: FragmentAiReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AiReportViewModel by viewModels()
    private val args: AiReportFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupObservers()
        
        // Generar el reporte automáticamente al cargar
        generateReport()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.title = when (args.reportType) {
            "general_stats" -> "Estadísticas Generales"
            "patients_trend" -> "Tendencia de Pacientes"
            "appointments_status" -> "Análisis de Citas"
            else -> "Reporte AI"
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
                binding.contentLayout.isVisible = !state.isLoading && state.report != null
                binding.errorLayout.isVisible = !state.isLoading && state.error != null
                
                state.report?.let { report ->
                    binding.tvReportContent.text = report
                }
                
                state.error?.let { error ->
                    binding.tvError.text = error
                }
            }
        }
    }

    private fun generateReport() {
        when (args.reportType) {
            "general_stats" -> viewModel.generateGeneralStatsReport()
            "patients_trend" -> viewModel.generatePatientsTrendReport()
            "appointments_status" -> viewModel.generateAppointmentsAnalysis()
            else -> showError("Tipo de reporte no soportado")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
