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
        val touchOptimized: Boolean,
        val searchingKeyword: String,
        val searchingMode: Boolean,
        val loading: Boolean,
        val needCopyFileTotalSize: Long,
        val copiedFileTotalSize: Long,
        val allCopingProgress: Float,
        val enableThumbnail: Boolean,
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
        data class Search(val keyword: String, val involveAllDisk: Boolean): DBBrowserScreenAction()
        data class ExportFile(val virFile: VirFile, val targetDir: File): DBBrowserScreenAction()
        data class ExportFileToDesktop(val virFile: VirFile): DBBrowserScreenAction()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun UI(state: DBBrowserScreenState) {
        Column {
            var setCustomNameVisible by remember { mutableStateOf(false) }
            setCustomNameVisibleDialogWindow(setCustomNameVisible, state) {
                setCustomNameVisible = false
            }
            var selectStorageDirVisible by remember { mutableStateOf(false) }
            selectStorageDirVisibleDialogWindow(selectStorageDirVisible) {
                selectStorageDirVisible = false
            }
            var selectedVirFile: VirFile? by remember { mutableStateOf(null) }
            selectFileDialogWindow(selectedVirFile, state, unSelectedVirFile = {
                selectedVirFile = null
            })
            GlobalTopAppBar(state, setCustomNameVisible = { setCustomNameVisible = it }, setSelectStorageDirVisible = { selectStorageDirVisible = it })
            Animates.VisibilityAnimates {
                TitleBar(state)
            }
            Box(modifier = Modifier.weight(1.0f)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    AllUdisks(state)
                    CurrentFilesAndDir(state, setSelectedVirFile = {
                        selectedVirFile = it
                    })
                }
                if (state.loading) {
                    Animates.VisibilityAnimates {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                }
            }
            BottomInfo(state)
        }
    }

    @Composable
    fun BottomInfo(state: DBBrowserScreenState) {
        Row(modifier = Modifier.padding(6.dp)) {
            val coping by remember(state.needCopyFileTotalSize) {
                derivedStateOf {
                    state.needCopyFileTotalSize > 0
                }
            }
            AnimatedContent(coping, modifier = Modifier.align(Alignment.CenterVertically)) {
                if (it) {
                    Row {
                        LinearProgressIndicator(progress = state.allCopingProgress, modifier = Modifier.weight(1.0f).align(Alignment.CenterVertically))
                        Text(fileSize(state.needCopyFileTotalSize - state.copiedFileTotalSize), modifier = Modifier.align(Alignment.CenterVertically))
                    }
                } else {
                    Text("无复制任务", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun selectFileDialogWindow(selectedVirFile: VirFile?, state: DBBrowserScreenState, unSelectedVirFile: () -> Unit) {
        DialogWindow(onCloseRequest = {
            unSelectedVirFile()
        }, visible = (selectedVirFile != null)) {
            if (selectedVirFile != null) {
                Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                    Row {
                        IconButton(onClick = {
                            unSelectedVirFile()
                        }, modifier = Modifier.align(Alignment.CenterVertically)) {
                            Icon(rememberVectorPainter(TablerIcons.ArrowBack), contentDescription = null)
                        }
                        Text("文件信息", modifier = Modifier.align(Alignment.CenterVertically))
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Icon(TablerIcons.File, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 6.dp, top = 12.dp, bottom = 12.dp).align(Alignment.CenterVertically))
                        Card(Modifier.padding(horizontal = 6.dp, vertical = 12.dp)) {
                            Column(Modifier.padding(6.dp)) {
                                Row(Modifier.fillMaxWidth()) {
                                    Column(Modifier.weight(1f)) {
                                        Text(selectedVirFile.name, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Animates.VisibilityAnimates {
                                    Text(fileSize(selectedVirFile.size), style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.height(12.dp))
                                val disk = remember(selectedVirFile) { dbDataProvider.getUDisk(selectedVirFile.udiskId) }
                                val customName = (dbProject as Project).getCustomUDiskNameState(disk.udiskId)?: disk.name
                                Animates.VisibilityAnimates {
                                    val filePath = remember(selectedVirFile) {
                                        "[$customName](${fileSize(disk.totalSize - disk.freeSize)} / ${fileSize(disk.totalSize)}) \n ${dbDataProvider.getFilePath(selectedVirFile)}"
                                    }
                                    Text(filePath, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    val selectExportTargetDirLauncher = rememberSelectExportTargetDirLauncher {
                        if (it != null) {
                            state.action(DBBrowserScreenAction.ExportFile(selectedVirFile, it.mustDir()))
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(16.dp).weight(1.0f)) {
                        Button(onClick = {
                            state.action(DBBrowserScreenAction.ExportFileToDesktop(selectedVirFile))
                            unSelectedVirFile()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("发送到桌面")
                        }
                        Button(onClick = {
                            selectExportTargetDirLauncher.launch()
                            unSelectedVirFile()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("导出")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun rememberSelectExportTargetDirLauncher(selected: (File?) -> Unit): PickerResultLauncher {
        return rememberDirectoryPickerLauncher(
            title = "选择文件存储库",
        ) { selectedFile ->
            // Handle the picked files
            if (selectedFile != null) {
                val file = selectedFile.file
                if (file.exists()) {
                    selected(file)
                }
                selected(null)
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun RowScope.CurrentFilesAndDir(state: DBBrowserScreenState, setSelectedVirFile: (VirFile) -> Unit) {
        Animates.VisibilityAnimates {
            AnimatedContent(state.touchOptimized) { touchOptimized ->
                if (touchOptimized) {
                    val listDirsAndFilesScrollState = rememberLazyListState()
                    Row (modifier = Modifier.weight(1f).animateContentSize()) {
                        LazyColumn(modifier = Modifier.weight(1f), state = listDirsAndFilesScrollState) {
                            items(state.currentDirs, key = { it.dirId }) { dir ->
                                UDirCard(dir, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                    state.action(DBBrowserScreenAction.SwitchDir(dir))
                                }, dbDataProvider)
                            }
                            items(state.currentFiles, key = { it.fileId }) { file ->
                                UFileCard(file, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                    setSelectedVirFile(file)
                                }, dbProject)
                            }
                        }
                        AnimatedVisibility(state.touchOptimized) {
                            // 滚动条
                            VerticalScrollbar(
                                modifier = Modifier.fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(listDirsAndFilesScrollState),
                                style = LocalScrollbarStyle.current.copy(unhoverColor = MaterialTheme.colorScheme.primary, hoverColor = MaterialTheme.colorScheme.inversePrimary)
                            )
                        }
                    }
                } else {
                    val listDirsAndFilesScrollState = rememberLazyGridState()
                    LazyVerticalGrid(columns = GridCells.Adaptive(state.fileCardMinWidth.dp), modifier = Modifier.weight(1f), state = listDirsAndFilesScrollState) {
                        items(state.currentDirs, key = { it.dirId }) { dir ->
                            UDirCard(dir, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                state.action(DBBrowserScreenAction.SwitchDir(dir))
                            }, dbDataProvider)
                        }
                        items(state.currentFiles, key = { it.fileId }) { file ->
                            UFileCard(file, modifier = Modifier.animateItemPlacement().padding(6.dp).height(80.dp).clickable {
                                setSelectedVirFile(file)
                            },dbProject)
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun AllUdisks(state: DBBrowserScreenState) {
        val disksScrollState = rememberLazyListState()
        AnimatedVisibility(state.currentParentDir?.isRoot == true || state.currentParentDir == null) {
            Animates.VisibilityAnimates {
                Column {
                    Row(modifier = Modifier.width(200.dp).animateContentSize().weight(1f)) {
                        LazyColumn(Modifier.weight(1f), state = disksScrollState) {
                            items(state.allUdisks, key = { it.udiskId }) { udisk ->
                                UDiskCard(udisk, modifier = Modifier.animateItemPlacement().padding(6.dp).clickable {
                                    state.action(DBBrowserScreenAction.SelectUdisk(udisk))
                                }, dbProject)
                            }
                        }
                        AnimatedVisibility(state.touchOptimized) {
                            // 滚动条
                            VerticalScrollbar(
                                modifier = Modifier.fillMaxHeight(),
                                adapter = rememberScrollbarAdapter(disksScrollState),
                                style = LocalScrollbarStyle.current.copy(unhoverColor = MaterialTheme.colorScheme.primary, hoverColor = MaterialTheme.colorScheme.inversePrimary)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun GlobalTopAppBar(state: DBBrowserScreenState, setCustomNameVisible: (Boolean) -> Unit, setSelectStorageDirVisible: (Boolean) -> Unit) {
        if (dbProject is Project) {
            Main.GlobalTopAppBar(getTitle(state.currentUDisk), LocalMainTopTitleBarState.current!!) {
                IconButton(onClick = {
                    setCustomNameVisible(true)
                }) {
                    Icon(TablerIcons.EditCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    setSelectStorageDirVisible(true)
                }) {
                    Icon(TablerIcons.FolderPlus, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    @Composable
    fun selectStorageDirVisibleDialogWindow(selectStorageDirVisible: Boolean, closeSelectStorageDirVisible: () -> Unit) {
        val selectedStorageDirLauncher = rememberSelectedStorageDirLauncher {
            closeSelectStorageDirVisible()
        }
        if (dbProject is Project) {
            DialogWindow(onCloseRequest = {
                closeSelectStorageDirVisible()
            }, visible = selectStorageDirVisible) {
                var pathStr by remember(dbProject.storageDir) { mutableStateOf(dbProject.storageDir ?: "") }
                Column(Modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()) {
                    Text(
                        "选择文件存储库位置",
                        modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
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
                            closeSelectStorageDirVisible()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("保存")
                        }
                        Button(onClick = {
                            closeSelectStorageDirVisible()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun setCustomNameVisibleDialogWindow(setCustomNameVisible: Boolean, state: DBBrowserScreenState, closeCustomNameVisible: () -> Unit) {
        if (dbProject is Project) {
            DialogWindow(onCloseRequest = {
                closeCustomNameVisible()
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
                            closeCustomNameVisible()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("保存")
                        }
                        Button(onClick = {
                            closeCustomNameVisible()
                        }, modifier = Modifier.padding(horizontal = 8.dp).weight(1f)) {
                            Text("取消")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun rememberSelectedStorageDirLauncher(closeSelectStorageDirVisible: () -> Unit): PickerResultLauncher {
        return rememberDirectoryPickerLauncher(
            title = "选择文件存储库",
        ) { selectedFile ->
            // Handle the picked files
            if (selectedFile != null) {
                val file = selectedFile.file
                if (file.exists()) {
                    (dbProject as Project).storageDir = file.absolutePath
                    dbProject.save()
                }
                closeSelectStorageDirVisible()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TitleBar(state: DBBrowserScreenState) {
        Row {
            IconToggleButton(state.searchingMode, onCheckedChange = {state.action(DBBrowserScreenAction.SwitchSearchingMode(it))}) {
                Icon(rememberVectorPainter(TablerIcons.Search), contentDescription = null)
            }

            var involveAllDisk by remember { mutableStateOf(true) }
            AnimatedContent(state.searchingMode) {
                Row(Modifier.weight(1f).align(Alignment.CenterVertically)) {
                    if (it) {
                        IconToggleButton(involveAllDisk, onCheckedChange = {
                            involveAllDisk = it
                            state.action(DBBrowserScreenAction.Search(state.searchingKeyword, involveAllDisk))
                        }) {
                            Icon(rememberVectorPainter(TablerIcons.BorderAll), contentDescription = null)
                        }
                        OutlinedTextField(state.searchingKeyword, {
                            state.action(DBBrowserScreenAction.ChangeSearchingKeyword(it))
                            state.action(DBBrowserScreenAction.Search(it, involveAllDisk))
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

    inner class InnerPresenter {
        private val allUdisks = mutableStateListOf<VirUdisk>()
        private var currentUDisk by mutableStateOf<VirUdisk?>(null)
        private val currentFiles = mutableStateListOf<VirFile>()
        private val currentDirs = mutableStateListOf<VirDir>()
        private var currentParentDir by mutableStateOf<VirDir?>(null)
        private var fileCardMinWidth by WorkDir.globalServiceConfig.fileCardMinWidth
        private var touchOptimized by WorkDir.globalServiceConfig.touchOptimized
        private var currentSearchingMode by mutableStateOf(false)
        private var currentSearchingKeyword by mutableStateOf("")
        private var loading by mutableStateOf(false)
        private var needCopyFileTotalSize by mutableStateOf(0L)
        private var copiedFileTotalSize by mutableStateOf(0L)
        private var allCopingProgress by mutableFloatStateOf(1f)
        private var enableThumbnail by WorkDir.globalServiceConfig.enableThumbnail

        fun actionLink(action: DBBrowserScreenAction) {
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
                    val saveDir = WorkDir.globalServiceConfig.tempDir
                    actionLink(DBBrowserScreenAction.ExportFile(action.file, saveDir))
                    // TODO openFile
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
                        loadSearchFiles(
                            if (action.involveAllDisk) null else currentUDisk?.udiskId,
                            action.keyword
                        )
                    }
                }

                is DBBrowserScreenAction.ExportFile -> {
                    ioScope.launch {
                        val virFile = action.virFile
                        val name = virFile.name
                        val udiskId = virFile.udiskId
                        val fileId = virFile.fileId

                        try {
                            val sourceInputStream = dbProject.getFileInputStream(udiskId, fileId)
                            val sourceSize = dbProject.getFileSize(udiskId, fileId)
                            //saveDir
                            val saveDir = action.targetDir
                            val targetFile = saveDir.linkFile(name)
                            val speedCalculator = SpeedCalculator()
                            needCopyFileTotalSize += sourceSize
                            logger.info { "${fileSize(copiedFileTotalSize)} / ${fileSize(needCopyFileTotalSize)}" }
                            allCopingProgress = (copiedFileTotalSize.toDouble() / needCopyFileTotalSize).toFloat()
                            sourceInputStream.autoTransferTo(targetFile.outputStream()) { _, current ->
                                // TODO 计算速度
//                                        val progress = (copied.toDouble() * 100 / sourceSize).toFloat()
//                                        val currentSpeed = speedCalculator.calculate(copied)
//                                        logger.debug { "progress[${fileSize(currentSpeed)}/s]: $progress" }
                                copiedFileTotalSize += current
                                logger.info { "${fileSize(copiedFileTotalSize)} / ${fileSize(needCopyFileTotalSize)}" }
                                allCopingProgress = (copiedFileTotalSize.toDouble() / needCopyFileTotalSize).toFloat()
                            }
                            copiedFileTotalSize -= sourceSize
                            needCopyFileTotalSize -= needCopyFileTotalSize
                            logger.info { "${fileSize(copiedFileTotalSize)} / ${fileSize(needCopyFileTotalSize)}" }
                            if (needCopyFileTotalSize == 0L) {
                                allCopingProgress = 1f
                            } else {
                                allCopingProgress = (copiedFileTotalSize.toDouble() / needCopyFileTotalSize).toFloat()
                            }
                            logger.debug { "file export complete: ${targetFile.absolutePath}[${speedCalculator.getUsedTime()} ms]" }
                            toast.applyShow(ToastModel("文件导出完成(${targetFile.absolutePath})", type = ToastModel.Type.Success))
                        } catch (e: IOException) {
                            toast.applyShow(ToastModel("文件导出失败(${e.message})", type = ToastModel.Type.Warning))
                            logger.error { "文件导出失败[IOException](${e.message})" }
                        } catch (e: Throwable) {
                            toast.applyShow(ToastModel("文件导出失败(${e.message})", type = ToastModel.Type.Error))
                            logger.error { "文件导出失败(${e.message})" }
                        }
                    }
                }
                is DBBrowserScreenAction.ExportFileToDesktop -> {
                    val desktopDir = File(WorkDir.globalServiceConfig.desktopDirLocation.value)
                    if (desktopDir.exists() && desktopDir.isDirectory) {
                        actionLink(DBBrowserScreenAction.ExportFile(action.virFile, desktopDir))
                    } else {
                        logger.warn { "桌面文件夹目录有问题: ${desktopDir.absolutePath}" }
                        toast.applyShow(ToastModel("桌面文件夹目录有问题: ${desktopDir.absolutePath}", type = ToastModel.Type.Warning))
                    }
                }
            }
        }

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
                touchOptimized,
                currentSearchingKeyword,
                currentSearchingMode,
                loading,
                needCopyFileTotalSize,
                copiedFileTotalSize,
                allCopingProgress,
                enableThumbnail,
                action = { action ->
                    actionLink(action)
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