package com.medical.app.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.entities.Appointment
import com.medical.app.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private val onItemClick: (Appointment) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = getItem(position)
        holder.bind(appointment)
    }

    inner class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(appointment: Appointment) {
            binding.apply {
                // Formatear hora
                tvTime.text = timeFormat.format(appointment.dateTime)
                
                // Mostrar título y tipo
                tvTitle.text = appointment.title
                tvType.text = appointment.type
                tvDuration.text = "${appointment.duration} min"
                
                // Mostrar descripción si existe
                appointment.description?.let {
                    tvDescription.text = it
                }
                
                // Mostrar estado
                tvStatus.text = when (appointment.status) {
                    Appointment.AppointmentStatus.SCHEDULED -> "Programada"
                    Appointment.AppointmentStatus.CONFIRMED -> "Confirmada"
                    Appointment.AppointmentStatus.IN_PROGRESS -> "En progreso"
                    Appointment.AppointmentStatus.COMPLETED -> "Completada"
                    Appointment.AppointmentStatus.CANCELLED -> "Cancelada"
                    Appointment.AppointmentStatus.NO_SHOW -> "No asistió"
                }
            }
        }
    }
}

class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {
    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }
}
