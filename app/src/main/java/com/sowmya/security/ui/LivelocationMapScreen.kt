package com.sowmya.security.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.sowmya.security.viewmodel.LocationViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LiveLocationMapScreen(navController: NavController, locationViewModel: LocationViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
//
    // Start location updates when permission is granted
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            startLocationUpdates(context, fusedLocationClient) { latLng ->
                locationViewModel.updateLocation(latLng)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)
            }
        } else {
            permissionState.launchPermissionRequest()
        }
    }

    // UI
    if (permissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true)
            ) {
                locationViewModel.userLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "You"
                    )
                }
            }

            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

// Handles location updates every 3 seconds
internal fun startLocationUpdates(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
): LocationCallback? {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) return null

    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        3000
    ).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                onLocationReceived(LatLng(it.latitude, it.longitude))
                Log.d("LocationUpdates", "Location: ${it.latitude}, ${it.longitude}")
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )

    return locationCallback
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationCheckHandler() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
        else {
            if (!isLocationEnabled(context)) {
                Toast.makeText(context, "Please enable location to continue", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }
    }
}
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
