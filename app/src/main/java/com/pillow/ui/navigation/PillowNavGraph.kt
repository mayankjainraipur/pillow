package com.pillow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pillow.ui.screen.HomeScreen
import com.pillow.ui.screen.NoteEditorScreen
import com.pillow.ui.screen.SettingsScreen
import com.pillow.ui.screen.TrashScreen

sealed class PillowScreen(val route: String) {
    object Home : PillowScreen("home")
    object NoteEditor : PillowScreen("noteEditor/{noteId}") {
        fun createRoute(noteId: Long?) = if (noteId != null) "noteEditor/$noteId" else "noteEditor/0"
    }
    object Settings : PillowScreen("settings")
    object Trash : PillowScreen("trash")
}

@Composable
fun PillowNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = PillowScreen.Home.route
    ) {
        composable(PillowScreen.Home.route) {
            HomeScreen(
                onNoteClick = { noteId ->
                    navController.navigate(PillowScreen.NoteEditor.createRoute(noteId))
                },
                onCreateNoteClick = {
                    navController.navigate(PillowScreen.NoteEditor.createRoute(null))
                },
                onSettingsClick = {
                    navController.navigate(PillowScreen.Settings.route)
                },
                onTrashClick = {
                    navController.navigate(PillowScreen.Trash.route)
                }
            )
        }

        composable(PillowScreen.NoteEditor.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
            NoteEditorScreen(
                noteId = noteId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(PillowScreen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(PillowScreen.Trash.route) {
            TrashScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
