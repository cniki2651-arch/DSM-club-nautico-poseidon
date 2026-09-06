package com.dsm.clubnauticoposeidon.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.dsm.clubnauticoposeidon.ui.screens.initial.InitialScreen
import com.dsm.clubnauticoposeidon.ui.screens.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationWrapper(
    navHostController: NavHostController,
    auth: FirebaseAuth
) {
    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(
                onLogin = { navHostController.navigate("logIn") },
                onSignUp = { navHostController.navigate("signUp") }
            )
        }

        composable("logIn") {
            LoginScreen(
                auth = auth,
                onSignUp = { navHostController.navigate("signUp") }
            )
        }

        composable("signUp") {
            // SignUpScreen(auth, onLogin = { navHostController.navigate("logIn") })
        }

        composable("home") {
            // HomeScreen()
        }
    }
}