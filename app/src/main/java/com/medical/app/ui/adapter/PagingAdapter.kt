package com.medical.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.medical.app.data.remote.dto.YourDataModel
import com.medical.app.databinding.ItemYourDataBinding

class PagingAdapter : PagingDataAdapter<YourDataModel, PagingAdapter.YourDataViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YourDataViewHolder {
        val binding = ItemYourDataBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return YourDataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: YourDataViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    inner class YourDataViewHolder(
        private val binding: ItemYourDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: YourDataModel) {
            binding.apply {
                // Aquí vinculas los datos con las vistas
                // Ejemplo:
                // textTitle.text = item.title
                // textDescription.text = item.description
                
                // Manejar clics si es necesario
                root.setOnClickListener {
                    // Manejar clic en el ítem
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<YourDataModel>() {
            override fun areItemsTheSame(oldItem: YourDataModel, newItem: YourDataModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: YourDataModel, newItem: YourDataModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
