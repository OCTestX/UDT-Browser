package ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import browser.models.ActionPack
import browser.models.WorkType
import androidx.compose.material3.Icon
import compose.icons.TablerIcons
import compose.icons.tablericons.Download

@Composable
fun ActionLED(action: ActionPack.Companion.Action) {
    when (action.work) {
        WorkType.NOP -> TODO("不可能会出现")
        WorkType.ProvideFileToManagerUDisk -> Icon(TablerIcons.Download, "提供文件到U盘管理器")
        WorkType.DeleteDBUsb -> Icon(Icons.Default.Delete, "删除数据库U盘")
        WorkType.DeleteDBUsbDir -> Icon(Icons.Default.Delete, "删除数据库文件夹")
        WorkType.DeleteDBUsbFile -> Icon(Icons.Default.Delete, "删除数据库文件")
    }
}