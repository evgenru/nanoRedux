package tk.ivpe.ui.common.store

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import tk.ivpe.logs.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

abstract class ReduxStoreViewModel<S : ReduxStore.State, A : ReduxStore.Action, E : ReduxStore.Effect> :
    ViewModel(), ReduxStore<S, A, E>, CoroutineScope by CoroutineScope(Dispatchers.Main),
    KoinComponent {

    protected abstract val logTag: String

    private val _state = MutableStateFlow<S?>(null)
    private val _sideEffect = Channel<E>(Channel.BUFFERED)
    override fun observeState(): StateFlow<S?> = _state
    override fun observeSideEffect(): Flow<E> = _sideEffect.receiveAsFlow().onEach {
        log?.d(logTag, "effect $it")
    }

    private val actions = Channel<A>(Channel.BUFFERED)

    init {
        launch {
            actions.receiveAsFlow().collect {
                dispatchAction(it)
            }
        }
    }

    override fun dispatch(action: A) {
        actions.trySend(action)
    }

    private fun dispatchAction(action: A) {
        log?.d(logTag, "start $action")
        val oldState = _state.value

        runCatching {
            val newState = doAction(action, oldState)

            if (newState != oldState) {
                log?.d(logTag, "end newState: $newState")
                _state.value = newState
            }
        }.onFailure {
            log?.e(logTag, "dispatchError", it)
        }
    }

    abstract fun doAction(action: A, oldState: S?): S?

    protected fun sendEffect(e: E) {
        _sideEffect.trySend(e)
    }

    fun reset() {
        _state.tryEmit(null)
    }
}

interface ReduxStore<S : ReduxStore.State, A : ReduxStore.Action, E : ReduxStore.Effect> {
    fun observeState(): StateFlow<S?>
    fun observeSideEffect(): Flow<E>
    fun dispatch(action: A)

    interface State
    interface Action
    interface Effect
}


/** Вызывает лямбду для дочернего типа, или возвращает исходный класс
 *
 * Если лябда возвращает null, то возвращает исходный класс
 *
 * *state.updateAs { state: State.Data -> state.copy(...) }*
 *
 * *state.updateAs { state: State.Data -> if (state.aaa) state.copy(...) else null }*
 */
inline fun <T : ReduxStore.State?, reified S : T> T.updateAs(update: (state: S) -> T?): T {
    return (this as? S)?.let { update(this) } ?: this
}
