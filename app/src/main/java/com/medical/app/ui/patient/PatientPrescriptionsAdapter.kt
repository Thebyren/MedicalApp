package com.medical.app.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.R
import com.medical.app.databinding.ItemPatientPrescriptionBinding
import java.text.SimpleDateFormat
import java.util.*

class PatientPrescriptionsAdapter(
    private val onItemClick: (PatientPrescription) -> Unit
) : ListAdapter<PatientPrescription, PatientPrescriptionsAdapter.PrescriptionViewHolder>(PrescriptionDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val binding = ItemPatientPrescriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrescriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrescriptionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PrescriptionViewHolder(
        private val binding: ItemPatientPrescriptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(prescription: PatientPrescription) {
            binding.apply {
                tvMedicationName.text = prescription.medicationName
                tvDosage.text = "Dosis: ${prescription.dosage}"
                tvFrequency.text = "Frecuencia: ${prescription.frequency}"
                
                // Duración
                if (prescription.duration != null) {
                    layoutDuration.isVisible = true
                    tvDuration.text = "Duración: ${prescription.duration} días"
                } else {
                    layoutDuration.isVisible = false
                }
                
                // Indicaciones
                if (!prescription.instructions.isNullOrEmpty()) {
                    tvInstructions.isVisible = true
                    tvInstructions.text = prescription.instructions
                } else {
                    tvInstructions.isVisible = false
                }
                
                // Doctor
                tvDoctor.text = prescription.doctorName
                
                // Fecha de prescripción
                if (prescription.prescribedDate != null) {
                    tvDate.text = dateFormat.format(prescription.prescribedDate)
                } else {
                    tvDate.text = "Fecha no disponible"
                }
                
                // Estado (activa/inactiva)
                if (prescription.isActive) {
                    chipStatus.text = "Activa"
                    chipStatus.setChipBackgroundColorResource(R.color.green_100)
                    chipStatus.setTextColor(ContextCompat.getColor(root.context, R.color.green_700))
                } else {
                    chipStatus.text = "Finalizada"
                    chipStatus.setChipBackgroundColorResource(R.color.grey_100)
                    chipStatus.setTextColor(ContextCompat.getColor(root.context, R.color.grey_700))
                }
                
                root.setOnClickListener {
                    onItemClick(prescription)
                }
            }
        }
    }

    private class PrescriptionDiffCallback : DiffUtil.ItemCallback<PatientPrescription>() {
        override fun areItemsTheSame(oldItem: PatientPrescription, newItem: PatientPrescription): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PatientPrescription, newItem: PatientPrescription): Boolean {
            return oldItem == newItem
        }
    }
}
