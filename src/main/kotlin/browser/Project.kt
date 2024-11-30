package browser

import WorkDir
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import utils.linkDir
import utils.linkFile
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

@Serializable
class Project(
    private val dbFileSource: String,
    var storageDir: String?,
    override val name: String = File(dbFileSource).name,
    override val customUDiskNames: Map<String, String> = mutableMapOf()
): AbsProject() {
    @Transient
    private val editingCustomUDiskNames = mutableStateMapOf<String, String>().apply {
        putAll(customUDiskNames)
    }
    fun setCustomUDiskName(udiskId: String, name: String?) {
        if (name == null) {
            editingCustomUDiskNames.remove(udiskId)
            (customUDiskNames as MutableMap) .remove(udiskId)
        } else {
            editingCustomUDiskNames[udiskId] = name
            (customUDiskNames as MutableMap) [udiskId] = name
        }
    }
    fun getCustomUDiskName(udiskId: String): String? {
        return customUDiskNames[udiskId]
    }
    @Composable
    fun getCustomUDiskNameState(udiskId: String): String? {
        return editingCustomUDiskNames[udiskId]
    }


    override fun load() {}

    override val dbFile: DBFile by lazy {
        DBFile(File(dbFileSource))
    }
    override fun exists(): Boolean {
        val dbFileExits = dbFile.file.exists()
        val tmpStorageDir = storageDir
        if (tmpStorageDir == null) return dbFileExits
        else {
            val storageDirExits = File(tmpStorageDir).exists()
            val storageDirIsDirectory = File(tmpStorageDir).isDirectory
            return dbFileExits && storageDirExits && storageDirIsDirectory
        }
    }
    override fun save() {
        WorkDir.saveServiceConfig()
    }
    override fun delete() {
        WorkDir.globalServiceConfig.recentProjects.remove(this)
        save()
    }

    private fun getSourceFile(udiskId: String, fileId: String): File {
        val storageDir = storageDir
        if (storageDir != null) {
            val sourceFile = File(storageDir).linkDir(udiskId).linkFile(fileId)
            if (sourceFile.exists()) {
                return sourceFile
            } else {
                throw FileNotFoundException("Storage dir not exists, please select again.")
            }
        } else {
            throw FileNotFoundException("Please select storage dir, first.")
        }
    }
    override suspend fun getFileInputStream(udiskId: String, fileId: String): InputStream {
        return getSourceFile(udiskId, fileId).inputStream()
    }

    override suspend fun getFileSize(udiskId: String, fileId: String): Long {
        return getSourceFile(udiskId, fileId).length()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Project
        if (dbFileSource != other.dbFileSource) return false
        if (storageDir != other.storageDir) return false
        return true
    }

    override fun hashCode(): Int {
        var result = dbFileSource.hashCode()
        result = 31 * result + (storageDir?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Project(dbFileSource='$dbFileSource', storageDir=$storageDir, name='$name')"
    }

    companion object {
        fun create(dbFile: DBFile, storageDir: String? = null): Project = Project(dbFile.file.absolutePath, storageDir)
    }
}

@Serializable
class RemoteProject(
    val url: String,
    override var name: String,
    override val customUDiskNames: Map<String, String> = emptyMap()
): AbsProject() {
    @Transient
    private var dbFileSource: String? = null
    override fun load() {
        TODO("Not yet implemented[dbFileSource = ;;;;]")
    }

    override val dbFile: DBFile by lazy {
        DBFile(File(dbFileSource!!))
    }

    override suspend fun getFileInputStream(udiskId: String, fileId: String): InputStream {
        TODO("Not yet implemented")
    }

    override suspend fun getFileSize(udiskId: String, fileId: String): Long {
        TODO("Not yet implemented")
    }

    override fun exists(): Boolean {
        //ping url
        TODO("Not yet implemented")
    }

    override fun save() {
        WorkDir.saveServiceConfig()
    }

    override fun delete() {
        WorkDir.globalServiceConfig.remoteProjects.remove(this)
        WorkDir.saveServiceConfig()
    }

}

abstract class AbsProject {
    abstract fun load()
    abstract val name: String
    abstract val dbFile: DBFile
    abstract suspend fun getFileInputStream(udiskId: String, fileId: String): InputStream
    abstract suspend fun getFileSize(udiskId: String, fileId: String): Long
    abstract fun exists(): Boolean
    abstract fun save()
    abstract fun delete()
    abstract val customUDiskNames: Map<String, String>
}