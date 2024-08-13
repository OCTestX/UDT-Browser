package browser

import WorkDir
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Project(
    val dbFileSource: String,
    var storageDir: String?,
    var name: String = File(dbFileSource).name,
) {
    val dbFile: DBFile by lazy {
        DBFile(File(dbFileSource))
    }
    fun exists(): Boolean {
        val dbFileExits = dbFile.file.exists()
        val tmpStorageDir = storageDir
        if (tmpStorageDir == null) return dbFileExits
        else {
            val storageDirExits = File(tmpStorageDir).exists()
            val storageDirIsDirectory = File(tmpStorageDir).isDirectory
            return dbFileExits && storageDirExits && storageDirIsDirectory
        }
    }

    companion object {
        fun create(dbFile: DBFile, storageDir: String? = null): Project = Project(dbFile.file.absolutePath, storageDir)
    }
    fun save() {
        WorkDir.saveServiceConfig()
    }
    fun delete() {
        WorkDir.globalServiceConfig.recentProjects.remove(this)
        save()
    }
}
