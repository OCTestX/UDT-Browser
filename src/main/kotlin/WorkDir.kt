import browser.Project
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ui.component.ToastModel
import utils.ifNotExits
import utils.mustDir
import utils.mustFile
import java.io.File
import java.nio.charset.Charset
import kotlin.jvm.Throws

object WorkDir {
    val globalServiceConfig = ServiceConfig.getConfig("/home/octest/Myself/Project/UDTProjects/DataDir/SeewoTest")//TODO
//    val globalServiceConfig = ServiceConfig.getConfig("D:/UDTProject/UDT/UDTService")//TODO

    fun saveServiceConfig() {
        val str = Json.encodeToString(globalServiceConfig.asContent())
        globalServiceConfig.configFile.writeText(str, Charset.forName("UTF-8"))
    }

    @Serializable
    data class ConfigFileContent(
        val currentDir: String,
        val recentProjects: List<Project> = emptyList(),
        val currentThemeSchemeIndex: Int
    )

    data class ServiceConfig(
        val currentDir: File,
        val recentProjects: MutableList<Project>,
        var currentThemeSchemeIndex: Int,
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
            currentThemeSchemeIndex = currentThemeSchemeIndex
        )

        //
        companion object {
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
                        return getConfig(rootPath)
//                        throw e
                    }
                }
                return serviceConfig
            }
            private fun transformConfig(content: ConfigFileContent): ServiceConfig {
                return ServiceConfig(
                    File(content.currentDir),
                    content.recentProjects.toMutableList(),
                    content.currentThemeSchemeIndex,
                )
            }
        }
    }

    fun createServiceConfig(rootDir: File): File {
        val configContent = ConfigFileContent(
            currentDir = rootDir.absolutePath,
            listOf(),
            0
        )
        val configFile = File(rootDir, "serviceConfig.json")
        configFile.writeText(Json.encodeToString(configContent), Charset.forName("UTF-8"))
        return configFile
    }

}