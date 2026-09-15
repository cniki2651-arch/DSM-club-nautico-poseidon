package com.dsm.clubnauticoposeidon.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.clubnauticoposeidon.ui.screens.initial.InitialScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.LoginScreen
import com.dsm.clubnauticoposeidon.ui.screens.recovery.RecuperarPasswordScreen
import com.dsm.clubnauticoposeidon.ui.screens.signup.SignUpScreen
import com.dsm.clubnauticoposeidon.ui.screens.home.HomeScreen
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
                onLoginSuccess = { navHostController.navigate("home") },
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
            HomeScreen(
                auth = auth,
                onLogout = {
                    navHostController.navigate("login") {
                        popUpTo(0) // Limpiar la pila de navegación al salir
                    }
                }
            )
        }
    }
}