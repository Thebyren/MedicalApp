package com.medical.app.ui.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.entities.Consulta
import com.medical.app.databinding.ItemConsultaBinding

class PagingAdapter : PagingDataAdapter<Consulta, PagingAdapter.ConsultaViewHolder>(CONSULTA_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultaViewHolder {
        val binding = ItemConsultaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConsultaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsultaViewHolder, position: Int) {
        val consulta = getItem(position)
        if (consulta != null) {
            holder.bind(consulta)
        }
    }

    class ConsultaViewHolder(private val binding: ItemConsultaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(consulta: Consulta) {
            binding.tvMotivo.text = consulta.motivoConsulta
            binding.tvFecha.text = consulta.fechaConsulta.toString()
        }
    }

    companion object {
        private val CONSULTA_COMPARATOR = object : DiffUtil.ItemCallback<Consulta>() {
            override fun areItemsTheSame(oldItem: Consulta, newItem: Consulta): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Consulta, newItem: Consulta): Boolean =
                oldItem == newItem
        }
    }
}
