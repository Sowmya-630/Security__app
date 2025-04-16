package com.sowmya.security

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.sowmya.security.navigation.MainNavigation
import com.sowmya.security.navigation.Screen

import com.sowmya.security.viewmodel.ContactViewModel


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route
        val biometricHelper = BiometricHelper(
            activity = this,
            onSuccess = {
                setContent {
                    val navController = rememberNavController()
                    MainNavigation(navController = navController, startDestination = startDestination)
                    // ✅ Pass ViewModel to MyApp
                    //MainNavigation()

                }

            },
            onFailure =
            {
                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()

            }
        )

        biometricHelper.authenticate()
    }


}
@Composable
fun LiveLocation(navController: NavController) {
    val context = LocalContext.current
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val requestLocationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            locationPermissionGranted = isGranted
            if (isGranted) {
                navController.navigate("home") // Navigate to ContactScreen
            } else {
                (context as? ComponentActivity)?.finish() // Close the app if permission is denied
            }
        }
    LaunchedEffect(Unit) {
        requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (!locationPermissionGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Shows a loading indicator while waiting for permission
        }
    }
}
