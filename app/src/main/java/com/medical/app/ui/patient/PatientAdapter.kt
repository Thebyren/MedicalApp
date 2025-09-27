package com.medical.app.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.model.Patient
import com.medical.app.databinding.ItemPatientBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para mostrar la lista de pacientes con paginación
 */
class PatientAdapter(
    private val onPatientClick: (Patient) -> Unit
) : PagingDataAdapter<Patient, PatientAdapter.PatientViewHolder>(PatientDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientViewHolder(binding, onPatientClick)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        getItem(position)?.let { patient ->
            holder.bind(patient)
        }
    }

    /**
     * ViewHolder para cada ítem de paciente
     */
    class PatientViewHolder(
        private val binding: ItemPatientBinding,
        private val onPatientClick: (Patient) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Patient) {
            binding.apply {
                tvPatientName.text = patient.name
                tvPatientInfo.text = "${getFormattedAge(patient.birthdate)} • ${patient.gender}"
                tvPhoneNumber.text = patient.phone
                
                root.setOnClickListener { onPatientClick(patient) }
            }
        }

        private fun getFormattedAge(dob: Date): String {
            val today = Calendar.getInstance()
            val birthDate = Calendar.getInstance().apply { time = dob }
            var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
            
            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            
            return "$age años"
        }
    }

}

/**
 * Callback para determinar si dos ítems son iguales
 */
private object PatientDiffCallback : DiffUtil.ItemCallback<Patient>() {
    override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
        return oldItem == newItem
    }
}
