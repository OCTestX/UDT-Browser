package browser.models

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class VirUdisk(
    val name: String,
    val totalSize: Long,
    val freeSize: Long,
    val udiskId: String,
)
