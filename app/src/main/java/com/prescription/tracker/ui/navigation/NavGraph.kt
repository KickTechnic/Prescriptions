package com.prescription.tracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prescription.tracker.ui.edit.EditMedicationScreen
import com.prescription.tracker.ui.list.MedicationListScreen
import com.prescription.tracker.ui.settings.SettingsScreen

object Routes {
    const val LIST = "list"
    const val ADD = "add"
    const val EDIT = "edit/{medicationId}"
    const val SETTINGS = "settings"

    fun editRoute(id: Long) = "edit/$id"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startMedicationId: Long? = null
) {
    val startDestination = if (startMedicationId != null && startMedicationId > 0) {
        Routes.editRoute(startMedicationId)
    } else {
        Routes.LIST
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LIST) {
            MedicationListScreen(
                onAddClick = { navController.navigate(Routes.ADD) },
                onMedicationClick = { id -> navController.navigate(Routes.editRoute(id)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ADD) {
            EditMedicationScreen(
                medicationId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("medicationId")
            EditMedicationScreen(
                medicationId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
