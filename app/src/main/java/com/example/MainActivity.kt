package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.MovieHubLogo
import com.example.ui.screens.*
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NearBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextWhite
import com.example.viewmodel.MovieHubViewModel

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val WATCHLIST = "watchlist"
    const val ABOUT = "about"
    const val LIST = "list/{category}/{genreId}/{genreName}"
    const val DETAIL = "detail/{mediaType}/{id}"
    const val WATCH = "watch/{mediaType}/{id}/{season}/{episode}"
    const val ACTOR = "actor/{id}"
}

enum class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    HOME(Routes.HOME, "Home", Icons.Default.Home),
    SEARCH(Routes.SEARCH, "Search", Icons.Default.Search),
    WATCHLIST(Routes.WATCHLIST, "Saved", Icons.Default.Favorite),
    ABOUT(Routes.ABOUT, "About", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MovieHubApp()
            }
        }
    }
}

@Composable
fun MovieHubApp() {
    val navController = rememberNavController()
    val viewModel: MovieHubViewModel = viewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Bottom bar is only visible on the root tab screens
    val rootTabs = listOf(Routes.HOME, Routes.SEARCH, Routes.WATCHLIST, Routes.ABOUT)
    val shouldShowBottomBar = currentRoute in rootTabs

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (shouldShowBottomBar) {
                Surface(
                    color = NearBlack,
                    tonalElevation = 8.dp,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MovieHubLogo()
                    }
                }
            }
        },
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = SurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding().testTag("bottom_navigation_bar")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) AmberGold else Color.Gray
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) AmberGold else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SurfaceDark // Avoid default high contrast circle indicator
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .background(NearBlack)
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onMediaClick = { mediaType, id ->
                        navController.navigate("detail/$mediaType/$id")
                    },
                    onActorClick = { actorId ->
                        navController.navigate("actor/$actorId")
                    },
                    onSeeAllClick = { category, genreId, genreName ->
                        navController.navigate("list/$category/$genreId/$genreName")
                    }
                )
            }
            
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument("mediaType") { type = NavType.StringType },
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                DetailScreen(
                    viewModel = viewModel,
                    mediaType = mediaType,
                    id = id,
                    onBackClick = { navController.popBackStack() },
                    onActorClick = { actorId ->
                        navController.navigate("actor/$actorId")
                    },
                    onMediaClick = { mType, mId ->
                        navController.navigate("detail/$mType/$mId")
                    },
                    onPlayClick = { mType, mId, season, episode ->
                        navController.navigate("watch/$mType/$mId/$season/$episode")
                    }
                )
            }
            
            composable(
                route = Routes.WATCH,
                arguments = listOf(
                    navArgument("mediaType") { type = NavType.StringType },
                    navArgument("id") { type = NavType.IntType },
                    navArgument("season") { type = NavType.IntType },
                    navArgument("episode") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: "movie"
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                val season = backStackEntry.arguments?.getInt("season") ?: 1
                val episode = backStackEntry.arguments?.getInt("episode") ?: 1
                PlayerScreen(
                    viewModel = viewModel,
                    mediaType = mediaType,
                    id = id,
                    initialSeason = season,
                    initialEpisode = episode,
                    onBackClick = { navController.popBackStack() }
                )
            }
            
            composable(
                route = Routes.ACTOR,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val actorId = backStackEntry.arguments?.getInt("id") ?: 0
                ActorScreen(
                    viewModel = viewModel,
                    actorId = actorId,
                    onBackClick = { navController.popBackStack() },
                    onMediaClick = { mType, mId ->
                        navController.navigate("detail/$mType/$mId")
                    }
                )
            }
            
            composable(
                route = Routes.LIST,
                arguments = listOf(
                    navArgument("category") { type = NavType.StringType },
                    navArgument("genreId") { type = NavType.IntType },
                    navArgument("genreName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val genreId = backStackEntry.arguments?.getInt("genreId") ?: -1
                val genreName = backStackEntry.arguments?.getString("genreName") ?: ""
                ListScreen(
                    viewModel = viewModel,
                    category = category,
                    genreId = genreId,
                    genreName = genreName,
                    onBackClick = { navController.popBackStack() },
                    onMediaClick = { mType, mId ->
                        navController.navigate("detail/$mType/$mId")
                    }
                )
            }
            
            composable(Routes.SEARCH) {
                SearchScreen(
                    viewModel = viewModel,
                    onMediaClick = { mType, mId ->
                        navController.navigate("detail/$mType/$mId")
                    },
                    onActorClick = { actorId ->
                        navController.navigate("actor/$actorId")
                    }
                )
            }
            
            composable(Routes.WATCHLIST) {
                WatchlistHistoryScreen(
                    viewModel = viewModel,
                    onMediaClick = { mType, mId ->
                        navController.navigate("detail/$mType/$mId")
                    }
                )
            }
            
            composable(Routes.ABOUT) {
                AboutScreen()
            }
        }
    }
}
