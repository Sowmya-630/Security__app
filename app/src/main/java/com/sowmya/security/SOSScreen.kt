package com.sowmya.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SOSScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // Emergency Help Text
        Text(
            text = "Emergency help Needed?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(70.dp))

        // Emergency Options Grid
        EmergencyHelplineScreen()
    }
}

// Emergency Contacts Grid
@Composable
fun EmergencyOptions() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            EmergencyItem("Police", "100", R.drawable.ic_police)
            EmergencyItem("Helpline", "1091", R.drawable.ic_helpline)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            EmergencyItem("FirstAid", "108", R.drawable.ic_nearby)


        }
    }
}
@Composable
fun EmergencyHelplineScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmergencyCallButton(phoneNumber = "100", label = "Call Police 🚔")
        EmergencyCallButton(phoneNumber = "108", label = "Call Ambulance 🚑")
        EmergencyCallButton(phoneNumber = "101", label = "Call Fire 🚒")
        EmergencyCallButton(phoneNumber = "1098", label = "Call Child Helpline 📞")
        EmergencyCallButton(phoneNumber = "181", label = "Call Women Helpline 👩‍🦰")
    }
}


// Individual Emergency Item
@Composable
fun EmergencyItem(title: String, subtitle: String, icon: Int) {
    Column(
        modifier = Modifier
            .size(120.dp)
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(32.dp),
            tint = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun EmergencyCallButton(phoneNumber: String, label: String) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            makeEmergencyCall(context, phoneNumber)
        } else {
            Toast.makeText(context, "Permission Denied! Unable to call.", Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        onClick = {
            if (hasCallPermission(context)) {
                makeEmergencyCall(context, phoneNumber)
            } else {
                permissionLauncher.launch(android.Manifest.permission.CALL_PHONE)
            }
        },
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text(label)
    }
}

fun hasCallPermission(context: Context): Boolean {
    return context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

fun makeEmergencyCall(context: Context, phoneNumber: String) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:$phoneNumber")
    context.startActivity(callIntent)
}
