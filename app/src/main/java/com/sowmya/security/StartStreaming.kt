package com.sowmya.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.sowmya.security.data.ContactEntity
import com.sowmya.security.viewmodel.ContactViewModel



//
//@Composable
//fun ContactScreen(
//    viewModel: ContactViewModel,
//    locationViewModel: LocationViewModel
//) {
//    val context = LocalContext.current
//    val activity = context as? ComponentActivity
//    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
//
//    val contacts by viewModel.contacts.collectAsState()
//    val currentLocation = locationViewModel.userLocation
//    val surfaceView = remember { SurfaceView(context) }
//
//    var cameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
//    var isStreaming by remember { mutableStateOf(false) }
//
//    // Permissions
//    var smsPermissionGranted by remember { mutableStateOf(false) }
//    var locationPermissionGranted by remember { mutableStateOf(false) }
//
//    val requestSendSMSPermission = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted -> smsPermissionGranted = isGranted }
//
//    val requestLocationPermission = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted -> locationPermissionGranted = isGranted }
//
//    LaunchedEffect(Unit) {
//        smsPermissionGranted = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.SEND_SMS
//        ) == PackageManager.PERMISSION_GRANTED
//
//        locationPermissionGranted = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//
//        if (!smsPermissionGranted) requestSendSMSPermission.launch(Manifest.permission.SEND_SMS)
//        if (!locationPermissionGranted) requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//
//        requestPermissions(context)
//        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//    }
//
//    LaunchedEffect(surfaceView) {
//        val helper = RtmpCameraHelper(context, surfaceView)
//        helper.startPreview()
//        cameraHelper = helper
//    }
//
//    LaunchedEffect(locationPermissionGranted) {
//        if (locationPermissionGranted) {
//            startLocationUpdates(context, fusedLocationClient) { latLng ->
//                locationViewModel.updateLocation(latLng)
//            }
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            cameraHelper?.stopStream()
//            cameraHelper?.stopPreview()
//        }
//    }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        AndroidView(
//            factory = { surfaceView },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(240.dp)
//        )
//
//        if (isStreaming) {
//            Text(
//                text = "🔴 LIVE",
//                color = Color.Red,
//                fontWeight = FontWeight.Bold,
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(
//            onClick = {
//                val smsManager = SmsManager.getDefault()
//                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
//                val live = "http://16.170.228.168/stream.html?stream=$userId"
//                val livelocation = "http://16.170.228.168/live-location.html?id=$userId"
//                val streamUrl = "rtmp://16.170.228.168/live/$userId"
//
//                val basicMsg = "🚨 SOS Alert! I need help!"
//                val linkMsg = "Live Streaming: $live\nLive Location: $livelocation"
//
//                try {
//                    contacts.forEach {
//                        smsManager.sendTextMessage(it.phone, null, basicMsg, null, null)
//                        smsManager.sendTextMessage(it.phone, null, linkMsg, null, null)
//                    }
//
//                    if (currentLocation != null) {
//                        val locationMsg =
//                            "$basicMsg\nLocation: https://maps.google.com/?q=${currentLocation.latitude},${currentLocation.longitude}\n$live"
//                        contacts.forEach {
//                            smsManager.sendTextMessage(it.phone, null, locationMsg, null, null)
//                        }
//                        Toast.makeText(context, "SOS sent with location.", Toast.LENGTH_SHORT).show()
//                    } else {
//                        Toast.makeText(context, "Location not available. Sent basic SOS.", Toast.LENGTH_SHORT).show()
//                    }
//
//                    // Start streaming
//                    cameraHelper?.startStream(streamUrl)
//                    isStreaming = true
//
//                } catch (e: Exception) {
//                    Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
//                    val fallback = contacts.firstOrNull()
//                    fallback?.let {
//                        val uri = Uri.parse("smsto:${it.phone}")
//                        val intent = Intent(Intent.ACTION_SENDTO, uri)
//                        intent.putExtra("sms_body", basicMsg)
//                        context.startActivity(intent)
//                    }
//                }
//            },
//            enabled = contacts.isNotEmpty()
//        ) {
//            Text("Send SOS Message")
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Button(onClick = {
//            cameraHelper?.stopStream()
//            isStreaming = false
//        }) {
//            Text("Stop Streaming")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        if (currentLocation != null) {
//            Text("Current Location: $currentLocation")
//        } else {
//            Text("Location not yet available.")
//        }
//    }
//}
