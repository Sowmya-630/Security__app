package com.sowmya.security.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sowmya.security.*
import com.sowmya.security.viewmodel.ContactViewModel

@Composable
fun MainNavigation(navController: NavHostController, startDestination: String) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            AuthenticationScreen(navController, isSignup = false)
        }
        composable(Screen.Signup.route) {
            AuthenticationScreen(navController, isSignup = true)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Location.route) {
            LiveLocation(navController)
        }
        composable(Screen.Contacts.route) {
            ContactScreen(viewModel = ContactViewModel(context.applicationContext as android.app.Application))
        }
        composable(Screen.Camera.route) {
            CameraScreen()
        }
        composable(Screen.Sos.route) {
            SOSScreen()
        }
        composable(Screen.Stream.route) {
            MultiUserStreamScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Location : Screen("location")
    object Contacts : Screen("contacts")
    object Camera : Screen("camera")
    object Sos : Screen("sos")
    object Stream : Screen("stream")
}
