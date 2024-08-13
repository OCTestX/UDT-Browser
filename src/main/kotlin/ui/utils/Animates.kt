package ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

object Animates {
    @Composable
    inline fun VisibilityAnimates(delayTime: Long = 0, crossinline content: @Composable () -> Unit) {
        val state = remember { MutableTransitionState(false) }
        LaunchedEffect(Unit) {
            delay(delayTime)
            state.targetState = true
        }
        AnimatedVisibility(
            state
        ) {
            content()
        }
    }
}