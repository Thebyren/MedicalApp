// UpcomingAppointmentsAdapter.kt
package com.medical.app.ui.medico

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.R
import com.medical.app.data.model.Appointment
import java.text.SimpleDateFormat
import java.util.*

class UpcomingAppointmentsAdapter : 
    ListAdapter<Appointment, UpcomingAppointmentsAdapter.AppointmentViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_appointment, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = getItem(position)
        holder.bind(appointment)
    }

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPatientName: TextView = itemView.findViewById(R.id.tvPatientName)
        private val tvAppointmentTime: TextView = itemView.findViewById(R.id.tvAppointmentTime)
        private val tvAppointmentType: TextView = itemView.findViewById(R.id.tvAppointmentType)

        fun bind(appointment: Appointment) {
            tvPatientName.text = appointment.patientName
            tvAppointmentTime.text = formatAppointmentTime(appointment.appointmentTime)
            tvAppointmentType.text = appointment.type
            
            itemView.setOnClickListener {
                // Navegar al detalle de la cita
                // findNavController().navigate(MedicoHomeFragmentDirections.actionMedicoHomeFragmentToAppointmentDetailFragment(appointment.id))
            }
        }

        private fun formatAppointmentTime(date: Date): String {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            return timeFormat.format(date)
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