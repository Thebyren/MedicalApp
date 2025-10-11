package com.medical.app.ui.historial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.entities.Consulta
import com.medical.app.databinding.ItemConsultaHistorialBinding
import java.text.SimpleDateFormat
import java.util.*

class HistorialConsultasAdapter(
    private val onItemClick: (Consulta) -> Unit
) : ListAdapter<Consulta, HistorialConsultasAdapter.ConsultaViewHolder>(ConsultaDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val binding = ItemConsultaHistorialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConsultaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        val consulta = getItem(position)
        holder.bind(consulta)
    }

    inner class ConsultaViewHolder(
        private val binding: ItemConsultaHistorialBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            
            binding.btnVerDetalle.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(consulta: Consulta) {
            binding.apply {
                // Formatear fecha y hora
                val fecha = Date(consulta.fechaConsulta.time)
                tvFecha.text = dateFormat.format(fecha)
                tvHora.text = timeFormat.format(fecha)
                
                // Mostrar información de la consulta
                tvMotivo.text = consulta.motivoConsulta.takeIf { it.isNotBlank() } ?: "Sin motivo"
                tvDiagnostico.text = consulta.diagnostico?.takeIf { it.isNotBlank() } ?: "Sin diagnóstico"
                
                // Mostrar próxima cita si existe
                consulta.proximaCita?.let { proximaCita ->
                    val proximaCitaStr = dateFormat.format(proximaCita)
                    tvProximaCita.text = proximaCitaStr
                    tvProximaCita.visibility = View.VISIBLE
                    tvProximaCitaLabel.visibility = View.VISIBLE
                } ?: run {
                    tvProximaCita.visibility = View.GONE
                    tvProximaCitaLabel.visibility = View.GONE
                }
            }
        }
    }
}

class ConsultaDiffCallback : DiffUtil.ItemCallback<Consulta>() {
    override fun areItemsTheSame(oldItem: Consulta, newItem: Consulta): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Consulta, newItem: Consulta): Boolean {
        return oldItem == newItem
    }
}
