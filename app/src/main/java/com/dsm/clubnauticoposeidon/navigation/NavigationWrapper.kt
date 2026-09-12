package com.dsm.clubnauticoposeidon.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.clubnauticoposeidon.ui.screens.initial.InitialScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.LoginScreen
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
                onLogin = { navHostController.navigate("logIn") },
                onSignUp = { navHostController.navigate("signUp") }
            )
        }

        composable("logIn") {
            LoginScreen(
                auth = auth,
                onSignUp = { navHostController.navigate("signUp") },
                onLoginSuccess = { navHostController.navigate("home") }
            )
        }

        composable("signUp") {
            SignUpScreen(
                auth = auth,
                onLogin = { navHostController.navigate("logIn") }
            )
        }

        composable("home") {
            HomeScreen(
                auth = auth,
                onLogout = { 
                    navHostController.navigate("logIn") {
                        popUpTo(0) // Limpiar la pila de navegación al salir
                    } 
                }
            )
        }
    }
}