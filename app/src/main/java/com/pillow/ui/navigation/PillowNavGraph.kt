package com.pillow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pillow.ui.screen.BackupScreen
import com.pillow.ui.screen.BucketDetailScreen
import com.pillow.ui.screen.BucketsScreen
import com.pillow.ui.screen.HomeScreen
import com.pillow.ui.screen.NoteEditorScreen
import com.pillow.ui.screen.NoteSection
import com.pillow.ui.screen.NoteSectionScreen
import com.pillow.ui.screen.SettingsScreen
import com.pillow.ui.screen.TrashScreen

sealed class PillowScreen(val route: String) {
    object Home : PillowScreen("home")
    object NoteEditor : PillowScreen("noteEditor/{noteId}") {
        fun createRoute(noteId: Long?) = if (noteId != null) "noteEditor/$noteId" else "noteEditor/0"
    }
    object Settings : PillowScreen("settings")
    object Trash : PillowScreen("trash")
    object Favorites : PillowScreen("favorites")
    object Pinned : PillowScreen("pinned")
    object Archive : PillowScreen("archive")
    object Buckets : PillowScreen("buckets")
    object BucketDetail : PillowScreen("buckets/{bucketId}") {
        fun createRoute(bucketId: Long) = "buckets/$bucketId"
    }
    object Backup : PillowScreen("backup")
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
                onFavoritesClick = {
                    navController.navigate(PillowScreen.Favorites.route)
                },
                onPinnedClick = {
                    navController.navigate(PillowScreen.Pinned.route)
                },
                onArchiveClick = {
                    navController.navigate(PillowScreen.Archive.route)
                },
                onBucketsClick = {
                    navController.navigate(PillowScreen.Buckets.route)
                },
                onSettingsClick = {
                    navController.navigate(PillowScreen.Settings.route)
                },
                onTrashClick = {
                    navController.navigate(PillowScreen.Trash.route)
                },
                onBackupClick = {
                    navController.navigate(PillowScreen.Backup.route)
                }
            )
        }

        composable(PillowScreen.NoteEditor.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: 0L
            NoteEditorScreen(
                noteId = noteId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(PillowScreen.Settings.route) {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(PillowScreen.Trash.route) {
            TrashScreen(onBackClick = { navController.popBackStack() })
        }

        composable(PillowScreen.Favorites.route) {
            NoteSectionScreen(
                section = NoteSection.FAVORITES,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(PillowScreen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(PillowScreen.Pinned.route) {
            NoteSectionScreen(
                section = NoteSection.PINNED,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(PillowScreen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(PillowScreen.Archive.route) {
            NoteSectionScreen(
                section = NoteSection.ARCHIVE,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(PillowScreen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(PillowScreen.Buckets.route) {
            BucketsScreen(
                onBackClick = { navController.popBackStack() },
                onBucketClick = { bucketId ->
                    navController.navigate(PillowScreen.BucketDetail.createRoute(bucketId))
                }
            )
        }

        composable(PillowScreen.BucketDetail.route) { backStackEntry ->
            val bucketId = backStackEntry.arguments?.getString("bucketId")?.toLongOrNull() ?: 0L
            BucketDetailScreen(
                bucketId = bucketId,
                onBackClick = { navController.popBackStack() },
                onNoteClick = { noteId ->
                    navController.navigate(PillowScreen.NoteEditor.createRoute(noteId))
                }
            )
        }

        composable(PillowScreen.Backup.route) {
            BackupScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
