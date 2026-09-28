package com.dsm.clubnauticoposeidon.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeOperacionesScreen
import com.dsm.clubnauticoposeidon.ui.screens.initial.InitialScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.LoginScreen
import com.dsm.clubnauticoposeidon.ui.screens.recovery.RecuperarPasswordScreen
import com.dsm.clubnauticoposeidon.ui.screens.signup.SignUpScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeSocioScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.MapaMuellesScreen
import com.dsm.clubnauticoposeidon.ui.screens.profile.ProfileScreen
import com.dsm.clubnauticoposeidon.ui.screens.radas.AsignarRadaScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    // Evalúa si existe una sesión activa
    val startDest = if (auth.currentUser != null) "home" else "initial"

    NavHost(navController = navHostController, startDestination = startDest) {
        composable("initial") {
            InitialScreen(
                onLogin = { navHostController.navigate("login") },
                onSignUp = { navHostController.navigate("signup") }
            )
        }

        composable("login") {
            LoginScreen(
                auth = auth,
                onSignUp = { navHostController.navigate("signup") },
                onNavigateToSocio = { navHostController.navigate("home") },
                onNavigateToAdmin = { navHostController.navigate("home_operaciones") },
                onBackClick = { navHostController.popBackStack() },
                onForgotPassword = { navHostController.navigate("recuperar_password") }
            )
        }

        composable("signup") {
            SignUpScreen(
                auth = auth,
                onLogin = { navHostController.navigate("login") },
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable("recuperar_password") {
            RecuperarPasswordScreen(
                viewModel = viewModel(),
                onVolver = { navHostController.popBackStack() }
            )
        }

        composable("home") {
            HomeSocioScreen(
                onNavigateToProfile = {
                    // Navega a la pantalla del código QR
                    navHostController.navigate("profile")
                },
                onLogout = {
                    // Cierra sesión en Firebase y regresa al inicio
                    auth.signOut()
                    navHostController.navigate("initial") {
                        popUpTo(0) // Borra el historial para que no pueda volver con la flecha
                    }
                }
            )
        }
        composable(route = "profile") {
            ProfileScreen(
                onBackClick = {
                    navHostController.popBackStack()
                }
            )
        }
        composable("home_operaciones") {
            HomeOperacionesScreen(
                onLogout = {
                    auth.signOut()
                    navHostController.navigate("initial") {
                        popUpTo(0)
                    }
                },
                onNavigateToAsignarRada = { navHostController.navigate("asignar_rada") },
                onNavigateToMapa = { navHostController.navigate("mapa_muelles") }
            )
        }

        composable("asignar_rada") { backStackEntry ->
            // 1. Leemos el "buzón" para ver si el mapa nos dejó alguna rada seleccionada
            val radaDelMapa = backStackEntry.savedStateHandle.get("rada_seleccionada") ?: ""

            AsignarRadaScreen(
                radaSeleccionada = radaDelMapa, // 2. Le pasamos el dato a la pantalla
                onAbrirMapaClick = { navHostController.navigate("mapa_muelles") },
                onBackClick = { navHostController.popBackStack() }
            )
        }

        // Ruta del Mapa interactivo
        composable("mapa_muelles") {
            MapaMuellesScreen(
                onRadaSelected = { nombreRada ->
                    // 3. Cuando el usuario toca una rada verde, guardamos el nombre en el "buzón" de la pantalla anterior
                    navHostController.previousBackStackEntry?.savedStateHandle?.set("rada_seleccionada", nombreRada)
                    // 4. Regresamos al formulario automáticamente
                    navHostController.popBackStack()
                },
                onBackClick = { navHostController.popBackStack() }
            )
        }
    }
}