package com.medical.app.ui.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.medical.app.data.model.Patient
import com.medical.app.databinding.FragmentPatientDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PatientDetailFragment : Fragment() {

    private var _binding: FragmentPatientDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PatientDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.patient.collect { patient ->
                patient?.let { updateUi(it) }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateUi(patient: Patient) {
        binding.apply {
            tvPatientName.text = "${patient.name} ${patient.lastName}"
            // El layout usa tvPatientInfo para combinar datos, aquí ponemos el género.
            // Lo ideal sería calcular la edad desde patient.birthdate y añadirla.
            tvPatientInfo.text = patient.gender
            tvPhoneNumber.text = patient.phone
            tvEmail.text = patient.email
            tvAddress.text = patient.address
            tvBloodType.text = patient.bloodType // Corregido el typo y el ID
            tvAllergies.text = patient.allergies
            tvNotes.text = patient.notes
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
