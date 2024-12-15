package browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import browser.models.*
import logger
import udiskmanager.UDiskManager
import ui.screens.DBBrowserScreen.DBBrowserScreenAction
import ui.screens.DBBrowserScreen.DBBrowserScreenState

object ChangeActionPack {

    /**
     * 切换参数会先remove在add
     */
    @Composable
    fun AskUDisk(udisk: VirUdisk, uDiskManager: UDiskManager, close: () -> Unit) {
        var visible by remember { mutableStateOf(true) }
        UDiskActionSettingDialogWindow(udisk, uDiskManager.getUDiskAction(udisk)?.work?: WorkType.NOP, visible) { action ->
            close()
            visible = false
            if (action != null) {
                uDiskManager.setUDiskAction(udisk, action)
                logger.debug("设置动作成功: $action")
            }
        }
    }

    /**
     * 返回null就是放弃更改
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UDiskActionSettingDialogWindow(udisk: VirUdisk, defaultWorkType: WorkType, visible: Boolean, closeVisible: (action: ActionPack.Companion.Action?) -> Unit) {
        DialogWindow(onCloseRequest = {
            closeVisible(null)
        }, visible = visible) {
            var currentWorkType: WorkType by remember(udisk) {
                mutableStateOf(defaultWorkType)
            }
            Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                Text("设置动作", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally), fontSize = MaterialTheme.typography.titleLarge.fontSize)

                Row(Modifier.padding(16.dp)) {
                    RadioButton(selected = currentWorkType == WorkType.NOP, onClick = {
                        currentWorkType = WorkType.NOP
                    })
                    Text("不设置")
                    RadioButton(selected = currentWorkType == WorkType.DeleteDBUsb, onClick = {
                        currentWorkType = WorkType.DeleteDBUsb
                    })
                    Text("删除数据库")
                }

                Row(Modifier.fillMaxWidth().padding(16.dp)) {
                    Button(onClick = {
                        if (currentWorkType != WorkType.NOP) {
                            val work = if (currentWorkType == WorkType.DeleteDBUsb) Work.DeleteDBUsb(udisk.udiskId) else throw IllegalStateException("不支持的WorkType: $currentWorkType")
                            val action = ActionPack.Companion.Action(udisk.udiskId, "DeleteDBUsb:${udisk.name}", description = "Inner: 删除数据库U盘[${udisk.udiskId}]", eventListener = EventListenerType.Load, data = work.toWorkData(), work = work.type)
                            closeVisible(action)
                        } else {
                            closeVisible(ActionPack.Companion.Action.nop)
                        }
                    }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                        Text("保存")
                    }
                    Button(onClick = {
                        closeVisible(null)
                    }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                        Text("取消")
                    }
                }
            }
        }
    }
}