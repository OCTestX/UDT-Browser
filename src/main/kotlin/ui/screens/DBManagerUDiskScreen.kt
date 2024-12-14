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

class DBManagerUDiskScreen(private val managerUdiskRootDir: File): UIComponent<DBManagerUDiskScreen.DBBrowserScreenAction, DBManagerUDiskScreen.DBBrowserScreenState>() {
    class DBBrowserScreenState(
        val enableThumbnail: Boolean,
        action: (DBBrowserScreenAction) -> Unit
    ) : UIState<DBBrowserScreenAction>(action)
    sealed class DBBrowserScreenAction: UIAction() {
        data object Back: DBBrowserScreenAction()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun UI(state: DBBrowserScreenState) {
        Column {
            GlobalTopAppBar(state)
            Animates.VisibilityAnimates {
                TitleBar(state)
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
//        if (dbProject is Project) {
//            Main.GlobalTopAppBar(getTitle(state.currentUDisk), LocalMainTopTitleBarState.current!!) {
//                IconButton(onClick = {
//                    setCustomNameVisible(true)
//                }) {
//                    Icon(TablerIcons.EditCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
//                }
//                IconButton(onClick = {
//                    setSelectStorageDirVisible(true)
//                }) {
//                    Icon(TablerIcons.FolderPlus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
//                }
//            }
//        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TitleBar(state: DBBrowserScreenState) {
        Row {
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

    @Composable
    fun getTitle(currentUDisk: VirUdisk?): String {
        return "管理U盘部署"
    }
}