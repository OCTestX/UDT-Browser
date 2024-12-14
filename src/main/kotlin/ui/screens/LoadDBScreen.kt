package ui.screens

import LocalMainTopTitleBarState
import WorkDir
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import browser.DBFile
import browser.Project
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import toast
import ui.core.UIComponent
import java.io.File

class LoadDBScreen(val launchDBBrowserScreen: (Project) -> Unit, val launchManagerUDiskScreen: (File) -> Unit): UIComponent<LoadDBScreen.ScannerAction, LoadDBScreen.LoadDBScreenState>() {
    class LoadDBScreenState(
        val recentProjects: List<Project>,
        action: (ScannerAction) -> Unit
    ) : UIState<ScannerAction>(action)
    sealed class ScannerAction: UIAction() {
        class OpenDBFile(val dbFileSource: File) : ScannerAction()
        class OpenRecentProject(val project: Project) : ScannerAction()
        class OpenManagerUDisk(val udiskRootDir: File) : ScannerAction()
    }

    @Composable
    override fun UI(state: LoadDBScreenState) {
        val importDBFileLauncher = rememberFilePickerLauncher(
            type = PickerType.File(extensions = listOf("db")),
            mode = PickerMode.Single,
            title = "选择数据库文件",
        ) { file ->
            // Handle the picked files
            if (file != null) {
                state.action(ScannerAction.OpenDBFile(file.file))
            }
        }
        val openManagerUDiskLauncher = rememberDirectoryPickerLauncher(
            title = "选择管理U盘",
        ) { selectedFile ->
            // Handle the picked files
            println("selectedFile: $selectedFile")
            if (selectedFile != null) {
                val file = selectedFile.file
                if (file.exists()) {
                    state.action(ScannerAction.OpenManagerUDisk(file))
                }
            }
        }
        Column {
            Main.GlobalTopAppBar("载入U盘小偷数据库", LocalMainTopTitleBarState.current!!)
            Box(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.align(Alignment.Center)) {
                    Text("打开数据项目", fontSize = 24.sp, modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        importDBFileLauncher.launch()
                    }, modifier = Modifier.align(Alignment.CenterHorizontally).width(320.dp)) {
                        Text("选择数据库文件")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        TODO()
                    }, modifier = Modifier.align(Alignment.CenterHorizontally).width(320.dp)) {
                        Text("导入远程服务器")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        openManagerUDiskLauncher.launch()
                    }, modifier = Modifier.align(Alignment.CenterHorizontally).width(320.dp)) {
                        Text("打开管理U盘")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("最近打开的项目", fontSize = 20.sp, modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                    LazyColumn(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        items(state.recentProjects) { project ->
                            RecentProjectCard(state, project, modifier = Modifier.width(240.dp).padding(6.dp))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RecentProjectCard(state: LoadDBScreenState, project: Project, modifier: Modifier = Modifier) {
        Card(modifier = modifier, onClick = {
            state.action(ScannerAction.OpenRecentProject(project))
        }) {
            Text(text = project.name, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
        }
    }

    @Composable
    override fun Presenter(): LoadDBScreenState {
        val recentProjects = remember { WorkDir.globalServiceConfig.recentProjects }
        return LoadDBScreenState(
            recentProjects = recentProjects,
            action = { action ->
                when (action) {
                    is ScannerAction.OpenDBFile -> {
                        scope.launch {
                            val dbFile = DBFile(action.dbFileSource)
                            val project = Project.create(dbFile)
                            launchDBBrowserScreen(project)
                            if (WorkDir.globalServiceConfig.recentProjects.contains(project).not()) {
                                WorkDir.globalServiceConfig.recentProjects.add(project)
                                WorkDir.saveServiceConfig()
                            }
                        }
                    }
                    is ScannerAction.OpenRecentProject -> {
                        scope.launch {
                            val project = action.project
                            launchDBBrowserScreen(project)
                        }
                    }

                    is ScannerAction.OpenManagerUDisk -> {
                        scope.launch {
                            val udiskRootDir = action.udiskRootDir
                            launchManagerUDiskScreen(udiskRootDir)
                        }
                    }
                }
            }
        )
    }
}