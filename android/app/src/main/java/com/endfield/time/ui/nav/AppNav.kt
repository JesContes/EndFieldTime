package com.endfield.time.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.endfield.time.ui.detail.DetailScreen
import com.endfield.time.ui.edit.EditScreen
import com.endfield.time.ui.home.HomeScreen

private object Routes {
    const val HOME = "home"
    const val DETAIL = "detail/{id}"
    const val EDIT = "edit?id={id}"

    fun detail(id: Long) = "detail/$id"
    fun edit(id: Long?) = if (id == null) "edit" else "edit?id=$id"
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onAdd = { navController.navigate(Routes.edit(null)) },
                onOpenDetail = { id -> navController.navigate(Routes.detail(id)) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            DetailScreen(
                milestoneId = id,
                onBack = { navController.popBackStack() },
                onEdit = { editId -> navController.navigate(Routes.edit(editId)) }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val raw = backStackEntry.arguments?.getLong("id") ?: -1L
            EditScreen(
                milestoneId = if (raw > 0L) raw else null,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
