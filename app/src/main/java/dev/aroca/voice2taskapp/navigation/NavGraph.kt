package dev.aroca.voice2taskapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.aroca.voice2taskapp.data.model.Tarea
import dev.aroca.voice2taskapp.ui.screens.GrabarAudioScreen
import dev.aroca.voice2taskapp.ui.screens.HomeScreen
import dev.aroca.voice2taskapp.ui.screens.ListaDetailScreen
import dev.aroca.voice2taskapp.ui.screens.LoginScreen
import dev.aroca.voice2taskapp.ui.screens.RegisterScreen
import dev.aroca.voice2taskapp.ui.screens.SettingsScreen
import dev.aroca.voice2taskapp.ui.screens.SplashScreen
import dev.aroca.voice2taskapp.ui.screens.TareaDetailScreen
import dev.aroca.voice2taskapp.viewmodel.AuthState
import dev.aroca.voice2taskapp.viewmodel.AuthViewModel
import dev.aroca.voice2taskapp.viewmodel.TareasViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object ListaDetail : Screen("lista/{listaId}/{listaNombre}") {
        fun createRoute(listaId: Int, listaNombre: String) = "lista/$listaId/$listaNombre"
    }
    object Settings : Screen("settings")
    object GrabarAudio : Screen("grabar/{listaId}/{listaNombre}") {
        fun createRoute(listaId: Int, listaNombre: String) = "grabar/$listaId/$listaNombre"
    }
    object TareaDetail : Screen("tarea/{tareaId}") {
        fun createRoute(tareaId: Int) = "tarea/$tareaId"
    }
}

@Composable
fun NavGraph(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val authState by authViewModel.state.collectAsState()
    var tareaSeleccionada by remember { mutableStateOf<Tarea?>(null) }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    val destination = if (authState is AuthState.Success) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLista = { id, nombre ->
                    navController.navigate(Screen.ListaDetail.createRoute(id, nombre))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToTareaDetalle = { tarea ->
                    tareaSeleccionada = tarea
                    navController.navigate(Screen.TareaDetail.createRoute(tarea.id))
                }
            )
        }

        composable(
            route = Screen.ListaDetail.route,
            arguments = listOf(
                navArgument("listaId") { type = NavType.IntType },
                navArgument("listaNombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val listaId = backStackEntry.arguments?.getInt("listaId") ?: 0
            val listaNombre = backStackEntry.arguments?.getString("listaNombre") ?: ""
            ListaDetailScreen(
                listaId = listaId,
                listaNombre = listaNombre,
                onBack = { navController.popBackStack() },
                onGrabar = { navController.navigate(Screen.GrabarAudio.createRoute(listaId, listaNombre)) },
                onVerDetalle = { tarea ->
                    tareaSeleccionada = tarea
                    navController.navigate(Screen.TareaDetail.createRoute(tarea.id))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.GrabarAudio.route,
            arguments = listOf(
                navArgument("listaId") { type = NavType.IntType },
                navArgument("listaNombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            GrabarAudioScreen(
                listaId = backStackEntry.arguments?.getInt("listaId") ?: 0,
                listaNombre = backStackEntry.arguments?.getString("listaNombre") ?: "",
                onDismiss = { navController.popBackStack() },
                onTareaCreada = { navController.popBackStack() },
                onIrAjustes = {
                    navController.popBackStack()
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.TareaDetail.route,
            arguments = listOf(navArgument("tareaId") { type = NavType.IntType })
        ) {
            val tarea = tareaSeleccionada
            if (tarea != null) {
                val viewModel: TareasViewModel = viewModel()
                TareaDetailScreen(
                    tarea = tarea,
                    onBack = { navController.popBackStack() },
                    onCompletar = {
                        viewModel.completarTarea(tarea.id, tarea.lista_id)
                        navController.popBackStack()
                    },
                    onEliminar = {
                        viewModel.eliminarTarea(tarea.id, tarea.lista_id)
                        navController.popBackStack()
                    }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}