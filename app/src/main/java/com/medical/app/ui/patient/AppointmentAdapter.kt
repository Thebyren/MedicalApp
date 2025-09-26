package com.medical.app.ui.patient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.model.Appointment
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
            
            binding.btnViewDetails.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(appointment: Appointment) {
            binding.apply {
                // Formatear fecha y hora
                val date = Date(appointment.dateTime.time)
                tvAppointmentDate.text = dateFormat.format(date)
                tvAppointmentTime.text = timeFormat.format(date)
                
                // Mostrar título y descripción
                tvAppointmentTitle.text = appointment.title
                
                // Mostrar nombre del doctor (si está disponible)
                // TODO: Cargar nombre del doctor desde el repositorio
                tvAppointmentDoctor.text = "Dr. " + (appointment.doctorId?.toString() ?: "Sin asignar")
                
                // Configurar estado de la cita (opcional)
                // cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, getStatusColor(appointment.status)))
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
