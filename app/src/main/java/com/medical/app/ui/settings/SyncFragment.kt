package com.medical.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkInfo
import com.google.android.material.snackbar.Snackbar
import com.medical.app.databinding.FragmentSyncBinding
import com.medical.app.viewmodel.SyncViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment para controlar la sincronización de datos
 */
@AndroidEntryPoint
class SyncFragment : Fragment() {
    
    private var _binding: FragmentSyncBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SyncViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSyncBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        // Observar estado de UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
        
        // Observar número de cambios no sincronizados
        viewModel.unsyncedCount.observe(viewLifecycleOwner) { count ->
            binding.tvUnsyncedCount.text = "$count cambios pendientes"
            
            if (count > 0) {
                binding.cardUnsyncedChanges.visibility = View.VISIBLE
            } else {
                binding.cardUnsyncedChanges.visibility = View.GONE
            }
        }
        
        // Observar estado del trabajo de sincronización
        viewModel.syncWorkStatus.observe(viewLifecycleOwner) { workInfoList ->
            val workInfo = workInfoList.firstOrNull()
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {
                    binding.progressSync.visibility = View.VISIBLE
                    binding.tvSyncStatus.text = "Sincronizando..."
                }
                WorkInfo.State.SUCCEEDED -> {
                    binding.progressSync.visibility = View.GONE
                    binding.tvSyncStatus.text = "Sincronización completada"
                }
                WorkInfo.State.FAILED -> {
                    binding.progressSync.visibility = View.GONE
                    binding.tvSyncStatus.text = "Error en sincronización"
                }
                else -> {
                    binding.progressSync.visibility = View.GONE
                }
            }
        }
    }
    
    private fun setupListeners() {
        // Botón de sincronización manual
        binding.btnSyncNow.setOnClickListener {
            viewModel.startSync()
        }
        
        // Switch de sincronización automática
        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.schedulePeriodicSync()
                showSnackbar("Sincronización automática activada")
            } else {
                viewModel.cancelPeriodicSync()
                showSnackbar("Sincronización automática desactivada")
            }
        }
        
        // Botón de sincronización única
        binding.btnScheduleSync.setOnClickListener {
            viewModel.scheduleSyncOnce()
            showSnackbar("Sincronización programada")
        }
        
        // Botón para cancelar sincronizaciones
        binding.btnCancelSync.setOnClickListener {
            viewModel.cancelAllSync()
            showSnackbar("Sincronizaciones canceladas")
        }
    }
    
    private fun updateUI(state: com.medical.app.viewmodel.SyncUiState) {
        // Actualizar estado de carga
        if (state.isLoading) {
            binding.progressMain.visibility = View.VISIBLE
            binding.contentContainer.visibility = View.GONE
        } else {
            binding.progressMain.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
        }
        
        // Actualizar estado de sincronización
        if (state.isSyncing) {
            binding.progressSync.visibility = View.VISIBLE
            binding.btnSyncNow.isEnabled = false
            binding.tvSyncStatus.text = "Sincronizando..."
        } else {
            binding.progressSync.visibility = View.GONE
            binding.btnSyncNow.isEnabled = true
        }
        
        // Actualizar estado de conexión
        state.syncStatus?.let { status ->
            binding.tvConnectionStatus.text = if (status.isConnected) {
                "Conectado a Supabase"
            } else {
                "Sin conexión"
            }
            
            binding.iconConnectionStatus.setImageResource(
                if (status.isConnected) {
                    android.R.drawable.presence_online
                } else {
                    android.R.drawable.presence_offline
                }
            )
            
            // Actualizar contador de errores
            if (status.errorCount > 0) {
                binding.cardErrors.visibility = View.VISIBLE
                binding.tvErrorCount.text = "${status.errorCount} errores de sincronización"
            } else {
                binding.cardErrors.visibility = View.GONE
            }
        }
        
        // Mostrar mensaje del último resultado
        state.lastSyncMessage?.let { message ->
            binding.tvLastSyncResult.text = message
            binding.tvLastSyncResult.visibility = View.VISIBLE
        }
        
        // Mostrar detalles del resultado si existen
        state.syncResult?.let { result ->
            if (result.entityResults.isNotEmpty()) {
                val details = buildString {
                    appendLine("Detalles de sincronización:")
                    result.entityResults.forEach { entity ->
                        appendLine("${entity.entityType}: ↑${entity.uploaded} ↓${entity.downloaded} ❌${entity.errors}")
                    }
                }
                binding.tvSyncDetails.text = details
                binding.tvSyncDetails.visibility = View.VISIBLE
            }
        }
        
        // Mostrar error si existe
        state.error?.let { error ->
            showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// SyncUiState is now defined in SyncViewModel.kt - no need for duplicate definition
