import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import browser.Project
import compose.icons.TablerIcons
import compose.icons.tablericons.Menu
import compose.icons.tablericons.Palette
import io.github.vinceglb.filekit.compose.PickerResultLauncher
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.launch
import ui.component.ToastUI
import ui.component.ToastUIState
import ui.core.UIComponent
import ui.screens.DBBrowserScreen
import ui.screens.DBManagerUDiskScreen
import ui.screens.LoadDBScreen
import ui.utils.UIAdditional
import utils.Colors
import utils.ListItemIterable
import utils.TmpStorage
import java.io.File

lateinit var toast: ToastUIState
val LocalMainTopTitleBarState = compositionLocalOf<Main.MainTopTitleBarState?> { null }
private val MainWindowState: WindowState = WindowState(width = WorkDir.globalServiceConfig.windowSize.value.first.dp, height = WorkDir.globalServiceConfig.windowSize.value.second.dp)
fun main() = application {
    Core.init()
    Window(
        onCloseRequest = ::exitApplication,
        title = "UDTBrowser",
        state = MainWindowState
    ) {
        Main.Main()
    }
}

object Main: UIComponent<Main.AppAction, Main.AppState>() {
    class AppState(
        val currentScreen: HyperShareScreen,
        val canNavigateBack: Boolean,
        val navController: NavHostController,
        val currentColorScheme: ColorScheme,
        val availableTypography: Map<String, Typography>,
        val currentTypographyName: String,
        val currentTypography: Typography,
        action: (AppAction) -> Unit
    ) : UIState<AppAction>(action)

    sealed class AppAction : UIAction() {
        data object Back : AppAction()
        data class Nav(val navStr: String) : AppAction()
        data object SwitchTheme : AppAction()
        data class SelectTypography(val typographyName: String) : AppAction()
    }

    enum class HyperShareScreen() {
        LoadDBScreen,
        DBBrowserScreen,
        DBManagerUDiskScreen,
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun UI(state: AppState) {
        MaterialTheme(colorScheme = state.currentColorScheme, typography = state.currentTypography) {
            Row(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                ModalNavigationDrawer(
                    drawerContent = {
                        DrawerContent(state)
                    }, drawerState = drawerState
                ){
                    Scaffold(topBar = {
//                        HyperShareTopAppBar(
//                            state.currentScreen,
//                            state.canNavigateBack,
//                            navigateUp = {
//                                state.action(AppAction.Back)
//                            }
//                        )
                    }) { innerPadding ->
                        CompositionLocalProvider(LocalMainTopTitleBarState provides MainTopTitleBarState(
                            state.currentScreen,
                            state.canNavigateBack,
                            navigateUp = {
                                state.action(AppAction.Back)
                            }, menuClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        )) {
                            NavHost(
                                state.navController,
                                startDestination = HyperShareScreen.LoadDBScreen.name,
                                modifier = Modifier.fillMaxSize().padding(innerPadding)
                            ) {
                                buildHosts(state)
                            }
                        }
                    }
                }
            }
            toast = remember { ToastUIState() }
            ToastUI(toast)
        }
    }

    @Composable
    override fun Presenter(): AppState {
        val navController = rememberNavController()
        // Get current back stack entry
        val backStackEntry by navController.currentBackStackEntryAsState()
        // Get the name of the current screen
        val currentScreen = HyperShareScreen.valueOf(
            backStackEntry?.destination?.route?.split("/")?.firstOrNull() ?: HyperShareScreen.LoadDBScreen.name
        )
        val currentColorSchemeIterable = remember { ListItemIterable(Colors.ThemeColorScheme.schemes, WorkDir.globalServiceConfig.currentThemeSchemeIndex) }
        var currentColorScheme by remember { mutableStateOf(Colors.ThemeColorScheme.schemes[currentColorSchemeIterable.currentIndex]) }
        val availableTypography = mapOf(
            "Default" to UIAdditional.Typographies.DefaultTypography,
            "Mi Sans" to UIAdditional.Typographies.MiSansTypography,
        )
        var currentTypography by remember { mutableStateOf(WorkDir.globalServiceConfig.typographyName.value to availableTypography[WorkDir.globalServiceConfig.typographyName.value]!!) }
        val canNavigateBack = navController.previousBackStackEntry != null
        return AppState(currentScreen, canNavigateBack, navController, currentColorScheme, availableTypography, currentTypography.first, currentTypography.second) { action ->
            when (action) {
                AppAction.Back -> navController.navigateUp()
                is AppAction.Nav -> {
                    navController.navigate(action.navStr)
                }

                AppAction.SwitchTheme -> {
                    currentColorScheme = currentColorSchemeIterable.next()
                    WorkDir.globalServiceConfig.currentThemeSchemeIndex = currentColorSchemeIterable.currentIndex
                    WorkDir.saveServiceConfig()
                }

                is AppAction.SelectTypography -> {
                    val typography = availableTypography[action.typographyName]
                    if (typography != null) {
                        currentTypography = action.typographyName to typography
                        WorkDir.globalServiceConfig.typographyName.value = action.typographyName
                    } else {
//                        currentTypography = "Default" to UIAdditional.Typographies.DefaultTypography
                    }
                }
            }
        }
    }

    private fun NavGraphBuilder.buildHosts(state: AppState) {
        composable(route = HyperShareScreen.LoadDBScreen.name) {
            val scanner = remember {
                LoadDBScreen(launchDBBrowserScreen = {
                    logger.info("Launching DBBrowserScreen with file: $it")
                    val key = TmpStorage.store(it)
                    state.action(AppAction.Nav(HyperShareScreen.DBBrowserScreen.name + "/$key"))
                }, launchManagerUDiskScreen = {
                    logger.info("Launching ManagerUDiskScreen")
                    val key = TmpStorage.store(it)
                    state.action(AppAction.Nav(HyperShareScreen.DBManagerUDiskScreen.name + "/$key"))
                })
            }
            scanner.Main()
        }
        composable(
            route = HyperShareScreen.DBBrowserScreen.name+"/{tkey_project}",
            arguments = listOf(navArgument("tkey_project") { type = NavType.StringType })
        ) {
            val args = requireNotNull(it.arguments)
            val project = TmpStorage.retrieve(args.getString("tkey_project")!!, Project::class.java)!!
            val dbBrowser = remember { DBBrowserScreen(project) }
            dbBrowser.Main()
        }
        composable(
            route = HyperShareScreen.DBManagerUDiskScreen.name+"/{tkey_manager_udisk_root_dir}",
            arguments = listOf(navArgument("tkey_manager_udisk_root_dir") { type = NavType.StringType })
        ) {
            val args = requireNotNull(it.arguments)
            val managerUDiskRootDir = TmpStorage.retrieve(args.getString("tkey_manager_udisk_root_dir")!!, File::class.java)!!
            val dbBrowser = remember { DBManagerUDiskScreen(managerUDiskRootDir, navigateToBrowseFiles = { project ->
                logger.info("Launching DBBrowserScreen form managerUDiskRootDir: $project")
                val key = TmpStorage.store(project)
                state.action(AppAction.Nav(HyperShareScreen.DBBrowserScreen.name + "/$key"))
            }) }
            dbBrowser.Main()
        }
    }

    data class MainTopTitleBarState(
        val currentScreen: HyperShareScreen,
        val canNavigateBack: Boolean,
        val navigateUp: () -> Unit,
        val menuClick: () -> Unit,
    )

    @Composable
    fun GlobalTopAppBar(
        title: String,
        state: MainTopTitleBarState,
        modifier: Modifier = Modifier,
        actions: @Composable RowScope.() -> Unit = {}
    ) {
        CenterAlignedTopAppBar(
            title = {
                AnimatedContent(title) {
                    Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                }
            },
            modifier = modifier,
            navigationIcon = {
                Row {
                    AnimatedVisibility(state.canNavigateBack) {
                        IconButton(onClick = state.navigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = state.menuClick) {
                        Icon(
                            imageVector = TablerIcons.Menu,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }, actions = actions
        )
    }

    @Composable
    fun DrawerContent(state: AppState) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.weight(1f)) {
                val disksScrollState = rememberLazyListState()
                LazyColumn(Modifier.padding(12.dp).weight(1f), state = disksScrollState) {
                    item {
                        Image(
                            painterResource("author1.png"),
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp).size(145.dp).clip(MaterialTheme.shapes.large).border(3.dp, MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large)
                        )
                        Text("Code by OCTest", modifier = Modifier, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        Text("触控屏优化[开启后会将多列列表换成单列列表]")
                        var touchOptimized by remember { WorkDir.globalServiceConfig.touchOptimized }
                        Switch(touchOptimized, onCheckedChange = { touchOptimized = it })
                    }
                    item {
                        Text("Settings", fontSize = MaterialTheme.typography.labelLarge.fontSize, color = MaterialTheme.colorScheme.primary)
                    }
                    item {
                        Text("调节文件项最小宽度: ${WorkDir.globalServiceConfig.fileCardMinWidth.value}dp")
                        var fileItemMinWidth by remember { WorkDir.globalServiceConfig.fileCardMinWidth }
                        Slider(fileItemMinWidth.toFloat(), onValueChange = { fileItemMinWidth = it.toInt() }, valueRange = 50f..1000f, steps = 25, colors = SliderDefaults.colors())
                    }
                    item {
                        Text("调节目录大小遍历统计动画延迟: ${WorkDir.globalServiceConfig.dirSizeEachCountAnimateDelay.value}ms")
                        var dirSizeEachCountAnimateDelay by remember { WorkDir.globalServiceConfig.dirSizeEachCountAnimateDelay }
                        Slider(dirSizeEachCountAnimateDelay.toFloat(), onValueChange = { dirSizeEachCountAnimateDelay = it.toLong() }, valueRange = 0f..1000f, steps = 100, colors = SliderDefaults.colors())
                    }
                    item {
                        Text("桌面文件夹位置")
                        Row {
                            var desktopDirLocation by remember { WorkDir.globalServiceConfig.desktopDirLocation }
                            val selectedStorageDirLauncher = rememberSelectDesktopDirLocationLauncher {}
                            OutlinedTextField(desktopDirLocation, onValueChange = {
                                desktopDirLocation = it
                            })
                            IconButton(modifier = Modifier.align(Alignment.CenterVertically), onClick = {
                                selectedStorageDirLauncher.launch()
                            }) {
                                Icon(Icons.Default.LocationOn, contentDescription = null)
                            }
                        }
                    }
                    item {
                        Text("启用缩略图: ${WorkDir.globalServiceConfig.enableThumbnail.value}")
                        var enableThumbnail by remember { WorkDir.globalServiceConfig.enableThumbnail }
                        Switch(enableThumbnail, onCheckedChange = { enableThumbnail = it })
                    }

                    item {
                        Text("字体风格")
                    }
                    items(state.availableTypography.entries.toList()) { entry ->
                        Text(entry.key, color = if (WorkDir.globalServiceConfig.typographyName.value == entry.key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary, style = entry.value.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).clickable {
                            state.action(AppAction.SelectTypography(entry.key))
                        })
                    }
                }
                AnimatedVisibility(WorkDir.globalServiceConfig.touchOptimized.value) {
                    // 滚动条
                    VerticalScrollbar(
                        modifier = Modifier.fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(disksScrollState),
                        style = LocalScrollbarStyle.current.copy(unhoverColor = MaterialTheme.colorScheme.primary, hoverColor = MaterialTheme.colorScheme.inversePrimary)
                    )
                }
            }
            Row(Modifier.padding(12.dp)) {
                Button(onClick = {
                    WorkDir.globalServiceConfig.windowSize.value = Pair(MainWindowState.size.width.value.toInt(), MainWindowState.size.height.value.toInt())
                    WorkDir.saveServiceConfig()
                    toast.applyShow("保存成功")
                }) {
                    Text("保存")
                }
                IconButton(onClick = {
                    state.action(AppAction.SwitchTheme)
                }) {
                    Icon(TablerIcons.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    @Composable
    fun rememberSelectDesktopDirLocationLauncher(closeSelectStorageDirVisible: () -> Unit): PickerResultLauncher {
        return rememberDirectoryPickerLauncher(
            title = "选择桌面文件夹",
        ) { selectedFile ->
            // Handle the picked files
            if (selectedFile != null) {
                val file = selectedFile.file
                if (file.exists()) {
                    WorkDir.globalServiceConfig.desktopDirLocation.value = file.absolutePath
                }
                closeSelectStorageDirVisible()
            }
        }
    }
}