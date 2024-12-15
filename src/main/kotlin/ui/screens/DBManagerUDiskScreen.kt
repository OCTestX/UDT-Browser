package ui.screens

import LocalMainTopTitleBarState
import Main
import WorkDir
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import browser.AbsProject
import browser.DBDataProvider
import browser.Project
import browser.models.VirDir
import browser.models.VirFile
import browser.models.VirUdisk
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logger
import toast
import udiskmanager.UDiskManager
import ui.component.ToastModel
import ui.component.UDirCard
import ui.component.UDiskCard
import ui.component.UFileCard
import ui.core.UIComponent
import ui.utils.Animates
import utils.*
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

class DBManagerUDiskScreen(private val managerUdiskRootDir: File, private val navigateToBrowseFiles: (Project) -> Unit): UIComponent<DBManagerUDiskScreen.DBBrowserScreenAction, DBManagerUDiskScreen.DBBrowserScreenState>() {
    private val manager = UDiskManager(managerUdiskRootDir)
    class DBBrowserScreenState(
        val enableThumbnail: Boolean,
        action: (DBBrowserScreenAction) -> Unit
    ) : UIState<DBBrowserScreenAction>(action)
    sealed class DBBrowserScreenAction: UIAction() {
        data object Back: DBBrowserScreenAction()
        data object NavigateToBrowseFiles: DBBrowserScreenAction()
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun UI(state: DBBrowserScreenState) {
        Column {
            GlobalTopAppBar(state)
            Card(modifier = Modifier.padding(16.dp)) {
                Text("UDT文件统计", modifier = Modifier.padding(16.dp))
                if (manager.fsDBExists().not()) {
                    Text("数据库索引不存在", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(32.dp))
                } else {
                    Button(onClick = {
                        state.action(DBBrowserScreenAction.NavigateToBrowseFiles)
                    }) {
                        Text("进入文件浏览")
                    }
                }
            }
            Box(modifier = Modifier.weight(1.0f)) {
//                Row(modifier = Modifier.fillMaxSize()) {
//                    AllUdisks(state)
//                    CurrentFilesAndDir(state, setSelectedVirFile = {
//                        selectedVirFile = it
//                    })
//                }
//                if (state.loading) {
//                    Animates.VisibilityAnimates {
//                        LinearProgressIndicator(Modifier.fillMaxWidth())
//                    }
//                }
            }
            BottomInfo(state)
        }
    }

    @Composable
    fun BottomInfo(state: DBBrowserScreenState) {
//        Row(modifier = Modifier.padding(6.dp)) {
//            val coping by remember(state.needCopyFileTotalSize) {
//                derivedStateOf {
//                    state.needCopyFileTotalSize > 0
//                }
//            }
//            AnimatedContent(coping, modifier = Modifier.align(Alignment.CenterVertically)) {
//                if (it) {
//                    Row {
//                        LinearProgressIndicator(progress = state.allCopingProgress, modifier = Modifier.weight(1.0f).align(Alignment.CenterVertically))
//                        Text(fileSize(state.needCopyFileTotalSize - state.copiedFileTotalSize), modifier = Modifier.align(Alignment.CenterVertically))
//                    }
//                } else {
//                    Text("无复制任务", modifier = Modifier.align(Alignment.CenterVertically))
//                }
//            }
//        }
    }

    @Composable
    fun GlobalTopAppBar(state: DBBrowserScreenState, ) {
        Main.GlobalTopAppBar("配置管理U盘", LocalMainTopTitleBarState.current!!) {
        }
    }

    inner class InnerPresenter {
        private var enableThumbnail by WorkDir.globalServiceConfig.enableThumbnail

        fun actionLink(action: DBBrowserScreenAction) {
            when (action) {
                DBBrowserScreenAction.Back -> {
//                    scope.launch {
//                        val udisk = currentUDisk!!
//                        val parentDirId = currentParentDir?.parentDirId
//                        parentDirId?.also { dirId ->
//                            logger.debug { "new parent dir id: $dirId" }
//                            val newDir = dbDataProvider.getDir(udisk.udiskId, dirId)
//                            loadDirData(udisk, newDir)
//                        }
//                    }
                }

                DBBrowserScreenAction.NavigateToBrowseFiles -> {
                    val dbFile = manager.dbFile
                    if (dbFile.exists().not()) {
                        toast.applyShow(ToastModel(message = "数据库文件不存在", type = ToastModel.Type.Error))
                    } else {
                        val project = Project.create(manager.getDBFile(), manager.storageDir.absolutePath, manager)
                        navigateToBrowseFiles(project)
                    }
                }
            }
        }

        @Composable
        fun InnerPresenter(): DBBrowserScreenState {
            return DBBrowserScreenState(
                enableThumbnail,
                action = { action ->
                    actionLink(action)
                }
            )
        }
    }


    @Composable
    override fun Presenter(): DBBrowserScreenState {
        val presenter = remember { InnerPresenter() }
        return presenter.InnerPresenter()
    }
}