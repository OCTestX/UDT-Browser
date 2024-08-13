package ui.screens

import LocalTopTitleBarState
import Main
import WorkDir
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import browser.DBDataProvider
import browser.Project
import browser.models.VirDir
import browser.models.VirFile
import browser.models.VirUdisk
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logger
import toast
import ui.component.ToastModel
import ui.core.UIComponent
import ui.utils.Animates
import utils.*
import java.io.File
import java.util.concurrent.locks.ReentrantLock

class DBBrowserScreen(private val dbProject: Project): UIComponent<DBBrowserScreen.DBBrowserScreenAction, DBBrowserScreen.DBBrowserScreenState>() {
    private val dbFile = dbProject.dbFile
    private val dbDataProvider = DBDataProvider(dbFile)
    class DBBrowserScreenState(
        val allUdisks: List<VirUdisk>,
        val currentUDisk: VirUdisk?,
        val currentFiles: List<VirFile>,
        val currentDirs: List<VirDir>,
        val currentParentDir: VirDir?,
        action: (DBBrowserScreenAction) -> Unit
    ) : UIState<DBBrowserScreenAction>(action)
    sealed class DBBrowserScreenAction: UIAction() {
        data object Back: DBBrowserScreenAction()
        data class SelectUdisk(val udisk: VirUdisk): DBBrowserScreenAction()
        data class SwitchDir(val dir: VirDir): DBBrowserScreenAction()
        data class OpenFile(val file: VirFile): DBBrowserScreenAction()
        data object SwitchDirToHome: DBBrowserScreenAction()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun UI(state: DBBrowserScreenState) {
        Column {
            var selectStorageDirVisible by remember { mutableStateOf(false) }
            DialogWindow(onCloseRequest = {
                selectStorageDirVisible = false
            }, visible = selectStorageDirVisible) {
                var pathStr by remember { mutableStateOf(dbProject.storageDir?: "") }
                Column {
                    TextField(pathStr, { pathStr = it })
                    Button(onClick = {
                        val file = File(pathStr)
                        if (file.exists()) {
                            dbProject.storageDir = file.absolutePath
                            dbProject.save()
                        }
                        selectStorageDirVisible = false
                    }) {
                        Text("Confirm")
                    }
                    Button(onClick = {
                        selectStorageDirVisible = false
                    }) {
                        Text("Cancel")
                    }
                }
            }
            Main.GlobalTopAppBar(LocalTopTitleBarState.current!!) {
                val storageDir= dbProject.storageDir
                if (storageDir == null || File(storageDir).exists().not()) {
                    IconButton(onClick = {
                        selectStorageDirVisible = true
                    }) {
                        Icon(TablerIcons.FolderPlus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Animates.VisibilityAnimates {
                TitleBar(state)
            }
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(state.currentParentDir?.isRoot == true || state.currentParentDir == null) {
                    Animates.VisibilityAnimates {
                        LazyColumn(modifier = Modifier.width(200.dp).animateContentSize()) {
                            items(state.allUdisks, key = { it.udiskId }) { udisk ->
                                UDiskCard(udisk, modifier = Modifier.animateItemPlacement().padding(6.dp).clickable {
                                    state.action(DBBrowserScreenAction.SelectUdisk(udisk))
                                })
                            }
                        }
                    }
                }
                Animates.VisibilityAnimates {
                    LazyVerticalGrid(columns = GridCells.Adaptive(180.dp), modifier = Modifier.weight(1f).animateContentSize()) {
                        items(state.currentDirs, key = { it.dirId }) { dir ->
                            UDirCard(dir, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                state.action(DBBrowserScreenAction.SwitchDir(dir))
                            })
                        }
                        items(state.currentFiles, key = { it.fileId }) { file ->
                            UFileCard(file, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                state.action(DBBrowserScreenAction.OpenFile(file))
                            })
                        }
                    }
//                    LazyColumn(modifier = Modifier.weight(1f).animateContentSize()) {
//
//                    }
                }
            }
        }
    }

    @Composable
    private fun TitleBar(state: DBBrowserScreenState) {
        Row {
            val enable = state.currentParentDir?.isRoot != true
            IconButton(onClick = {
                state.action(DBBrowserScreenAction.Back)
            }, enabled = enable) {
                Icon(TablerIcons.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enable) 1f else 0.45f))
            }
            IconButton(onClick = {
                state.action(DBBrowserScreenAction.SwitchDirToHome)
            }, enabled = enable) {
                Icon(TablerIcons.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (enable) 1f else 0.45f))
            }
            val path = remember(state.currentParentDir) { state.currentParentDir?.path?:"/" }
            AnimatedContent(path, modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) { currentPath ->
                Text(currentPath, fontSize = MaterialTheme.typography.titleSmall.fontSize, color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UDiskCard(disk: VirUdisk, modifier: Modifier = Modifier) {
        Card(
            modifier = Modifier.then(modifier)
        ) {
            Column(Modifier.padding(6.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Icon(TablerIcons.BoxModel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(disk.name, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        Text("${fileSize(disk.totalSize - disk.freeSize)} / ${fileSize(disk.totalSize)}", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UDirCard(dir: VirDir, modifier: Modifier = Modifier) {
        Card(
            modifier = Modifier.then(modifier)
        ) {
            Column(Modifier.padding(6.dp)) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Icon(TablerIcons.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(dir.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                }
                val size by produceState(0L) {
                    withContext(Dispatchers.IO) {
                        dbDataProvider.deepCountDirSize(dir.udiskId, dir.dirId) {
                            value = it
                        }
                    }
                }
                Text(fileSize(size))
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun UFileCard(file: VirFile, modifier: Modifier = Modifier) {
        Card(
            modifier = Modifier.then(modifier)
        ) {
            Column(Modifier.padding(6.dp)) {
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Icon(TablerIcons.File, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(file.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    }
                }
                Text(fileSize(file.size))
            }
        }
    }

    inner class InnerPresenter {
        private val allUdisks = mutableStateListOf<VirUdisk>()
        private var currentUDisk by mutableStateOf<VirUdisk?>(null)
        private val currentFiles = mutableStateListOf<VirFile>()
        private val currentDirs = mutableStateListOf<VirDir>()
        private var currentParentDir by mutableStateOf<VirDir?>(null)

        @Composable
        fun InnerPresenter(): DBBrowserScreenState {
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val disks =dbDataProvider.getAllUDisk()
                    scope.launch {
                        disks.apply {
                            allUdisks.clear()
                            allUdisks.addAll(this)
                        }.apply {
                            currentUDisk = firstOrNull()
                        }
                    }
                }
            }
            LaunchedEffect(currentUDisk) {
                currentUDisk?.apply {
                    currentParentDir = null
                    val rootDir = loadRootDir(this)
                    loadDirData(this, rootDir)
                }
            }
            return DBBrowserScreenState(
                allUdisks,
                currentUDisk,
                currentFiles,
                currentDirs,
                currentParentDir,
                action = { action ->
                    when (action) {
                        DBBrowserScreenAction.Back -> {
                            scope.launch {
                                val udisk = currentUDisk!!
                                val parentDirId = currentParentDir?.parentDirId
                                parentDirId?.also { dirId ->
                                    logger.debug { "new parent dir id: $dirId" }
                                    val newDir = dbDataProvider.getDir(udisk.udiskId, dirId)
                                    loadDirData(udisk, newDir)
                                }
                            }
                        }
                        is DBBrowserScreenAction.SelectUdisk -> {
                            currentUDisk = action.udisk
                        }
                        is DBBrowserScreenAction.SwitchDir -> {
                            scope.launch {
                                loadDirData(currentUDisk!!, action.dir)
                            }
                        }

                        DBBrowserScreenAction.SwitchDirToHome -> {
                            scope.launch {
                                currentUDisk?.also { udisk ->
                                    val rootDir = dbDataProvider.getRootDir(udisk.udiskId)
                                    loadDirData(udisk, rootDir)
                                }
                            }
                        }

                        is DBBrowserScreenAction.OpenFile -> {
                            ioScope.launch {
                                val virFile = action.file
                                val name = virFile.name
                                val udiskId = virFile.udiskId
                                val fileId = virFile.fileId
                                val storageDir = dbProject.storageDir
                                if (storageDir != null) {
                                    val sourceFile = File(storageDir).linkDir(udiskId).linkFile(fileId)
                                    if (sourceFile.exists()) {
                                        //saveDir
                                        val saveDir = WorkDir.globalServiceConfig.tempDir
                                        val targetFile = saveDir.linkFile(name)
                                        sourceFile.autoTransferTo(targetFile)
                                        toast.applyShow(ToastModel("文件导出完成(${targetFile.absolutePath})", type = ToastModel.Type.Success))
                                    } else {
                                        toast.applyShow("Storage dir not exists, please select again.")
                                    }
                                } else {
                                    toast.applyShow("Please select storage dir, first.")
                                }
                            }
                        }
                    }
                }
            )
        }

        private suspend fun loadRootDir(udisk: VirUdisk) = dbDataProvider.getRootDir(udisk.udiskId)

        private val lock = ReentrantLock()
        private suspend fun loadDirData(udisk: VirUdisk, dir: VirDir) {
            lock.lock()
            logger.info("Loading data for udisk ${udisk.name} and dir ${dir.path}[${dir.dirId}]")
            withContext(Dispatchers.IO) {
                val files = dbDataProvider.getDirFiles(udisk.udiskId, dir.dirId)
                val dirs = dbDataProvider.getDirDirs(udisk.udiskId, dir.dirId)
                scope.launch {
                    currentFiles.clear()
                    currentFiles.addAll(files)

                    currentDirs.clear()
                    currentDirs.addAll(dirs)

                    currentParentDir = dir
                }
            }
            logger.info("Data loaded for udisk ${udisk.name}[currentFiles=${currentFiles.size}, currentDirs=${currentDirs.size}]")
            lock.unlock()
        }
    }


    @Composable
    override fun Presenter(): DBBrowserScreenState {
        val presenter = remember { InnerPresenter() }
        return presenter.InnerPresenter()
//        return DBBrowserScreenState(
//            listOf(),
//            null,
//            listOf(),
//            listOf(),
//            null
//        ) {}
    }
}