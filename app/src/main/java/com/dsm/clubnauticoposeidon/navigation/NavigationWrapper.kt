package com.dsm.clubnauticoposeidon.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeOperacionesScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeSocioScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.MapaMuellesScreen
import com.dsm.clubnauticoposeidon.ui.screens.initial.InitialScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.LoginScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.validarAcceso
import com.dsm.clubnauticoposeidon.ui.screens.postulante.SeguimientoScreen
import com.dsm.clubnauticoposeidon.ui.screens.profile.ProfileScreen
import com.dsm.clubnauticoposeidon.ui.screens.radas.AsignarRadaScreen
import com.dsm.clubnauticoposeidon.ui.screens.recovery.RecuperarPasswordScreen
import com.dsm.clubnauticoposeidon.ui.screens.signup.SignUpScreen
import com.dsm.clubnauticoposeidon.ui.theme.Gold500
import com.dsm.clubnauticoposeidon.ui.theme.Navy900
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    // Evalúa si existe una sesión activa
    val startDest = if (auth.currentUser != null) "router" else "initial"

    // Cierra sesión y vuelve al inicio
    val cerrarSesion: () -> Unit = {
        auth.signOut()
        navHostController.navigate("initial") { popUpTo(0) }
    }

    NavHost(navController = navHostController, startDestination = startDest) {

        //  ROUTER
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
                    onJefe = {
                        navHostController.navigate("jefe") {
                            popUpTo("router") { inclusive = true }
                        }
                    },
                    // Finanzas y mantenimiento
                    onFinanzas = {
                        auth.signOut()
                        navHostController.navigate("login") {
                            popUpTo("router") { inclusive = true }
                        }
                        Toast.makeText(context, "Panel de finanzas próximamente", Toast.LENGTH_LONG).show()
                    },
                    onMantenimiento = {
                        auth.signOut()
                        navHostController.navigate("login") {
                            popUpTo("router") { inclusive = true }
                        }
                        Toast.makeText(context, "Panel de mantenimiento próximamente", Toast.LENGTH_LONG).show()
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

        //  INICIO, LOGIN, REGISTRO
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
                onNavigateToJefe = {
                    navHostController.navigate("jefe") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                onNavigateToSeguimiento = {
                    navHostController.navigate("seguimiento") {
                        popUpTo("initial") { inclusive = true }
                    }
                }
                // onNavigateToFinanzas y onNavigateToMantenimiento
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // El router lee el rol y manda a cada persona a su panel
                    navHostController.navigate("router") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUp = { navHostController.navigate("signup") },
                onBackClick = { navHostController.popBackStack() },
                onForgotPassword = { navHostController.navigate("recuperar_password") }
            )
        }

        composable("signup") {
            SignUpScreen(
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

        //  SOCIO
        composable("home") {
            HomeSocioScreen(
                onNavigateToProfile = { navHostController.navigate("profile") },
                onNavigateToServicios = { navHostController.navigate("servicios") },
                onLogout = cerrarSesion
            )
        }
        composable("profile") {
            ProfileScreen(onBackClick = { navHostController.popBackStack() })
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

        //  NAVIERO
        composable("home_operaciones") {
            HomeOperacionesScreen(
                onLogout = cerrarSesion,
                onNavigateToAsignarRada = { navHostController.navigate("asignar_rada") },
                onNavigateToMapa = { navHostController.navigate("mapa_muelles") }   // modo gestión
            )
        }

        composable("asignar_rada") { backStackEntry ->
            val radaDelMapa: String =
                backStackEntry.savedStateHandle.get<String>("rada_seleccionada") ?: ""

            AsignarRadaScreen(
                radaSeleccionada = radaDelMapa,
                onAbrirMapaClick = { navHostController.navigate("elegir_rada") },   // modo elegir
                onBackClick = { navHostController.popBackStack() }
            )
        }

        // Modo gestión: desde el panel del naviero
        composable("mapa_muelles") {
            MapaMuellesScreen(
                modoSeleccion = false,
                onBackClick = { navHostController.popBackStack() }
            )
        }

        // Modo elegir: desde Nueva Asignación
        composable("elegir_rada") {
            MapaMuellesScreen(
                modoSeleccion = true,
                onRadaSelected = { radaId ->
                    navHostController.previousBackStackEntry?.savedStateHandle?.set(
                        "rada_seleccionada",
                        radaId
                    )
                    navHostController.popBackStack()
                },
                onBackClick = { navHostController.popBackStack() }
            )
        }

        //  POSTULANTE
        composable("seguimiento") {
            SeguimientoScreen(
                onNavigateToHomeSocio = {
                    navHostController.navigate("home") {
                        popUpTo("seguimiento") { inclusive = true }
                    }
                },
                onLogout = cerrarSesion
            )
        }

        //  SECRETARIA
        composable("secretaria") {
            com.dsm.clubnauticoposeidon.ui.screens.secretaria.HomeSecretariaScreen(
                onNavigateToSolicitudes = { navHostController.navigate("bandeja_solicitudes") },
                onLogout = cerrarSesion
            )
        }
        composable("bandeja_solicitudes") {
            com.dsm.clubnauticoposeidon.ui.screens.secretaria.BandejaSecretariaScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }

        //  JEFE
        composable("jefe") {
            com.dsm.clubnauticoposeidon.ui.screens.jefatura.HomeJefaturaScreen(
                onNavigateToAprobaciones = { navHostController.navigate("bandeja_aprobaciones") },
                onLogout = cerrarSesion
            )
        }
        composable("bandeja_aprobaciones") {
            com.dsm.clubnauticoposeidon.ui.screens.jefatura.BandejaAprobacionesScreen(
                onBackClick = { navHostController.popBackStack() }
            )
        }
    }
}