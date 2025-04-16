package com.sowmya.security

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.sowmya.security.data.ContactEntity
import com.sowmya.security.navigation.Screen
import com.sowmya.security.ui.startLocationUpdates
import com.sowmya.security.viewmodel.ContactViewModel
import com.sowmya.security.viewmodel.LocationViewModel
import com.sowmya.security.viewmodel.StreamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

private val SMS_PERMISSION_REQUEST_CODE = 1001

fun normalizeNumber(number: String): String {
    return number.replace(Regex("[^+0-9]"), "")
}

private fun requestSmsPermission(context: Context) {
    ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(Manifest.permission.SEND_SMS),
        SMS_PERMISSION_REQUEST_CODE
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactScreen(
    viewModel: ContactViewModel,
    locationViewModel: LocationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val contacts by viewModel.contacts.collectAsState()
    val currentLocation = locationViewModel.userLocation
    var locationCallback: LocationCallback? = null
    val streamViewModel: StreamViewModel = viewModel()

    @Suppress("DEPRECATION")
    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId()
        SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
    } else {
        SmsManager.getDefault()
    }

    val isDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName

    if (!isDefaultSmsApp) {
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Default SMS app set", Toast.LENGTH_SHORT).show()
    }

    var smsPermissionGranted by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var cameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
    var isStreaming by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Exception?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val permissionState = rememberPermissionState(permission = Manifest.permission.SEND_SMS)
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: "unknown"

    val live = "http://16.170.228.168/stream.html?stream=$currentUserId"
    val livelocation = "http://16.170.228.168/live-location.html?id=$currentUserId"
    val streamUrl = "rtmp://16.170.228.168/live/$currentUserId"
    val message = "🚨 SOS Alert! I need help!"
    val extraInfo = "$message \n Live Streaming: $live\n Live Location: $livelocation"

    val SENT = "SMS_SENT"
    val DELIVERED = "SMS_DELIVERED"

    // Register BroadcastReceivers
    DisposableEffect(Unit) {
        val sentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(context, "✅ SMS Sent", Toast.LENGTH_SHORT).show()
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(context, "❌ Generic failure", Toast.LENGTH_SHORT).show()
                    SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(context, "❌ No service", Toast.LENGTH_SHORT).show()
                    SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(context, "❌ Null PDU", Toast.LENGTH_SHORT).show()
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(context, "❌ Radio off", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val deliveredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(context, "📬 SMS Delivered", Toast.LENGTH_SHORT).show()
                    Activity.RESULT_CANCELED -> Toast.makeText(context, "❌ SMS Not Delivered", Toast.LENGTH_SHORT).show()
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            sentReceiver,
            IntentFilter(SENT),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        ContextCompat.registerReceiver(
            context,
            deliveredReceiver,
            IntentFilter(DELIVERED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(sentReceiver)
            context.unregisterReceiver(deliveredReceiver)
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            cameraHelper?.stopPreview()
            cameraHelper?.stopStream()
        }
    }

    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> locationPermissionGranted = isGranted }

    LaunchedEffect(Unit) {
        locationPermissionGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!smsPermissionGranted) {
            permissionState.launchPermissionRequest()
        }

        if (!locationPermissionGranted) {
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        cameraHelper?.startPreview()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestSmsPermission(context)
        }
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            startLocationUpdates(context, fusedLocationClient) { latLng ->
                locationViewModel.updateLocation(latLng)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isStreaming) {
            Text("🔴 LIVE", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        Button(onClick = {
            cameraHelper?.startStream(streamUrl)
            isStreaming = true
            streamViewModel.setFrontStreaming(true)
        }) {
            Text("Live Streaming")
        }


        Button(onClick = {
            val sentIntent = PendingIntent.getBroadcast(context, 0, Intent(SENT), PendingIntent.FLAG_IMMUTABLE)
            val deliveredIntent = PendingIntent.getBroadcast(context, 0, Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE)


            coroutineScope.launch {
                contacts.forEachIndexed { index, contact ->
                    try {
//                        val cleanNumber = normalizeNumber(contact.phone)
                        val parts = smsManager.divideMessage(extraInfo)

// Create lists of PendingIntent with the same size as parts
                        val sentIntents = List(parts.size) {
                            PendingIntent.getBroadcast(context, 0, Intent(SENT), PendingIntent.FLAG_IMMUTABLE)
                        }
                        val deliveredIntents = List(parts.size) {
                            PendingIntent.getBroadcast(context, 0, Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE)
                        }
// Send multipart SMS
                        smsManager.sendMultipartTextMessage(contact.phone, null, parts,
                            sentIntents as ArrayList<PendingIntent?>?, deliveredIntents as ArrayList<PendingIntent?>?
                        )

//                        Toast.makeText(context, "Sending to ${contact.phone}", Toast.LENGTH_SHORT).show()
//                        smsManager.sendTextMessage(contact.phone, null, extraInfo, sentIntent, deliveredIntent)
                        delay(1000L) // throttle
//                        smsManager.sendTextMessage(contact.phone, null, message, sentIntent, deliveredIntent)
//                        delay(1000L) // throttle

                    } catch (e: Exception) {
                        error = e
                        Toast.makeText(context, "Failed to send to ${contact.phone}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Send SOS")
        }

        Button(onClick = {
            cameraHelper?.stopStream()
            isStreaming = false
            streamViewModel.setFrontStreaming(false)
        }) {
            Text("Stop Stream")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate(Screen.Stream.route)
        }) {
            Text("Preview")
        }

//        if (error != null) {
//            Text("Error: ${error?.message}")
//        }
//
//        if (currentLocation != null) {
//            Text("Current Location: $currentLocation")
//        } else {
//            Text("Location not yet available.")
//        }
    }
}
