package com.medical.app.ui.medico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.model.Medico
import com.medical.app.databinding.ItemMedicoBinding

class MedicoAdapter(
    private val onItemClick: (Medico) -> Unit
) : ListAdapter<Medico, MedicoAdapter.MedicoViewHolder>(MedicoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicoViewHolder {
        val binding = ItemMedicoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MedicoViewHolder, position: Int) {
        val medico = getItem(position)
        holder.bind(medico)
    }

    inner class MedicoViewHolder(
        private val binding: ItemMedicoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(medico: Medico) {
            binding.apply {
                // Asignar datos del médico a las vistas
                tvMedicoNombre.text = medico.nombre ?: "Nombre no disponible"
                tvEspecialidad.text = medico.especialidad ?: "Sin especialidad"
                
                // Puedes cargar la imagen del médico aquí usando Glide o Coil
                // Glide.with(imgMedico).load(medico.fotoUrl).into(imgMedico)
                
                // Mostrar calificación si está disponible
                medico.calificacion?.let { calificacion ->
                    ratingBar.rating = calificacion.toFloat()
                    tvCalificacion.text = String.format("%.1f", calificacion)
                } ?: run {
                    ratingBar.visibility = View.GONE
                    tvCalificacion.visibility = View.GONE
                }
                
                // Mostrar disponibilidad
                tvDisponibilidad.text = if (medico.disponible == true) {
                    "Disponible"
                } else {
                    "No disponible"
                }
            }
        }
    }
}

class MedicoDiffCallback : DiffUtil.ItemCallback<Medico>() {
    override fun areItemsTheSame(oldItem: Medico, newItem: Medico): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Medico, newItem: Medico): Boolean {
        return oldItem == newItem
    }
}
