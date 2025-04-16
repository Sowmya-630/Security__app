package com.sowmya.security.ui

import android.app.Activity
import android.content.Intent
import android.location.Location
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
import androidx.core.content.ContextCompat
import com.sowmya.security.ImageButton
import com.sowmya.security.R
import com.sowmya.security.navigation.Screen



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Hi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Complete profile", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Alan Street...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Safe location", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open Camera") },
                                onClick = {
                                    navController.navigate("camera")
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Title Text
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Safety First!", style = MaterialTheme.typography.headlineLarge)

                // Spacer
                Spacer(modifier = Modifier.height(20.dp))

                // Row with SOS Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ImageButton(navController, R.drawable.ic_sos, "SOS", Screen.Sos.route)
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Row with Police & Emergency Contacts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ImageButton(navController, R.drawable.ic_police, "Police", Screen.Stream.route)
                    ImageButton(navController, R.drawable.ic_contacts, "Emergency Contacts", Screen.Contacts.route)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Row with Helpline & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ImageButton(navController, R.drawable.ic_helpline, "Helpline", Screen.Camera.route)
                    ImageButton(navController, R.drawable.ic_location, "Location", Screen.Location.route)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Row for Live Stream Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ImageButton(navController, R.drawable.ic_stream, "Live Stream", Screen.Stream.route)
                }
            }
        }
    )
}
