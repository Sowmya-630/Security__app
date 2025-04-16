package com.sowmya.security

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
import com.sowmya.security.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    var showMenu by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {Row(
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
                }},
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
            )
            {


                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Safety First!", style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                )
                {
                    ImageButton(navController, R.drawable.ic_sos, "SOS Button", Screen.Stream.route)
                }
                // Image Buttons Row
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    ImageButton(
                        navController,
                        R.drawable.ic_police,
                        "Police Helpline",
                        Screen.Stream.route
                    )
                    ImageButton(
                        navController,
                        R.drawable.ic_contacts,
                        "Emergency Contacts",
                        Screen.Stream.route
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ImageButton(
                        navController,
                        R.drawable.ic_helpline,
                        "Helpline",
                        Screen.Stream.route
                    )
                }
            }
        }
    )
}

@Composable
fun ImageButton(navController: NavController, imageRes: Int, label: String, route: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Button(
            onClick = {
                when (label) {
                    "Emergency Contacts" -> navController.navigate("contacts")
                    "SOS Button" -> navController.navigate("Sos")
                }
            },
            modifier = Modifier.size(80.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Image(painterResource(id = imageRes), contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label)
    }
}
@Composable
fun ContactScreen(viewModel: ContactViewModel) {
    val context = LocalContext.current
    val activity = context as Activity
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(activity) }


    val contacts by viewModel.contacts.collectAsState()

    val requestReadContactsPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val requestSendSMSPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val requestLocationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    val contactPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val contactUri: Uri? = result.data?.data
                contactUri?.let {
                    val cursor = context.contentResolver.query(
                        it,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null,
                        null,
                        null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            viewModel.addContact(ContactEntity(name = name, phoneNumber = number))
                        }
                    }
                }
            }
        }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            requestReadContactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        }) {
            Text("Add Contact")
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(
                    contact = contact,
                    onRemove = { viewModel.deleteContact(contact) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                return@Button
            }

            requestSendSMSPermission.launch(android.Manifest.permission.SEND_SMS)
            requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

            val smsManager = SmsManager.getDefault()
            var message = "I need help!"
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    message =
                        "🚨 SOS Alert! I need help!\nLocation: https://maps.google.com/?q=$latitude,$longitude"

                }
            }
            contacts.forEach {
                smsManager.sendTextMessage(it.phoneNumber, null, message, null, null)
            }
            Toast.makeText(context, "SOS message sent to all contacts", Toast.LENGTH_SHORT).show()
        }, enabled = contacts.isNotEmpty()) {
            Text("Send SOS Message")
        }
    }
}

@Composable
fun ContactItem(contact: ContactEntity, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("${contact.name} - ${contact.phoneNumber}", modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Contact")
        }
    }
}