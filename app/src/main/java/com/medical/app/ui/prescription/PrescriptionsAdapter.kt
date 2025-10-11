package com.medical.app.ui.prescription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.entities.Tratamiento
import com.medical.app.databinding.ItemPrescriptionBinding

class PrescriptionsAdapter(
    private val onItemClick: (Tratamiento) -> Unit,
    private val onDeleteClick: (Tratamiento) -> Unit
) : ListAdapter<Tratamiento, PrescriptionsAdapter.PrescriptionViewHolder>(PrescriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrescriptionViewHolder {
        val binding = ItemPrescriptionBinding.inflate(
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
        private val binding: ItemPrescriptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(prescription: Tratamiento) {
            binding.apply {
                tvMedicamento.text = prescription.medicamento
                tvDosis.text = "Dosis: ${prescription.dosis}"
                tvFrecuencia.text = "Frecuencia: ${prescription.frecuencia}"
                
                prescription.duracionDias?.let {
                    tvDuracion.text = "Duración: $it días"
                }
                
                prescription.indicaciones?.let {
                    tvIndicaciones.text = it
                }

                root.setOnClickListener {
                    onItemClick(prescription)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(prescription)
                }
            }
        }
    }

    private class PrescriptionDiffCallback : DiffUtil.ItemCallback<Tratamiento>() {
        override fun areItemsTheSame(oldItem: Tratamiento, newItem: Tratamiento): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tratamiento, newItem: Tratamiento): Boolean {
            return oldItem == newItem
        }
    }
}
