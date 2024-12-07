import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import browser.Project
import browser.RemoteProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import utils.ifNotExits
import utils.mustDir
import utils.mustFile
import java.io.File
import java.nio.charset.Charset

object WorkDir {
    private val udtHome = System.getProperty("UDT_Browser_HOME")
    val globalServiceConfig = ServiceConfig.getConfig(udtHome?:"/home/octest/Myself/Project/UDTProjects/DataDir/SeewoTest")//TODO
//    val globalServiceConfig = ServiceConfig.getConfig("D:/UDTProject/UDT/UDTService")//TODO

    fun saveServiceConfig() {
        val str = Json.encodeToString(globalServiceConfig.asContent())
        globalServiceConfig.configFile.writeText(str, Charset.forName("UTF-8"))
    }

    @Serializable
    data class ConfigFileContent(
        val currentDir: String,
        val recentProjects: List<Project> = emptyList(),
        val remoteProjects: List<RemoteProject> = emptyList(),
        val currentThemeSchemeIndex: Int,
        val fileCardMinWidth: Int,
        val dirSizeEachCountAnimateDelay: Long,
        val touchOptimized: Boolean,
        val desktopDirLocation: String,
        val windowSize: Pair<Int, Int>,
        val enableThumbnail: Boolean,
    )

    data class ServiceConfig(
        val currentDir: File,
        val recentProjects: MutableList<Project>,
        val remoteProjects: MutableList<RemoteProject>,
        var currentThemeSchemeIndex: Int,
        val fileCardMinWidth: MutableState<Int>,
        val dirSizeEachCountAnimateDelay: MutableState<Long>,
        val touchOptimized: MutableState<Boolean>,
        val desktopDirLocation: MutableState<String>,
        val windowSize: MutableState<Pair<Int, Int>>,
        val enableThumbnail: MutableState<Boolean>
    ) {
        val tempDir = File(currentDir, "Temp").mustDir()
        val cacheDir = File(currentDir, "Cache").mustDir()
        val storageDir = File(currentDir, "Storage").mustDir()
        val logsDir = File(currentDir, "Logs").mustDir()
        val configFile = File(currentDir, "serviceConfig.json").ifNotExits {
            createServiceConfig(currentDir)
        }.mustFile()

        fun asContent() = ConfigFileContent(
            currentDir = currentDir.absolutePath,
            recentProjects = recentProjects,
            currentThemeSchemeIndex = currentThemeSchemeIndex,
            fileCardMinWidth = fileCardMinWidth.value,
            dirSizeEachCountAnimateDelay = dirSizeEachCountAnimateDelay.value,
            touchOptimized = touchOptimized.value,
            desktopDirLocation = desktopDirLocation.value,
            windowSize = windowSize.value,
            enableThumbnail = enableThumbnail.value
        )

        //
        companion object {
            private var restoreCount = 0
            private lateinit var serviceConfig: ServiceConfig
            fun getConfig(rootPath: String): ServiceConfig {
                if (!this::serviceConfig.isInitialized) {
                    try {
                        val configContent: ConfigFileContent = Json.decodeFromString(
                            File(rootPath, "serviceConfig.json")
                                .ifNotExits {
                                    createServiceConfig(File(rootPath))
                                }.mustFile()
                                .readText()
                        )
                        serviceConfig = transformConfig(configContent)
                    } catch (e: Throwable) {
                        System.err.println("Failed to load service config: $e")
                        System.err.println("Restore default service config")
                        createServiceConfig(File(rootPath))
                        restoreCount ++
                        if (restoreCount > 3) {
                            throw e
                        }
                        return getConfig(rootPath)
                    }
                }
                return serviceConfig
            }
            private fun transformConfig(content: ConfigFileContent): ServiceConfig {
                return ServiceConfig(
                    File(content.currentDir),
                    content.recentProjects.toMutableList(),
                    content.remoteProjects.toMutableList(),
                    content.currentThemeSchemeIndex,
                    mutableStateOf(content.fileCardMinWidth),
                    mutableStateOf(content.dirSizeEachCountAnimateDelay),
                    mutableStateOf(content.touchOptimized),
                    mutableStateOf(content.desktopDirLocation),
                    mutableStateOf(content.windowSize),
                    mutableStateOf(content.enableThumbnail)
                )
            }
        }
    }

    fun createServiceConfig(rootDir: File): File {
        val configContent = ConfigFileContent(
            currentDir = rootDir.absolutePath,
            listOf(),
            listOf(),
            0,
            200,
            9,
            true,
            "",
            Pair(1200, 800),
            false
        )
        val configFile = File(rootDir, "serviceConfig.json")
        configFile.writeText(Json.encodeToString(configContent), Charset.forName("UTF-8"))
        return configFile
    }
}