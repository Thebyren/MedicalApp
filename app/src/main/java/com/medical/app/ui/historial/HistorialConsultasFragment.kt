package com.medical.app.ui.historial

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.medical.app.R
import com.medical.app.data.model.Consulta
import com.medical.app.databinding.FragmentHistorialConsultasBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HistorialConsultasFragment : Fragment() {

    private var _binding: FragmentHistorialConsultasBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HistorialConsultasViewModel by viewModels()
    private val args: HistorialConsultasFragmentArgs by navArgs()
    
    private lateinit var consultasAdapter: HistorialConsultasAdapter
    
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateRangeFormat = SimpleDateFormat("dd MMM - dd MMM yyyy", Locale.getDefault())
    private var searchQuery: String = ""
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialConsultasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupSearchView()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        
        // Cargar consultas del paciente
        viewModel.setPatientId(args.patientId)
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Configurar menú de búsqueda
        binding.toolbar.inflateMenu(R.menu.menu_historial_consultas)
        
        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        searchView = searchItem?.actionView as? SearchView
        
        searchView?.queryHint = getString(R.string.buscar_consultas)
        
        // Configurar listener de búsqueda con debounce
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText ?: ""
                viewModel.setSearchQuery(searchQuery)
                return true
            }
        })
    }
    
    private fun setupSearchView() {
        binding.searchView.setupWithSearchBar(binding.searchBar)
        
        // Configurar búsqueda con debounce
        binding.searchView
            .editText
            .doAfterTextChanged { editable ->
                searchQuery = editable?.toString() ?: ""
                viewModel.setSearchQuery(searchQuery)
            }
    }
    
    private fun setupRecyclerView() {
        consultasAdapter = HistorialConsultasAdapter { consulta ->
            // Navegar al detalle de la consulta
            // findNavController().navigate(
            //     HistorialConsultasFragmentDirections.actionToConsultaDetalle(consulta.id)
            // )
        }
        
        binding.rvConsultas.adapter = consultasAdapter
    }
    
    private fun setupClickListeners() {
        // Botón para seleccionar rango de fechas
        binding.btnFiltrarFechas.setOnClickListener {
            showDateRangePicker()
        }
        
        // Chip para limpiar filtro de fechas
        binding.chipRangoFechas.setOnCloseIconClickListener {
            viewModel.clearDateRange()
            binding.chipRangoFechas.visibility = View.GONE
        }
    }
    
    private fun setupObservers() {
        viewModel.filteredConsultas
            .onEach { consultas ->
                consultasAdapter.submitList(consultas)
                updateEmptyState(consultas.isEmpty())
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
                viewModel.clearError()
            }
        }
        
        // Observar cambios en el rango de fechas
        viewModel.uiState
            .map { it.dateRange }
            .distinctUntilChanged()
            .onEach { dateRange ->
                dateRange?.let { (start, end) ->
                    binding.chipRangoFechas.text = "${dateFormat.format(start)} - ${dateFormat.format(end)}"
                    binding.chipRangoFechas.visibility = View.VISIBLE
                } ?: run {
                    binding.chipRangoFechas.visibility = View.GONE
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        val noResults = isEmpty && (searchQuery.isNotEmpty() || 
                binding.chipRangoFechas.visibility == View.VISIBLE)
        
        binding.layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvConsultas.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (isEmpty) {
            val titleView = binding.layoutEmptyState.findViewById<TextView>(R.id.tvEmptyTitle)
            val subtitleView = binding.layoutEmptyState.findViewById<TextView>(R.id.tvEmptySubtitle)
            
            when {
                noResults -> {
                    titleView.text = getString(R.string.sin_resultados)
                    subtitleView.text = getString(R.string.intenta_cambiar_filtros)
                }
                searchQuery.isNotEmpty() -> {
                    titleView.text = getString(R.string.sin_consultas_busqueda, "\"$searchQuery\"")
                    subtitleView.text = getString(R.string.intenta_otra_busqueda)
                }
                else -> {
                    titleView.text = getString(R.string.sin_consultas)
                    subtitleView.text = getString(R.string.no_hay_consultas_registradas_periodo)
                }
            }
        }
    }
    
    private fun showDateRangePicker() {
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.now())
        
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.seleccionar_rango_fechas))
            .setCalendarConstraints(constraintsBuilder.build())
            .build()
        
        dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
            val startDate = Date(dateRange.first ?: return@addOnPositiveButtonClickListener)
            val endDate = Date(dateRange.second ?: return@addOnPositiveButtonClickListener)
            
            // Asegurarse de que la fecha de fin sea al final del día
            val calendar = Calendar.getInstance().apply { 
                time = endDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            
            viewModel.setDateRange(startDate, calendar.time)
        }
        
        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }
    
    private fun showError(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
    
    override fun onDestroyView() {
        searchView?.setOnQueryTextListener(null)
        _binding = null
        super.onDestroyView()
    }
}
