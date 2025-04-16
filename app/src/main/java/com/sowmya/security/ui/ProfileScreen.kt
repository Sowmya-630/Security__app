package com.sowmya.security.ui
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.text.TextPainter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sowmya.security.data.ContactEntity
import com.sowmya.security.viewmodel.ContactViewModel
import com.sowmya.security.viewmodel.ProfileViewModel
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.sowmya.security.navigation.Screen
import com.sowmya.security.ui.theam.GlowingCurvedLines
import com.sowmya.security.ui.theam.NeumorphicButton
import com.sowmya.security.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel0: ProfileViewModel = viewModel(),
    profileImageUri: Uri? = null,
    navController: NavController
) {
    val context = LocalContext.current
    val userProfile = viewModel0.userProfile
    val viewModel: ContactViewModel = viewModel()
    val contactListState = viewModel.contacts.collectAsState()
    val contactList = contactListState.value
    val authviewModel: AuthViewModel = viewModel()
    val auth = FirebaseAuth.getInstance()

    val requestReadContactsPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
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
                            val name =
                                it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            val number =
                                it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            viewModel.addContact(ContactEntity(name = name, phone = number))
                        }
                    }
                }
            }
        }

    var selectedImageUri by remember { mutableStateOf(profileImageUri) }
    var showDialog by remember { mutableStateOf(false) }
    var showNameEditDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
//    var navController = rememberNavController()
    var editedName by remember { mutableStateOf("Your Name") }
    val currentUserId = auth.currentUser?.uid ?: "unknown"

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            viewModel0.uploadProfilePicture(
                uri = it,
                context = context,
                onSuccess = { downloadUrl ->
                    Toast.makeText(context, "Profile image updated!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(
                        context,
                        "Failed to upload: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    // Load image from Firebase if available
    LaunchedEffect(Unit) {
        viewModel0.loadUserProfileImage { firebaseImageUri ->
            selectedImageUri = firebaseImageUri
        }
    }
    Box(modifier = Modifier.fillMaxSize()
        .background(
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF121212),
                    Color.Black
                )
            )
        )
    )
    {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Quick Actions") },
                            onClick = {
                                showDialog = true
                                showMenu = false
                            })
                        DropdownMenuItem(
                            text = { Text("logout") },
                            onClick = {
                            authviewModel.logout()
                                navController.navigate(Screen.Login.route)
                            })
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.2f))
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        }
                ) {
                    selectedImageUri?.let {
                        AsyncImage(
                            model = it,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile Icon",
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.clickable { showNameEditDialog = true }) {
                    Text(
                        text = editedName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White

                    )
                    Text(
                        text = userProfile?.email ?: "Add Email",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            Text("Live Stream : http://16.170.228.168/stream.html?stream=${currentUserId} ",)
            Spacer(modifier = Modifier.height(24.dp))
            Text("http://16.170.228.168/live-location.html?id=${currentUserId}")

            Spacer(modifier = Modifier.height(24.dp))
            NeumorphicButton(text = "Add Emergency Contacts") {
                requestReadContactsPermission.launch(android.Manifest.permission.READ_CONTACTS)
                val intent = Intent(
                    Intent.ACTION_PICK,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                )
                contactPickerLauncher.launch(intent)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Emergency Contacts",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (contactList.isEmpty()) {
                Text("No contacts added.", color = Color.Gray)
            } else {
                LazyColumn {
                    items(contactList) { contact ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = Color(0xFF1C1C1E),
                                    spotColor = Color(0xFF000000)
                                )
                                .background(Color(0xFF2A2D34), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D34)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(contact.name, fontWeight = FontWeight.Bold)
                                    Text(contact.phone, color = Color.Gray)
                                }

                                IconButton(onClick = {
                                    viewModel.deleteContact(contact)
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Contact",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
    if (showNameEditDialog) {
        AlertDialog(
            onDismissRequest = { showNameEditDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNameEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
