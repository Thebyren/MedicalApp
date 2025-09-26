package com.medical.app.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Adaptador base para RecyclerView que maneja múltiples tipos de vistas y estados
 */
abstract class BaseAdapter<T : Any, VB : ViewBinding>(
    private val itemClickListener: ((T) -> Unit)? = null
) : RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {

    companion object {
        private const val TYPE_ITEM = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_ERROR = 2
        private const val TYPE_EMPTY = 3
    }

    private val diffCallback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return getItemId(oldItem) == getItemId(newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: T, newItem: T): Any? {
            return if (oldItem != newItem) true else null
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    private var isLoading = false
    private var error: Throwable? = null
    private var emptyMessage: String? = null

    /**
     * Obtiene el ID único para un ítem
     */
    abstract fun getItemId(item: T): Any

    /**
     * Crea un nuevo ViewHolder para el tipo de vista dado
     */
    abstract fun createBinding(inflater: LayoutInflater, parent: ViewGroup): VB

    /**
     * Vincula los datos a la vista
     */
    abstract fun bind(binding: VB, item: T, position: Int)

    /**
     * Configura la vista de carga
     */
    open fun createLoadingView(parent: ViewGroup): View? = null

    /**
     * Configura la vista de error
     */
    open fun createErrorView(parent: ViewGroup): View? = null

    /**
     * Configura la vista vacía
     */
    open fun createEmptyView(parent: ViewGroup): View? = null

    /**
     * Maneja el clic en el botón de reintentar
     */
    open fun onRetry() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        return when (viewType) {
            TYPE_LOADING -> {
                val view = createLoadingView(parent)
                    ?: throw IllegalStateException("Loading view not implemented")
                LoadingViewHolder(view)
            }
            TYPE_ERROR -> {
                val view = createErrorView(parent)
                    ?: throw IllegalStateException("Error view not implemented")
                ErrorViewHolder(view)
            }
            TYPE_EMPTY -> {
                val view = createEmptyView(parent)
                    ?: throw IllegalStateException("Empty view not implemented")
                EmptyViewHolder(view)
            }
            else -> {
                val binding = createBinding(LayoutInflater.from(parent.context), parent)
                ItemViewHolder(binding).apply {
                    itemView.setOnClickListener {
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            itemClickListener?.invoke(differ.currentList[position])
                        }
                    }
                }
            }
        } as BaseViewHolder<VB>
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = differ.currentList[position]
                bind(holder.binding, item, position)
            }
            is ErrorViewHolder -> {
                // Configurar vista de error si es necesario
            }
            is EmptyViewHolder -> {
                // Configurar vista vacía si es necesario
            }
            // LoadingViewHolder no necesita binding
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isLoading -> TYPE_LOADING
            error != null -> TYPE_ERROR
            differ.currentList.isEmpty() -> TYPE_EMPTY
            else -> TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return when {
            isLoading || error != null || differ.currentList.isEmpty() -> 1
            else -> differ.currentList.size
        }
    }

    /**
     * Establece los datos en el adaptador
     */
    fun submitList(list: List<T>) {
        error = null
        isLoading = false
        differ.submitList(list)
    }

    /**
     * Muestra el estado de carga
     */
    fun showLoading() {
        error = null
        isLoading = true
        notifyDataSetChanged()
    }

    /**
     * Muestra un error
     */
    fun showError(throwable: Throwable) {
        error = throwable
        isLoading = false
        notifyDataSetChanged()
    }

    /**
     * Muestra el estado vacío
     */
    fun showEmpty(message: String? = null) {
        error = null
        isLoading = false
        emptyMessage = message
        differ.submitList(emptyList())
    }

    /**
     * Obtiene el ítem en la posición especificada
     */
    fun getItem(position: Int): T? {
        return if (position in 0 until itemCount) {
            differ.currentList[position]
        } else {
            null
        }
    }

    // Clases base para los ViewHolders
    abstract class BaseViewHolder<VB : ViewBinding>(view: View) : RecyclerView.ViewHolder(view)

    class ItemViewHolder<VB : ViewBinding>(val binding: VB) : BaseViewHolder<VB>(binding.root)
    class LoadingViewHolder<VB : ViewBinding>(view: View) : BaseViewHolder<VB>(view)
    class ErrorViewHolder<VB : ViewBinding>(view: View) : BaseViewHolder<VB>(view)
    class EmptyViewHolder<VB : ViewBinding>(view: View) : BaseViewHolder<VB>(view)
}
