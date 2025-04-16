package com.sowmya.security

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.VideoCapture
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@Composable
fun CameraScreen() {
    val context = LocalContext.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraPermission = remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraPermission.value = it
        if (!it) Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }
    LaunchedEffect(Unit) {

        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Camera Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Capture Image
        Button(onClick = { capturePhoto(context) { uri -> imageUri = uri } }) {
            Text("Take Picture")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Captured Image",
                modifier = Modifier.size(200.dp)
            )
        }
    }
}

// Function to capture a photo
fun capturePhoto(context: Context, onPhotoCaptured: (Uri) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val imageCapture = ImageCapture.Builder().build()


        val outputDir = context.cacheDir
        val photoFile = File(outputDir, "photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile)
                    uploadToFirebase(uri, "images/${photoFile.name}") { downloadUrl ->
                        onPhotoCaptured(downloadUrl)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
        cameraProvider.bindToLifecycle(context as ComponentActivity, cameraSelector, imageCapture)
    }, Executors.newSingleThreadExecutor())
}

// Function to record a video
// Function to upload to Firebase Storage
fun uploadToFirebase(fileUri: Uri, path: String, onSuccess: (Uri) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference.child(path)
    storageRef.putFile(fileUri)
        .addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl)
            }
        }
        .addOnFailureListener { exception ->
            Log.e("Firebase", "Upload failed: ${exception.message}", exception)
        }
}