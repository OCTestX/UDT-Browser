package udiskmanager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import browser.DBFile
import browser.models.ActionPack
import browser.models.VirUdisk
import browser.models.Work
import browser.models.WorkType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logger
import utils.linkDir
import utils.linkFile
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import javax.swing.filechooser.FileSystemView
import kotlin.concurrent.withLock

class UDiskManager(val udiskRootDir: File) {
    private val usb = UsbUtils.getUsb(udiskRootDir)
    val basicConfigDir = File(udiskRootDir, ".udtManager").apply { mkdirs() }
    val configDir = File(basicConfigDir, "config_${WorkDir.globalServiceConfig.id}").apply { mkdirs() }
    val dbFile = File(configDir, "fs.db")
    val storageDir = File(configDir, "storage").apply { mkdirs() }
    val serviceFile = File(configDir, "serviceConfig.json")
    val actionFile = File(configDir, "action.json")

    val actionPack: ActionPack = loadAction()
    init {

    }

    private val _actions: MutableMap<String, ActionPack.Companion.Action> = mutableStateMapOf<String, ActionPack.Companion.Action>().apply {
        actionPack.actions.forEach {
            this[it.id] = it
        }
    }
    val actions: Map<String, ActionPack.Companion.Action>
        get() = _actions

    /**
     * NOP means no action, remove the action from the map
     */
    fun setUDiskAction(udisk: VirUdisk, action: ActionPack.Companion.Action) {
        if (action.work == WorkType.NOP) {
            _actions.remove(udisk.udiskId)
            logger.info("Remove action for udisk ${udisk.udiskId}")
        } else {
            _actions[udisk.udiskId] = action
            logger.info("Set action ${action.id} for udisk ${udisk.udiskId}")
        }
    }

    @Composable
    fun getUDiskAction(udisk: VirUdisk) = _actions[udisk.udiskId]

    fun fsDBExists() = dbFile.exists()
    fun getDBFile() = DBFile(dbFile)

    private fun loadAction(): ActionPack {
        return ActionPack.loadAction(actionFile)
    }
    suspend fun saveActionPack() {
        withContext(Dispatchers.IO) {
            val actionPack = ActionPack(_actions.values.toList())
            actionFile.writeText(actionPack.toJson())
        }
    }
}