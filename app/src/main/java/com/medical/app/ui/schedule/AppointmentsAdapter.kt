package com.medical.app.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.R
import com.medical.app.data.dao.AppointmentWithPatient
import com.medical.app.data.entities.Appointment
import com.medical.app.databinding.ItemAppointmentBinding
import java.text.SimpleDateFormat
import java.util.*

class AppointmentsAdapter(
    private val onItemClick: (AppointmentWithPatient) -> Unit,
    private val onStatusChange: (Appointment, Appointment.AppointmentStatus) -> Unit
) : ListAdapter<AppointmentWithPatient, AppointmentsAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppointmentViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointmentWithPatient: AppointmentWithPatient) {
            val appointment = appointmentWithPatient.appointment
            binding.apply {
                tvTime.text = timeFormat.format(appointment.dateTime)
                // Mostrar título con nombre del paciente
                tvTitle.text = "${appointment.title} - ${appointmentWithPatient.fullPatientName}"
                tvType.text = appointment.type
                tvDuration.text = "${appointment.duration} min"
                
                appointment.description?.let {
                    tvDescription.text = it
                }

                // Color según estado
                val statusColor = when (appointment.status) {
                    Appointment.AppointmentStatus.SCHEDULED -> R.color.blue_500
                    Appointment.AppointmentStatus.CONFIRMED -> R.color.green_500
                    Appointment.AppointmentStatus.IN_PROGRESS -> R.color.orange_500
                    Appointment.AppointmentStatus.COMPLETED -> R.color.grey_500
                    Appointment.AppointmentStatus.CANCELLED -> R.color.red_500
                    Appointment.AppointmentStatus.NO_SHOW -> R.color.red_700
                }
                
                statusIndicator.setBackgroundColor(
                    ContextCompat.getColor(root.context, statusColor)
                )
                
                // Texto del estado
                tvStatus.text = when (appointment.status) {
                    Appointment.AppointmentStatus.SCHEDULED -> "Programada"
                    Appointment.AppointmentStatus.CONFIRMED -> "Confirmada"
                    Appointment.AppointmentStatus.IN_PROGRESS -> "En progreso"
                    Appointment.AppointmentStatus.COMPLETED -> "Completada"
                    Appointment.AppointmentStatus.CANCELLED -> "Cancelada"
                    Appointment.AppointmentStatus.NO_SHOW -> "No asistió"
                }
                
                tvStatus.setTextColor(
                    ContextCompat.getColor(root.context, statusColor)
                )

                root.setOnClickListener {
                    onItemClick(appointmentWithPatient)
                }

                // Botones de acción rápida
                btnComplete.setOnClickListener {
                    onStatusChange(appointment, Appointment.AppointmentStatus.COMPLETED)
                }

                btnCancel.setOnClickListener {
                    onStatusChange(appointment, Appointment.AppointmentStatus.CANCELLED)
                }
            }
        }
    }

    private class AppointmentDiffCallback : DiffUtil.ItemCallback<AppointmentWithPatient>() {
        override fun areItemsTheSame(oldItem: AppointmentWithPatient, newItem: AppointmentWithPatient): Boolean {
            return oldItem.appointment.id == newItem.appointment.id
        }

        override fun areContentsTheSame(oldItem: AppointmentWithPatient, newItem: AppointmentWithPatient): Boolean {
            return oldItem == newItem
        }
    }
}
