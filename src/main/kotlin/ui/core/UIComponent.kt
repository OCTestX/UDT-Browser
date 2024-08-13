package ui.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class UIComponent<A: UIComponent.UIAction, S: UIComponent.UIState<A>> {
    protected val ioScope = CoroutineScope(Dispatchers.IO)
    protected lateinit var scope: CoroutineScope private set
    @Composable
    fun Main() {
        scope = rememberCoroutineScope()
        val state = Presenter()
        UI(state)
    }

    @Composable
    protected abstract fun UI(state: S)

    @Composable
    protected abstract fun Presenter(): S


    abstract class UIState<A>(
        val action: (A) -> Unit
    )
    abstract class UIAction {

    }
}