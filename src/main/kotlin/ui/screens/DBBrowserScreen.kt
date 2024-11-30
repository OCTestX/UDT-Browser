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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logger
import toast
import ui.component.ToastModel
import ui.core.UIComponent
import ui.utils.Animates
import utils.*
import java.io.File
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock

class DBBrowserScreen(private val dbProject: AbsProject): UIComponent<DBBrowserScreen.DBBrowserScreenAction, DBBrowserScreen.DBBrowserScreenState>() {
    private val dbFile = dbProject.dbFile
    private val dbDataProvider = DBDataProvider(dbFile)
    class DBBrowserScreenState(
        val allUdisks: List<VirUdisk>,
        val currentUDisk: VirUdisk?,
        val currentFiles: List<VirFile>,
        val currentDirs: List<VirDir>,
        val currentParentDir: VirDir?,
        val fileCardMinWidth: Int,
        val scrollOffset: Int,
        val touchOptimized: Boolean,
        val searchingKeyword: String,
        val searchingMode: Boolean,
        val loading: Boolean,
        action: (DBBrowserScreenAction) -> Unit
    ) : UIState<DBBrowserScreenAction>(action)
    sealed class DBBrowserScreenAction: UIAction() {
        data object Back: DBBrowserScreenAction()
        data class SelectUdisk(val udisk: VirUdisk): DBBrowserScreenAction()
        data class SwitchDir(val dir: VirDir): DBBrowserScreenAction()
        data class OpenFile(val file: VirFile): DBBrowserScreenAction()
        data object SwitchDirToHome: DBBrowserScreenAction()
        data class ChangeCustomName(val customName: String): DBBrowserScreenAction()
        data class ChangeSearchingKeyword(val keyword: String): DBBrowserScreenAction()
        data class SwitchSearchingMode(val searchingMode: Boolean): DBBrowserScreenAction()
        data class Search(val keyword: String): DBBrowserScreenAction()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun UI(state: DBBrowserScreenState) {
        var selectStorageDirVisible by remember { mutableStateOf(false) }
        val selectedStorageDirLauncher = rememberDirectoryPickerLauncher(
            title = "选择文件存储库",
        ) { selectedFile ->
            // Handle the picked files
            if (selectedFile != null) {
                val file = selectedFile.file
                if (file.exists()) {
                    (dbProject as Project).storageDir = file.absolutePath
                    dbProject.save()
                }
                selectStorageDirVisible = false
            }
        }
        Column {
            var setCustomNameVisible by remember { mutableStateOf(false) }
            if (dbProject is Project) {
                DialogWindow(onCloseRequest = {
                    setCustomNameVisible = false
                }, visible = setCustomNameVisible) {
                    var customName by remember(state.currentUDisk) {
                        val udiskId = state.currentUDisk?.udiskId
                        val rawName = state.currentUDisk?.name?:"未知u盘"
                        mutableStateOf(dbProject.getCustomUDiskName(udiskId?:"未知u盘")?: rawName)
                    }
                    Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                        Text("设置自定义名称", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally), fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        TextField(customName, { customName = it }, modifier = Modifier.fillMaxWidth().padding(16.dp))
                        Row(Modifier.fillMaxWidth().padding(16.dp)) {
                            Button(onClick = {
                                state.action(DBBrowserScreenAction.ChangeCustomName(customName))
                                setCustomNameVisible = false
                            }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                                Text("保存")
                            }
                            Button(onClick = {
                                setCustomNameVisible = false
                            }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                                Text("取消")
                            }
                        }
                    }
                }
                DialogWindow(onCloseRequest = {
                    selectStorageDirVisible = false
                }, visible = selectStorageDirVisible) {
                    var pathStr by remember(dbProject.storageDir) { mutableStateOf(dbProject.storageDir?: "") }
                    Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                        Text("选择文件存储库位置", modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally), fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        TextField(pathStr, { pathStr = it }, modifier = Modifier.fillMaxWidth().padding(16.dp))
                        Row(Modifier.fillMaxWidth().padding(16.dp)) {
                            Button(onClick = {
                                selectedStorageDirLauncher.launch()
                            }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                                Text("选择")
                            }
                            Button(onClick = {
                                val file = File(pathStr)
                                if (file.exists()) {
                                    dbProject.storageDir = file.absolutePath
                                    dbProject.save()
                                }
                                selectStorageDirVisible = false
                            }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                                Text("保存")
                            }
                            Button(onClick = {
                                selectStorageDirVisible = false
                            }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                                Text("取消")
                            }
                        }
                    }
                }
                Main.GlobalTopAppBar(getTitle(state.currentUDisk), LocalMainTopTitleBarState.current!!) {
                    IconButton(onClick = {
                        setCustomNameVisible = true
                    }) {
                        Icon(TablerIcons.EditCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
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
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    val listDirsAndFilesScrollState = rememberLazyGridState()
                    AnimatedVisibility(state.currentParentDir?.isRoot == true || state.currentParentDir == null) {
                        Animates.VisibilityAnimates {
                            val disksScrollState = rememberLazyListState()
                            Column {
                                Row(modifier = Modifier.width(200.dp).animateContentSize().weight(1f)) {
                                    LazyColumn(Modifier.weight(1f), state = disksScrollState) {
                                        items(state.allUdisks, key = { it.udiskId }) { udisk ->
                                            UDiskCard(udisk, modifier = Modifier.animateItemPlacement().padding(6.dp).clickable {
                                                state.action(DBBrowserScreenAction.SelectUdisk(udisk))
                                            })
                                        }
                                    }
//                            // 滚动条
//                            VerticalScrollbar(
//                                modifier = Modifier.fillMaxHeight(),
//                                adapter = rememberScrollbarAdapter(disksScrollState),
//                                style = LocalScrollbarStyle.current.copy(unhoverColor = MaterialTheme.colorScheme.primary, hoverColor = MaterialTheme.colorScheme.inversePrimary)
//                            )
                                }
                                AnimatedVisibility(state.touchOptimized) {
                                    Row(modifier = Modifier.padding(6.dp)) {
                                        IconButton(onClick = {
                                            scope.launch {
                                                disksScrollState.animateScrollToItem(disksScrollState.firstVisibleItemIndex, disksScrollState.firstVisibleItemScrollOffset - state.scrollOffset)
                                            }
                                        }) {
                                            Icon(TablerIcons.ArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                disksScrollState.animateScrollToItem(disksScrollState.firstVisibleItemIndex, disksScrollState.firstVisibleItemScrollOffset + state.scrollOffset)
                                            }
                                        }) {
                                            Icon(TablerIcons.ArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                listDirsAndFilesScrollState.animateScrollToItem(listDirsAndFilesScrollState.firstVisibleItemIndex, listDirsAndFilesScrollState.firstVisibleItemScrollOffset - state.scrollOffset)
                                            }
                                        }) {
                                            Icon(TablerIcons.ArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                listDirsAndFilesScrollState.animateScrollToItem(listDirsAndFilesScrollState.firstVisibleItemIndex, listDirsAndFilesScrollState.firstVisibleItemScrollOffset + state.scrollOffset)
                                            }
                                        }) {
                                            Icon(TablerIcons.ArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Animates.VisibilityAnimates {
                        Row (modifier = Modifier.weight(1f).animateContentSize()) {
                            LazyVerticalGrid(columns = GridCells.Adaptive(state.fileCardMinWidth.dp), modifier = Modifier.weight(1f), state = listDirsAndFilesScrollState) {
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
                        }
                    }
                }
                if (state.loading) {
                    Animates.VisibilityAnimates {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TitleBar(state: DBBrowserScreenState) {
        Row {
            Checkbox(state.searchingMode, onCheckedChange = { state.action(DBBrowserScreenAction.SwitchSearchingMode(it)) })
            Text("搜索模式", modifier = Modifier.align(Alignment.CenterVertically), fontSize = MaterialTheme.typography.titleSmall.fontSize)

            AnimatedContent(state.searchingMode) {
                Row(Modifier.weight(1f).align(Alignment.CenterVertically)) {
                    if (it) {
                        TextField(state.searchingKeyword, {
                            state.action(DBBrowserScreenAction.ChangeSearchingKeyword(it))
                            state.action(DBBrowserScreenAction.Search(it))
                        }, modifier = Modifier.weight(1f).padding(horizontal = 6.dp).align(Alignment.CenterVertically))
                    } else {
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
                        if (dbProject is Project) {
                            AnimatedContent(dbProject.getCustomUDiskNameState(disk.udiskId)?: disk.name) {
                                Text(it, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                            }
                        } else {
                            Text(disk.name, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                        }
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

                var countingSize by rememberSaveable { mutableStateOf(true) }
                val size by produceState(0L) {
                    withContext(Dispatchers.IO) {
                        dbDataProvider.deepCountDirSize(dir.udiskId, dir.dirId) {
                            value = it
                            delay(WorkDir.globalServiceConfig.dirSizeEachCountAnimateDelay.value)
                        }
                        countingSize = false
                    }
                }
                Animates.VisibilityAnimates {
                    Column {
                        Text(fileSize(size))
                        AnimatedVisibility(countingSize) {
                            LinearProgressIndicator(Modifier.fillMaxWidth())
                        }
                    }
                }
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
                Animates.VisibilityAnimates {
                    Text(fileSize(file.size))
                }
            }
        }
    }

    inner class InnerPresenter {
        private val allUdisks = mutableStateListOf<VirUdisk>()
        private var currentUDisk by mutableStateOf<VirUdisk?>(null)
        private val currentFiles = mutableStateListOf<VirFile>()
        private val currentDirs = mutableStateListOf<VirDir>()
        private var currentParentDir by mutableStateOf<VirDir?>(null)
        private var fileCardMinWidth by WorkDir.globalServiceConfig.fileCardMinWidth
        private var scrollOffset by WorkDir.globalServiceConfig.scrollOffset
        private var touchOptimized by WorkDir.globalServiceConfig.touchOptimized
        private var currentSearchingMode by mutableStateOf(false)
        private var currentSearchingKeyword by mutableStateOf("")
        private var loading by mutableStateOf(false)

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
                    val disk = this
                    launch(Dispatchers.IO) {
                        val rootDir = loadRootDir(disk)
                        loadDirData(disk, rootDir)
                    }
                }
            }
            return DBBrowserScreenState(
                allUdisks,
                currentUDisk,
                currentFiles,
                currentDirs,
                currentParentDir,
                fileCardMinWidth,
                scrollOffset,
                touchOptimized,
                currentSearchingKeyword,
                currentSearchingMode,
                loading,
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

                                try {
                                    val sourceInputStream = dbProject.getFileInputStream(udiskId, fileId)
                                    val sourceSize = dbProject.getFileSize(udiskId, fileId)
                                    //saveDir
                                    val saveDir = WorkDir.globalServiceConfig.tempDir
                                    val targetFile = saveDir.linkFile(name)
                                    val speedCalculator = SpeedCalculator()
                                    sourceInputStream.autoTransferTo(targetFile.outputStream()) { copied ->
                                        val progress = (copied.toDouble() * 100 / sourceSize).toFloat()
                                        val currentSpeed = speedCalculator.calculate(copied)
                                        logger.debug { "progress[${fileSize(currentSpeed)}/s]: $progress" }
                                    }
                                    logger.debug { "file export complete: ${targetFile.absolutePath}[${speedCalculator.getUsedTime()} ms]" }
                                    toast.applyShow(ToastModel("文件导出完成(${targetFile.absolutePath})", type = ToastModel.Type.Success))
                                } catch (e: IOException) {
                                    toast.applyShow(ToastModel("文件导出失败(${e.message})", type = ToastModel.Type.Warning))
                                } catch (e: Throwable) {
                                    toast.applyShow(ToastModel("文件导出失败(${e.message})", type = ToastModel.Type.Error))
                                }
                            }
                        }

                        is DBBrowserScreenAction.ChangeCustomName -> {
                            val udiskId = currentUDisk?.udiskId
                            if (dbProject is Project && udiskId != null) {
                                val customName = action.customName
                                if (customName.isEmpty()) {
                                    dbProject.setCustomUDiskName(udiskId, null)
                                } else {
                                    dbProject.setCustomUDiskName(udiskId, customName)
                                }
                                dbProject.save()
                            }
                        }

                        is DBBrowserScreenAction.ChangeSearchingKeyword -> {
                            currentSearchingKeyword = action.keyword
                        }

                        is DBBrowserScreenAction.SwitchSearchingMode -> {
                            currentSearchingMode = action.searchingMode
                            if (currentSearchingMode) {
                                currentFiles.clear()
                                currentDirs.clear()
                                ioScope.launch {
                                    loadSearchFiles(currentUDisk?.udiskId, currentSearchingKeyword)
                                }
                            } else {
                                ioScope.launch {
                                    loadDirData(currentUDisk!!, currentParentDir!!)
                                }
                            }
                        }

                        is DBBrowserScreenAction.Search -> {
                            ioScope.launch {
                                loadSearchFiles(currentUDisk?.udiskId, action.keyword)
                            }
                        }
                    }
                }
            )
        }

        private suspend fun loadSearchFiles(udiskId: String?, keyword: String) {
            val files = dbDataProvider.searchFiles(udiskId, keyword)
            scope.launch {
                currentFiles.clear()
                currentFiles.addAll(files)
            }
        }

        private suspend fun loadRootDir(udisk: VirUdisk): VirDir {
            loading = true
            val dir = dbDataProvider.getRootDir(udisk.udiskId)
            loading = false
            return dir
        }

        private val lock = ReentrantLock()
        private suspend fun loadDirData(udisk: VirUdisk, dir: VirDir) {
            // 搜索模式下不检索当前u盘，文件夹内数据
            if (currentSearchingMode) return

            lock.lock()
            loading = true
            logger.info("Loading data for udisk ${udisk.name} and dir ${dir.path}[${dir.dirId}]")
            withContext(Dispatchers.IO) {
                currentFiles.clear()
                currentDirs.clear()
                val files = dbDataProvider.getDirFiles(udisk.udiskId, dir.dirId)
                val dirs = dbDataProvider.getDirDirs(udisk.udiskId, dir.dirId)
                scope.launch {
                    currentFiles.addAll(files)
                    currentDirs.addAll(dirs)

                    currentParentDir = dir
                }
            }
            logger.info("Data loaded for udisk ${udisk.name}[currentFiles=${currentFiles.size}, currentDirs=${currentDirs.size}]")
            loading = false
            lock.unlock()
        }
    }


    @Composable
    override fun Presenter(): DBBrowserScreenState {
        val presenter = remember { InnerPresenter() }
        return presenter.InnerPresenter()
    }

    @Composable
    fun getTitle(currentUDisk: VirUdisk?): String {
        val rawName = currentUDisk?.name
        val currentUDiskName = if (dbProject is Project) {
            val customName = dbProject.getCustomUDiskNameState(currentUDisk?.udiskId?:"未知u盘")
            if (customName != null) {
                "$customName[$rawName]"
            } else {
                rawName
            }
        } else {
            rawName
        }
        return if (currentUDiskName == null) {
            "数据管理"
        } else {
            "数据管理: $currentUDiskName"
        }
    }
}