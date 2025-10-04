package com.medical.app.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Clase base para todos los ViewModels de la aplicación.
 * Proporciona funcionalidades comunes como el manejo de estados de carga y errores.
 */
abstract class BaseViewModel<State : Any, Event : Any> : ViewModel() {

    protected val _state = MutableStateFlow<State?>(null)
    val state: StateFlow<State?> = _state

    protected val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    protected val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error

    protected val _event = SingleLiveEvent<Event>(
        replayCache = TODO(),
        subscriptionCount = TODO()
    )
    val event: SingleLiveEvent<Event> = _event

    protected fun setLoading(isLoading: Boolean) {
        _loading.value = isLoading
    }

    protected fun setError(throwable: Throwable) {
        _error.value = throwable
    }

    protected fun setState(newState: State) {
        _state.value = newState
    }

    protected fun postEvent(event: Event) {
        _event.value = event
    }

    protected fun launchDataLoad(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                setLoading(true)
                block()
            } catch (e: Exception) {
                setError(e)
            } finally {
                setLoading(false)
            }
        }
    }
}

/**
 * Clase de utilidad para eventos que solo deben ser manejados una vez, como navegación o mensajes.
 */
class SingleLiveEvent<T>(
    override val replayCache: List<T?>,
    override val subscriptionCount: StateFlow<Int>
) : MutableStateFlow<T?> {
    private val pending = MutableStateFlow(false)
    private var _value: T? = null
    override var value: T?
        get() = _value
        set(value) {
            pending.value = true
            _value = value
        }

    override fun compareAndSet(expect: T?, update: T?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun collect(collector: FlowCollector<T?>): Nothing {
        collect { value ->
            if (pending.value) {
                pending.value = false
                collector.emit(value)
            }
        }
    }

    override suspend fun emit(value: T?) {
        TODO("Not yet implemented")
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        TODO("Not yet implemented")
    }

    override fun tryEmit(value: T?): Boolean {
        TODO("Not yet implemented")
    }
}
