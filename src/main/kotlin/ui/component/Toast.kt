package ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.AccessibilityManager
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

interface ToastData {
    val message: String
    val icon: ImageVector?
    var shaking: Boolean
    fun shake()
    val animationDuration: StateFlow<Int?>
    val type: ToastModel.Type
    suspend fun run(accessibilityManager: AccessibilityManager?)
    fun pause()
    fun resume()
    fun dismiss()
    fun dismissed()
}

data class ToastModel(
    val message: String,
    val icon: ImageVector? = null,
    val type: Type
) {
    enum class Type {
        Normal, Success, Info, Warning, Error,
    }
}

private data class ColorData(
    val backgroundColor: Color,
    val textColor: Color,
    val iconColor: Color,
    val icon: ImageVector? = null,
)

@Composable
fun Toast(
    toastData: ToastData,
) {

    val animateDuration by toastData.animationDuration.collectAsState()

    val colorData = when (toastData.type) {
        ToastModel.Type.Normal -> ColorData(
            backgroundColor = MaterialTheme.colorScheme.background,
            textColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.Notifications,
        )

        ToastModel.Type.Success -> ColorData(
            backgroundColor = Color.Green,
            textColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.Check,
        )

        ToastModel.Type.Info -> ColorData(
            backgroundColor = Color.Gray,
            textColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.Info,

            )

        ToastModel.Type.Warning -> ColorData(
            backgroundColor = Color.Yellow,
            textColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.Warning,

            )

        ToastModel.Type.Error -> ColorData(
            backgroundColor = Color.Red,
            textColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.Close,
        )
    }
    val icon = toastData.icon ?: colorData.icon
    key(toastData) {
        Toast(
            message = toastData.message,
            icon = icon,
            shaking = toastData.shaking,
            shaken = { toastData.shaking = false },
            backgroundColor = colorData.backgroundColor,
            iconColor = colorData.iconColor,
            textColor = colorData.textColor,
            animateDuration = animateDuration,
            onPause = toastData::pause,
            onResume = toastData::resume,
            onDismissed = toastData::dismissed,
        )

    }
}

@Composable
private fun Toast(
    message: String,
    icon: ImageVector?,
    shaking: Boolean = false,
    shaken: () -> Unit = {},
    backgroundColor: Color,
    iconColor: Color,
    textColor: Color,
    animateDuration: Int? = 0,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onDismissed: () -> Unit = {},
) {
    val roundedValue = 26.dp
    Surface(
        modifier = Modifier
            .systemBarsPadding()
            .padding(8.dp)
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .toastGesturesDetector(onPause, onResume, onDismissed),
        color = backgroundColor,
        shape = RoundedCornerShape(roundedValue),
        tonalElevation = 2.dp,
    ) {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(animateDuration) {
            // Do not run animation when animations are turned off.

            if (coroutineContext.durationScale == 0f) return@LaunchedEffect

            if (animateDuration == null) {
                progress.stop()
            } else {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animateDuration,
                        easing = EaseInCubic,
                    ),
                )
            }
        }

        val color = LocalContentColor.current
        Row(
            Modifier
                .drawBehind {
                    val fraction = progress.value * size.width
                    drawRoundRect(
                        color = color,
                        size = Size(width = fraction, height = size.height),
                        cornerRadius = CornerRadius(roundedValue.toPx()),
                        alpha = 0.1f,
                    )
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    Modifier.size(24.dp),
                    tint = iconColor
                )
            }
            Text(message, color = textColor, fontSize = 15.sp)
        }
    }
}

private fun Modifier.toastGesturesDetector(
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDismissed: () -> Unit,
): Modifier = composed {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    pointerInput(Unit) {
        val decay = splineBasedDecay<Float>(this)
        coroutineScope {
            while (true) {
                awaitPointerEventScope {
                    // Detect a touch down event.
                    val down = awaitFirstDown()
                    onPause()
                    val pointerId = down.id

                    val velocityTracker = VelocityTracker()
                    // Stop any ongoing animation.
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        offsetY.stop()
                        alpha.stop()
                    }

                    verticalDrag(pointerId) { change ->
                        onPause()
                        // Update the animation value with touch events.
                        val changeY = (offsetY.value + change.positionChange().y).coerceAtMost(0f)
                        launch {
                            offsetY.snapTo(changeY)
                        }
                        if (changeY == 0f) {
                            velocityTracker.resetTracking()
                        } else {
                            velocityTracker.addPosition(
                                change.uptimeMillis,
                                change.position,
                            )
                        }
                    }

                    onResume()
                    // No longer receiving touch events. Prepare the animation.
                    val velocity = velocityTracker.calculateVelocity().y
                    val targetOffsetY = decay.calculateTargetValue(
                        offsetY.value,
                        velocity,
                    )
                    // The animation stops when it reaches the bounds.
                    offsetY.updateBounds(
                        lowerBound = -size.height.toFloat() * 3,
                        upperBound = size.height.toFloat(),
                    )
                    launch {
                        if (velocity >= 0 || targetOffsetY.absoluteValue <= size.height) {
                            // Not enough velocity; Slide back.
                            offsetY.animateTo(
                                targetValue = 0f,
                                initialVelocity = velocity,
                            )
                        } else {
                            // The element was swiped away.
                            launch { offsetY.animateDecay(velocity, decay) }
                            launch {
                                alpha.animateTo(targetValue = 0f, animationSpec = tween(300))
                                onDismissed()
                            }
                        }
                    }
                }
            }
        }
    }
        .offset {
            IntOffset(0, offsetY.value.roundToInt())
        }
        .alpha(alpha.value)
}


@Stable
class ToastUIState {
    private data class ToastRequest(val model: ToastModel, val onlyOne: Boolean = true, val dismissedListener: () -> Unit = {})
    private val requestFlow: MutableSharedFlow<ToastRequest> = MutableSharedFlow()
    val scope = CoroutineScope(Dispatchers.IO)
    init {
        scope.launch {
            requestFlow.collect { request ->
                launch {
                    showNow(request.model, request.onlyOne, request.dismissedListener)
                }
            }
        }
    }

    var currentDatas: MutableList<ToastData> = mutableStateListOf()

    fun applyShow(
        message: String,
        icon: ImageVector? = null,
        onlyOne: Boolean = true,
        dismissedListener: () -> Unit = {},
    ) {
        scope.launch {
            requestFlow.emit(ToastRequest(ToastModel(message, icon, type = ToastModel.Type.Normal), onlyOne, dismissedListener))
        }
    }

    fun applyShow(
        toastModel: ToastModel,
        onlyOne: Boolean = true,
        dismissedListener: () -> Unit = {},
    ) {
        scope.launch {
            requestFlow.emit(ToastRequest(toastModel, onlyOne, dismissedListener))
        }
    }

    fun applyShowForError(e: Throwable, dismissedListener: () -> Unit = {}) {
        applyShow(ToastModel(e.message ?: "Unknown error", type = ToastModel.Type.Error), dismissedListener = dismissedListener)
    }

    suspend fun showBlocking(
        message: String,
        icon: ImageVector? = null,
        onlyOne: Boolean = true,
        dismissedListener: () -> Unit = {}
    ) {
        showBlocking(ToastModel(message, icon, type = ToastModel.Type.Normal), onlyOne, dismissedListener = dismissedListener)
    }
    suspend fun showBlocking(
        toastModel: ToastModel,
        onlyOne: Boolean = true,
        dismissedListener: () -> Unit = {}
    ) = showNow(toastModel, onlyOne, dismissedListener)
    private suspend fun showNow(
        toastModel: ToastModel,
        onlyOne: Boolean = true,
        dismissedListener: () -> Unit = {},
    ) {
        var data: ToastDataImpl? = null
        try {
            if (onlyOne && currentDatas.map { Triple(it.message, it.icon, it.type) }.contains(Triple(toastModel.message, toastModel.icon, toastModel.type))) {
                currentDatas.firstOrNull()?.shake()
                return
            }
            return suspendCancellableCoroutine { cont ->
                data = ToastDataImpl(
                    toastModel.message,
                    toastModel.icon,
                    cont,
                    toastModel.type,
                    dismissedListener,
                )
                currentDatas.add(data!!)
            }
        } finally {
            if (data != null) {
                currentDatas.remove(data!!)
            }
        }
    }


    @Stable
    private data class ToastDataImpl(
        override val message: String,
        override val icon: ImageVector?,
        private val continuation: CancellableContinuation<Unit>,
        override val type: ToastModel.Type = ToastModel.Type.Normal,
        val dismissedListener: () -> Unit = {},
    ) : ToastData {
        val id = System.nanoTime() + Random.nextInt()
        private var elapsed = 0L
        private var started = 0L
        private var duration = 0L
        private val _state = MutableStateFlow<Int?>(null)
        override val animationDuration: StateFlow<Int?> = _state.asStateFlow()

        override var shaking: Boolean by mutableStateOf(false)
        override fun shake() {
            shaking = true
        }
        override suspend fun run(accessibilityManager: AccessibilityManager?) {
            duration = durationTimeout(
                hasIcon = icon != null,
                accessibilityManager = accessibilityManager,
            )

            // Accessibility decided to show forever
            // Let's await explicit dismiss, do not run animation.
            if (duration == Long.MAX_VALUE) {
                delay(duration)
                return
            }

            resume()
            supervisorScope {
                launch {
                    animationDuration.collectLatest { duration ->
                        val animationScale = coroutineContext.durationScale
                        if (duration != null) {
                            started = System.nanoTime()
                            // 关闭动画后，只需显示、等待和隐藏即可。
                            val finalDuration = when (animationScale) {
                                0f -> duration.toLong()
                                else -> (duration.toLong() * animationScale).roundToLong()
                            }
                            delay(finalDuration)
                            this@launch.cancel()
                        } else {
                            elapsed += System.nanoTime() - started
                            delay(Long.MAX_VALUE)
                        }
                    }
                }
            }
        }

        override fun pause() {
            _state.value = null
        }

        override fun resume() {
            val remains = (duration - elapsed).toInt()
            if (remains > 0) {
                _state.value = remains
            } else {
                dismiss()
            }
        }

        override fun dismiss() {
            _state.value = 0
        }

        override fun dismissed() {
            if (continuation.isActive) {
                continuation.resume(Unit)
            }
            //Toast结束!
            dismissedListener()
        }
        override fun equals(other: Any?): Boolean {
            if (other !is ToastDataImpl) return false
            return id == other.id
        }
        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ToastUI(
    hostState: ToastUIState,
    modifier: Modifier = Modifier,
    toast: @Composable (ToastData) -> Unit = { Toast(it) },
) {
    val accessibilityManager = LocalAccessibilityManager.current
//    val currentData = hostState.currentData ?: return
    val currentData = hostState.currentDatas.firstOrNull()
    if (currentData == null) {
//        logger.debug { "ToastUI currentData is null" }
        return
    }
    //震动
    val feedback = LocalHapticFeedback.current
    key(currentData) {
        var state by remember { mutableStateOf(false) }
        val transition = updateTransition(targetState = state, label = "toast")

        LaunchedEffect(Unit) {
            state = true
            currentData.run(accessibilityManager)
            state = false
//            feedback.vibration()
        }

        transition.AnimatedVisibility(
            visible = { it },
            modifier = modifier,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            toast(currentData)
        }

        // Await dismiss animation and dismiss the Toast completely.
        // This animation workaround instead of nulling the toast data is to prevent
        // relaunching another Toast when the dismiss animation has not completed yet.
        LaunchedEffect(state, transition.currentState, transition.isRunning) {
            if (!state && !transition.currentState && !transition.isRunning) {
                currentData.dismissed()
//                feedback.vibration()

            }
        }
    }
}

internal fun durationTimeout(
    hasIcon: Boolean,
    accessibilityManager: AccessibilityManager?,
): Long {
    val timeout = 3000L
    if (accessibilityManager == null) return timeout
    return accessibilityManager.calculateRecommendedTimeoutMillis(
        originalTimeoutMillis = timeout,
        containsIcons = hasIcon,
        containsText = true,
        containsControls = false,
    )
}

internal val CoroutineContext.durationScale: Float
    get() {
        val scale = this[MotionDurationScale]?.scaleFactor ?: 1f
        check(scale >= 0f)
        return scale
    }