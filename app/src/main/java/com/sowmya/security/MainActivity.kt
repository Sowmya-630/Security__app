package com.sowmya.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
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
import com.sowmya.security.viewmodel.StreamViewModel

class MainActivity : FragmentActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private val CALL_PERMISSION_REQUEST_CODE = 102
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var database: FirebaseDatabase
    private lateinit var locationRef: DatabaseReference
    private lateinit var biometricLoopManager: BiometricLoopManager
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
                    val streamViewModel: StreamViewModel = viewModel()

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
                            }
                            else{
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
                    isLive(streamViewModel)
                }
            },
            onFailure = {
                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        )

        biometricHelper.authenticate()
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
    @Composable
    fun isLive(viewModel: StreamViewModel) {
        val isFrontLive by viewModel.isFrontStreaming
        val isBackLive by viewModel.isBackStreaming

        Column(Modifier.padding(16.dp)) {
//            Text(text = "Front Camera Streaming: ${if (isFrontLive) "Yes" else "No"}")
//            Text(text = "Back Camera Streaming: ${if (isBackLive) "Yes" else "No"}")
            Text(text = "Overall Live: ${if (viewModel.isLive) "Yes" else "No"}")
        }
    }


    @Composable
    fun StartBiometricLoopButton() {
        var isLoopStarted by remember { mutableStateOf(false) }

        Button(onClick = {
            if (!isLoopStarted) {
                isLoopStarted = true
                startLoop()
            } else {
                isLoopStarted = false
                biometricLoopManager.stopLoop()
            }
        }) {
            Text(text = if (isLoopStarted) "Stop loop" else "Start Loop")
        }
    }

    private fun startLoop() {
        biometricLoopManager = BiometricLoopManager(this)
        biometricLoopManager.startLoop()
    }

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


