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
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dsm.clubnauticoposeidon.ui.screens.login.validarAcceso
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    // Evalúa si existe una sesión activa
    val startDest = if (auth.currentUser != null) "router" else "initial"

    NavHost(navController = navHostController, startDestination = startDest) {
        composable("router") {
            val context = LocalContext.current
            
            LaunchedEffect(Unit) {
                validarAcceso(
                    auth = auth,
                    onSocio = {
                        navHostController.navigate("home") {
                            popUpTo("router") { inclusive = true }
                        }
                    },
                    onAdmin = {
                        navHostController.navigate("home_operaciones") {
                            popUpTo("router") { inclusive = true }
                        }
                    },
                    onSecretaria = {
                        navHostController.navigate("secretaria") {
                            popUpTo("router") { inclusive = true }
                        }
                    },
                    onSeguimiento = {
                        navHostController.navigate("seguimiento") {
                            popUpTo("router") { inclusive = true }
                        }
                    },
                    onError = { msg ->
                        auth.signOut()
                        navHostController.navigate("login") {
                            popUpTo("router") { inclusive = true }
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                )
            }

            Scaffold(containerColor = Navy900) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold500)
                }
            }
        }

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
                        popUpTo(0)
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
            com.dsm.clubnauticoposeidon.ui.screens.secretaria.HomeSecretariaScreen(
                onNavigateToSolicitudes = {
                    navHostController.navigate("bandeja_solicitudes")
                },
                onLogout = {
                    auth.signOut()
                    navHostController.navigate("login") { popUpTo(0) }
                }
            )
        }
        
        composable("bandeja_solicitudes") {
            com.dsm.clubnauticoposeidon.ui.screens.admin.SecretariaScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
    }
}