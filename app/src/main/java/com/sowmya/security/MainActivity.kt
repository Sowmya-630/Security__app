package com.sowmya.security

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import androidx.annotation.RequiresPermission
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sowmya.security.navigation.MainNavigation

class MainActivity : FragmentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private val CALL_PERMISSION_REQUEST_CODE = 102
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: FirebaseDatabase
    private lateinit var locationRef: DatabaseReference
    private var biometricLoopManager: BiometricLoopManager? = null
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricHelper = BiometricHelper(
            activity = this,
            onSuccess = {
                setContent {
                    val navController = rememberNavController()
                    var permissionsGranted by remember { mutableStateOf(false) }


                    RequestAllPermissions(
                        requiredPermissions = requiredPermissions,
                        onResult = { granted ->
                            permissionsGranted = granted
                            if (!granted) {
                                Toast.makeText(
                                    this,
                                    "Please grant all permissions to use the app fully.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "All permissions granted.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )

                    MainNavigation(navController = navController)

                    database = FirebaseDatabase.getInstance()
                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    locationRef = database.getReference("locations")

                    checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOCATION_PERMISSION_REQUEST_CODE)
                    checkPermission(Manifest.permission.CALL_PHONE, CALL_PERMISSION_REQUEST_CODE)

//                    StartBiometricLoopButton()
                }
            },
            onFailure = {
                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        )

        biometricHelper.authenticate()
    }

    override fun onResume() {
        super.onResume()
        AppVisibilityTracker.isInForeground = true
        AppVisibilityTracker.currentActivity = this
    }

    override fun onPause() {
        super.onPause()
        AppVisibilityTracker.isInForeground = false
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                getLocationUpdates()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    val location = it.lastLocation
                    if (location != null) {
                        updateLocationInFirebase(location.latitude, location.longitude)
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    private fun updateLocationInFirebase(lat: Double, lng: Double) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val locationData = mapOf("lat" to lat, "lng" to lng)
            locationRef.child(userId).setValue(locationData)
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                LOCATION_PERMISSION_REQUEST_CODE -> getLocationUpdates()
                CALL_PERMISSION_REQUEST_CODE -> Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

//    @Composable
//    fun StartBiometricLoopButton() {
//        var isLoopStarted by remember { mutableStateOf(false) }
//        var sec=biometricLoopManager?.elapsedSeconds
//
//        Button(onClick = {
//            if (!isLoopStarted) {
//                isLoopStarted = true
//
//                startLoop()
//            } else {
//                isLoopStarted = false
//                biometricLoopManager?.stopLoop()
//                Log.d("BiometricLoop", "Loop Stopped")
//            }
//        }){
//            Text(text = if (isLoopStarted) "Stop loop  $sec" else "Start Loop ")
//        }
//        Spacer(modifier = Modifier.height(16.dp))
////        Text(text = "Time Elapsed: $sec")
//    }
//
//    private fun startLoop() {
////        val activity = getCurrentActivity()
//        val activity = AppVisibilityTracker.currentActivity as? FragmentActivity
//
//        if (activity == null) {
//            Log.e("BiometricLoop", "Failed to get current activity!")
//            return
//        }
//
//        if (biometricLoopManager == null) {
//            Log.d("BiometricLoop", "Loop Initialized")
//        } else {
//            Log.d("BiometricLoop", "Loop Already Initialized")
//        }
//
//        biometricLoopManager = BiometricLoopManager(
//            activity,
//            onTrigger =  {
//                Log.d("BiometricLoop", "Biometric trigger executed")
////                Toast.makeText(activity, "Biometric trigger executed", Toast.LENGTH_SHORT).show()
//            }
////                Log.d("BiometricLoop", "Biometric trigger executed")
//
//                    // Start SOS, or navigate, or show alert, etc.
//        /// Provide onTrigger logic here
//        )
//        biometricLoopManager?.startLoop()
//        Log.d("BiometricLoop", "Loop Started")
//        Toast.makeText(activity, "Loop Started", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun getCurrentActivity(): FragmentActivity? {
//        return AppVisibilityTracker.currentActivity as? FragmentActivity
//    }

    @Composable
    fun RequestAllPermissions(
        requiredPermissions: Array<String>,
        onResult: (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            onResult(allGranted)
        }

        LaunchedEffect(Unit) {
            val allPermissionsGranted = requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!allPermissionsGranted) {
                launcher.launch(requiredPermissions)
            } else {
                onResult(true)
            }
        }
    }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppVisibilityTracker)
    }
}

