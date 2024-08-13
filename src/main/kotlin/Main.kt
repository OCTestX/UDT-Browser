import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
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
import kotlinx.coroutines.launch
import ui.component.ToastUI
import ui.component.ToastUIState
import ui.core.UIComponent
import ui.screens.DBBrowserScreen
import ui.screens.LoadDBScreen
import utils.Colors
import utils.ListItemIterable
import utils.TmpStorage

lateinit var toast: ToastUIState
val LocalTopTitleBarState = compositionLocalOf<Main.TopTitleBarState?> { null }

fun main() = application {
    Core.init()
    Window(onCloseRequest = ::exitApplication) {
        Main.Main()
        toast = remember { ToastUIState() }
        ToastUI(toast)
    }
}

object Main: UIComponent<Main.AppAction, Main.AppState>() {
    class AppState(
        val currentScreen: HyperShareScreen,
        val canNavigateBack: Boolean,
        val navController: NavHostController,
        val currentColorScheme: ColorScheme,
        action: (AppAction) -> Unit
    ) : UIState<AppAction>(action)

    sealed class AppAction : UIAction() {
        data object Back : AppAction()
        data class Nav(val navStr: String) : AppAction()
        data object SwitchTheme : AppAction()
    }

    enum class HyperShareScreen(val title: String) {
        LoadDBScreen("载入U盘小偷数据库"),
        DBBrowserScreen("数据管理"),
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun UI(state: AppState) {
        MaterialTheme(colorScheme = state.currentColorScheme) {
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
                        CompositionLocalProvider(LocalTopTitleBarState provides TopTitleBarState(
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
        val canNavigateBack = navController.previousBackStackEntry != null
        return AppState(currentScreen, canNavigateBack, navController, currentColorScheme) { action ->
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
    }

    data class TopTitleBarState(
        val currentScreen: HyperShareScreen,
        val canNavigateBack: Boolean,
        val navigateUp: () -> Unit,
        val menuClick: () -> Unit,
    )

    @Composable
    fun GlobalTopAppBar(
        state: TopTitleBarState,
        modifier: Modifier = Modifier,
        actions: @Composable RowScope.() -> Unit = {}
    ) {
        CenterAlignedTopAppBar(
            title = { Text(state.currentScreen.title, color = MaterialTheme.colorScheme.primary) },
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
            Column(Modifier.padding(12.dp).weight(1f)) {
                Image(
                    painterResource("author1.png"),
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp).size(145.dp).align(Alignment.CenterHorizontally).clip(MaterialTheme.shapes.large).border(3.dp, MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large)
                )
                Text("Code by OCTest", modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.padding(12.dp)) {
                IconButton(onClick = {
                    state.action(AppAction.SwitchTheme)
                }) {
                    Icon(TablerIcons.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}