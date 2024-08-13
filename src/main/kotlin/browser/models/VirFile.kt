package browser.models

data class VirFile(
    val name: String,
    val parentDirId: String,
    val udiskId: String,
    val size: Long,
    val fileId: String,
)