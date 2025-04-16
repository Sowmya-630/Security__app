package com.sowmya.security.ui

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Close
import android.provider.ContactsContract
import android.telephony.SmsManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import com.sowmya.security.data.ContactEntity
import com.sowmya.security.viewmodel.ContactViewModel
import android.content.pm.PackageManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sowmya.security.BiometricLoopManager
import com.sowmya.security.R
import com.sowmya.security.hasCallPermission
import com.sowmya.security.makeEmergencyCall
import com.sowmya.security.navigation.Screen
import com.sowmya.security.ui.theam.GlowingCurvedLines
import com.sowmya.security.ui.theam.MoonGlowBackground
import com.sowmya.security.ui.theam.MovingGlowingLinesBackground
import com.sowmya.security.ui.theam.NeonTheme
import com.sowmya.security.ui.theam.darkGlowColor
import com.sowmya.security.viewmodel.LocationViewModel
import com.sowmya.security.viewmodel.ProfileViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    locationViewModel: LocationViewModel
) {
    var viewModel :ProfileViewModel = viewModel()
    val userProfile = viewModel.userProfile
//    MovingGlowingLinesBackground()

    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val userLocation by remember { mutableStateOf(locationViewModel.userLocation) }
    LocationCheckHandler()
//    val swirlSpeed = animateFloatAsState(targetValue = 1f, animationSpec = androidx.compose.animation.core.tween(4000))

        Box(modifier = Modifier.fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00B4D8),
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
        ) {
            // 🔵 Background Image
//        AnimatedBackground()
//        MovingGlowingLinesBackground()
//            BlackHoleEffect(swirlSpeed.value)
//            AnimatedAbstractBackground()
                // Background Image
            GlowingCurvedLines()

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Hi ${userProfile?.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
//                            Text("Complete profile", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
//                            Text("Alan Street...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (userLocation == null)
                                        Text(
                                            "location fetching.....",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp
                                        )
                                    userLocation?.let {
                                        Text(
                                            " ${it.latitude}, ${it.longitude}",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor =  Color(0xFF2E2E2E),// Color(0xFF00B4D8), // Neon cyan from the glowing line
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        actions = {

                            IconButton(onClick = {
                                navController.navigate(Screen.ProfileScreen.route)

                            }) {
                                Image(
//                            painter = rememberAsyncImagePainter("https://example.com/your_dp.jpg"),
                                    painter = painterResource(id = R.drawable.profile),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            }

                        }
                    )
                },
                floatingActionButton = {
                    val backgroundColor = Color(0xFF1E88E5) // Bright/Deep Blue

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable { navController.navigate(Screen.Location.route) }
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF1565C0).copy(alpha = 0.7f), // darker blue
                                spotColor = Color(0xFF90CAF9).copy(alpha = 0.2f)     // lighter blue highlight
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location on",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                containerColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
//                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Title Text
//                    Text(
//                        text = "Are you in emergency?",
//                        style = MaterialTheme.typography.headlineLarge
//                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    GlowingPulsingText("Are you in emergency?")
                    Spacer(modifier = Modifier.height(50.dp))
//                    GlowingPulsingText("Click me")

                    // Spacer
                    Spacer(modifier = Modifier.height(30.dp))

                    // Row with SOS Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,

                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        AnimatedImageButton(
                            navController = navController,
                            imageRes = R.drawable.sos,
                            label = "",
                            route = Screen.Sos.route,
                            onClick = {
                                // Handle SOS action here, like sending messages
                                navController.navigate(Screen.Contacts.route)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))

                    Spacer(modifier = Modifier.height(16.dp))

// Emergency Buttons Section
                    val emergencyContacts = listOf(
                        "Call Police 🚓" to "100",
                        "Call Ambulance 🚑" to "102",
                        "Call Fire 🚒" to "101",
                        "Call Child Helpline 📞" to "1098",
                        "Call Women Helpline 👩" to "1091"
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(emergencyContacts) { emergencyContact->
                            NeumorphicEmergencyButton(label = emergencyContact.first, number = emergencyContact.second)
                        }

                    }
//
                }
            }
        }

}


@Composable
fun AnimatedImageButton(
    navController: NavController,
    imageRes: Int,
    label: String,
    route: String,
    onClick: () -> Unit
) {
    // Animate scale for image "forward" pulse
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.45f,
        targetValue = 1.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Button(
            onClick = {
                onClick()
            },
            modifier = Modifier.size(200.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentScale = ContentScale.Crop,
                contentDescription = label,
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .offset(y = 7.dp)
                    .offset(x=-1.2.dp)
                    .clip(RoundedCornerShape(90.dp))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}


@Composable
fun GlowingPulsingText(text:String) {
    val infiniteTransition = rememberInfiniteTransition()

    // Pulsing scale animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glowing alpha animation
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        )
    )

    Text(
        text = text,
        color = Color.White, // White text
        style = MaterialTheme.typography.headlineLarge.copy(
            shadow = Shadow(
                color = Color.Red.copy(alpha = glowAlpha), // White glow
                offset = Offset(0f, 0f),
                blurRadius = 20f
            )
        ),
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    )
}

@Composable
fun NeumorphicEmergencyButton(
    label: String,
    number: String
) {
    val context = LocalContext.current
    var pendingNumber by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && pendingNumber != null) {
            makeEmergencyCall(context, pendingNumber!!)
        } else {
            Toast.makeText(context, "Permission Denied! Unable to call.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp)
            .background(
                color = Color(0xFF1E88E5) , // dark grey background
                shape = RoundedCornerShape(40.dp)
            )
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(40.dp),
                ambientColor = Color.White.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.6f)
            )
            .clickable {
                if (hasCallPermission(context)) {
                    makeEmergencyCall(context, number)
                } else {
                    pendingNumber = number
                    permissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

fun requestCallPermission(context: Context) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        != PackageManager.PERMISSION_GRANTED
    ) {
        val CALL_PERMISSION_REQUEST_CODE = 1002
        // Request permission if not granted
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.CALL_PHONE),
            CALL_PERMISSION_REQUEST_CODE
        )
    } else {
        // Don't make the call automatically, just allow button press to make the call
        Toast.makeText(context, "Permission granted! Please press the button to call.", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun SplashScreen(navController: NavController) {
    // Add a delay (2-3 seconds) before navigating to the Home screen

    // Splash screen UI with a CircularProgressIndicator
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

