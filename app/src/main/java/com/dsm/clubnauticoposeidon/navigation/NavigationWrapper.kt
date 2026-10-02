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
import com.dsm.clubnauticoposeidon.ui.screens.postulante.SeguimientoScreen // IMPORT AGREGADO AQUÍ
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
                onSignUp = { navHostController.navigate("signup") },
                onNavigateToSocio = {
                    navHostController.navigate("home") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navHostController.navigate("home_operaciones") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                onNavigateToSecretaria = {
                    navHostController.navigate("secretaria") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                onNavigateToSeguimiento = {
                    navHostController.navigate("seguimiento") {
                        popUpTo("initial") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                auth = auth,
                onSignUp = { navHostController.navigate("signup") },
                onNavigateToSocio = { navHostController.navigate("home") },
                onNavigateToAdmin = { navHostController.navigate("home_operaciones") },
                onNavigateToSecretaria = { navHostController.navigate("secretaria") },
                onBackClick = { navHostController.popBackStack() },
                onNavigateToSeguimiento = { navHostController.navigate("seguimiento") }, // CORREGIDO AQUÍ
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
                onNavigateToServicios = {
                    navHostController.navigate("servicios")
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
        composable("servicios") {
            com.dsm.clubnauticoposeidon.ui.screens.Socios.ServiciosScreen(
                onBackClick = { navHostController.popBackStack() },
                onNavigateToCatering = { navHostController.navigate("catering") },
                onNavigateToLimpieza = { navHostController.navigate("limpieza") },
                onNavigateToSuministros = { navHostController.navigate("suministros") },
                onNavigateToAsistencia = { navHostController.navigate("asistencia") }
            )
        }
        composable("catering") {
            com.dsm.clubnauticoposeidon.ui.screens.Socios.CateringScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable("limpieza") {
            com.dsm.clubnauticoposeidon.ui.screens.Socios.LimpiezaScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable("suministros") {
            com.dsm.clubnauticoposeidon.ui.screens.Socios.SuministrosScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable("asistencia") {
            com.dsm.clubnauticoposeidon.ui.screens.Socios.AsistenciaScreen(
                onBackClick = { navHostController.popBackStack() }
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
            val radaDelMapa = backStackEntry.savedStateHandle.get("rada_seleccionada") ?: ""

            AsignarRadaScreen(
                radaSeleccionada = radaDelMapa,
                onAbrirMapaClick = { navHostController.navigate("mapa_muelles") },
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable("mapa_muelles") {
            MapaMuellesScreen(
                onRadaSelected = { nombreRada ->
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "rada_seleccionada",
                        nombreRada
                    )
                    navHostController.popBackStack()
                },
                onBackClick = { navHostController.popBackStack() }
            )
        }

        composable("seguimiento") {
            SeguimientoScreen(
                auth = auth,
                onSignOut = {
                    navHostController.navigate("login") { // CORREGIDO AQUÍ
                        popUpTo(0)
                    }
                }
            )
        }
        composable("secretaria") {
            com.dsm.clubnauticoposeidon.ui.screens.admin.SecretariaScreen(
                onBackClick = {
                    auth.signOut()
                    navHostController.navigate("login") { popUpTo(0) }
                }
            )
        }
    }
}