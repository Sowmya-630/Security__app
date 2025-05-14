package com.sowmya.security.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.sowmya.security.*
import com.sowmya.security.ui.HomeScreen
import com.sowmya.security.ui.LiveLocationMapScreen
import com.sowmya.security.ui.ProfileScreen
import com.sowmya.security.ui.SplashScreen
import com.sowmya.security.viewmodel.AuthViewModel
import com.sowmya.security.viewmodel.ContactViewModel
import com.sowmya.security.viewmodel.LocationViewModel

@Composable
fun MainNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val locationViewModel: LocationViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()
//    LaunchedEffect(Unit) {
//        val user = auth.currentUser
//        if (user != null) {
//            navController.navigate(Screen.Home.route) {
//                popUpTo("splash") { inclusive = true }
//            }
//        }else {
//                navController.navigate(Screen.Login.route) {
//                    popUpTo("splash") { inclusive = true }
//                }
//            }
//        }

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable("splash") {
            SplashScreen(navController) // Splash screen composable
        }
        composable(Screen.Login.route)   {
            val authViewModel: AuthViewModel = viewModel()
          AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { isAdmin ->
                   navController.navigate(Screen.Home.route)
                })
        }
        composable(Screen.Home.route) {
            HomeScreen(navController, locationViewModel)
        }
        composable(Screen.Location.route) {
            LiveLocationMapScreen(navController, locationViewModel)
        }
        composable(Screen.Contacts.route) {
            ContactScreen(viewModel = ContactViewModel(), locationViewModel,navController)
//            DualCameraScreen()
//            StreamScreen()
//            DualCameraStreamScreen()
//            SOSScreen()
        }
        composable(Screen.Camera.route) {
            CameraScreen()
        }
        composable(Screen.Sos.route) {
            SOSScreen()
        }
        composable(Screen.Stream.route) {
            StreamScreen()
        }
<<<<<<< HEAD
=======
        composable(Screen.Streamd.route) {
            StreamScreen0()
        }
>>>>>>> f0358ee (security app)
        composable(Screen.ProfileScreen.route){
            ProfileScreen(navController = navController)
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
<<<<<<< HEAD
=======
    object Streamd: Screen("stream_d")
>>>>>>> f0358ee (security app)
    object ProfileScreen : Screen("profile")
}

