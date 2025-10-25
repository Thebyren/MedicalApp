package com.medical.app.ui.prescription

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.medical.app.data.entities.Tratamiento
import com.medical.app.databinding.DialogAddPrescriptionBinding

class AddPrescriptionDialog : DialogFragment() {

    private var _binding: DialogAddPrescriptionBinding? = null
    private val binding get() = _binding!!
    
    var onPrescriptionCreated: ((Tratamiento) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPrescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                createPrescription()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validar medicamento
        if (binding.etMedicamento.text.isNullOrBlank()) {
            binding.tilMedicamento.error = "Ingrese el nombre del medicamento"
            isValid = false
        } else {
            binding.tilMedicamento.error = null
        }
        
        // Validar dosis
        if (binding.etDosis.text.isNullOrBlank()) {
            binding.tilDosis.error = "Ingrese la dosis"
            isValid = false
        } else {
            binding.tilDosis.error = null
        }
        
        // Validar frecuencia
        if (binding.etFrecuencia.text.isNullOrBlank()) {
            binding.tilFrecuencia.error = "Ingrese la frecuencia"
            isValid = false
        } else {
            binding.tilFrecuencia.error = null
        }
        
        return isValid
    }

    private fun createPrescription() {
        val medicamento = binding.etMedicamento.text.toString()
        val dosis = binding.etDosis.text.toString()
        val frecuencia = binding.etFrecuencia.text.toString()
        val duracionText = binding.etDuracion.text.toString()
        val duracion = if (duracionText.isNotBlank()) duracionText.toIntOrNull() else null
        val indicaciones = binding.etIndicaciones.text.toString().takeIf { it.isNotBlank() }
        
        // Crear tratamiento sin consulta asociada (prescripción independiente)
        val tratamiento = Tratamiento(
            consultaId = null, // Prescripción independiente, no asociada a una consulta
            medicamento = medicamento,
            dosis = dosis,
            frecuencia = frecuencia,
            duracionDias = duracion,
            indicaciones = indicaciones
        )
        
        onPrescriptionCreated?.invoke(tratamiento)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddPrescriptionDialog"
        
        fun newInstance(): AddPrescriptionDialog {
            return AddPrescriptionDialog()
        }
    }
}
