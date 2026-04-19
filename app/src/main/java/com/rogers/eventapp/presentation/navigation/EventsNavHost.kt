package com.rogers.eventapp.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rogers.eventapp.presentation.screen.BookmarksScreen
import com.rogers.eventapp.presentation.screen.EventDetailScreen
import com.rogers.eventapp.presentation.screen.EventListScreen
import com.rogers.eventapp.presentation.ui.theme.AppDimens

data class BottomNavItem(
    val screen: Route,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)


val bottomNavItems = listOf(
    BottomNavItem(
        screen = Route.EventList,
        label = "Events",
        selectedIcon = Icons.Filled.Event,
        unselectedIcon = Icons.Outlined.Event
    ),
    BottomNavItem(
        screen = Route.Bookmarks,
        label = "Bookmarks",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder
    )
)


@Composable
fun EventsNavHost(
    startDestination: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Route.EventList.route,
        Route.Bookmarks.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(AppDimens.spaceNone),
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = startDestination ?: Route.EventList.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            // Event List
            composable(Route.EventList.route) {
                EventListScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Route.EventDetail.createRoute(eventId))
                    }
                )
            }

            // Bookmarks
            composable(Route.Bookmarks.route) {
                BookmarksScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Route.EventDetail.createRoute(eventId))
                    }
                )
            }

            // Event Detail
            composable(
                route = Route.EventDetail.route,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "eventsapp://event/{eventId}" }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                EventDetailScreen(
                    eventId = eventId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}