package browser.models

data class VirDir(
    val name: String,
    val parentDirId: String?,
    val udiskId: String,
    val path: String,
    val dirId: String
) {
    val isRoot: Boolean = path == "/"
}
