package com.sowmya.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
<<<<<<< HEAD
import androidx.lifecycle.viewmodel.compose.viewModel
=======
>>>>>>> f0358ee (security app)
import com.google.firebase.auth.FirebaseAuth
import com.pedro.library.rtmp.RtmpCamera2

import com.pedro.common.ConnectChecker
import com.pedro.encoder.input.video.CameraHelper

import com.pedro.library.view.OpenGlView
<<<<<<< HEAD
import com.sowmya.security.viewmodel.StreamViewModel
=======
>>>>>>> f0358ee (security app)

class RtmpCameraHelper(
    private val context: Context,
    private val openGlView: OpenGlView,
    private val cameraFacing: CameraHelper.Facing
) {
    private val connectChecker = ConnectCheckerRtmpImpl(context)
    private val rtmpCamera = RtmpCamera2(openGlView, connectChecker)

    fun startStream(rtmpUrl: String) {
        if (!rtmpCamera.isStreaming) {
            val audioReady = rtmpCamera.prepareAudio()
            val videoReady = rtmpCamera.prepareVideo()

            if (audioReady && videoReady) {
                rtmpCamera.startStream(rtmpUrl)
            } else {
                Toast.makeText(context, "Failed to prepare audio/video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stopStream() {
        if (rtmpCamera.isStreaming) rtmpCamera.stopStream()
    }

    fun isStreaming(): Boolean = rtmpCamera.isStreaming

    fun startPreview() {
        if (!rtmpCamera.isOnPreview) {
            rtmpCamera.startPreview(cameraFacing)
        }
    }

    fun stopPreview() {
        if (rtmpCamera.isOnPreview) {
            rtmpCamera.stopPreview()
        }
    }

    fun getCameraView(): OpenGlView = openGlView
<<<<<<< HEAD
=======
    fun release() {
        stopStream()
        stopPreview()
//        rtmpCamera2.clearFilters()
    }
>>>>>>> f0358ee (security app)
}

class ConnectCheckerRtmpImpl(private val context: Context) : ConnectChecker {
    override fun onAuthError() {
        Toast.makeText(context, "Auth error", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthSuccess() {
        Toast.makeText(context, "Auth success", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(reason: String) {
        Toast.makeText(context, "Connection failed: $reason", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionStarted(url: String) {
        Toast.makeText(context, "Connecting to $url", Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionSuccess() {
        Toast.makeText(context, "Connected successfully", Toast.LENGTH_SHORT).show()
    }

    override fun onDisconnect() {
        Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun StreamScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: "test"
    val rtmpUrl = "rtmp://16.170.228.168/live/$currentUserId"
<<<<<<< HEAD
    val streamViewModel: StreamViewModel = viewModel()
    val frontOpenGlView = remember { OpenGlView(context) }
    val backOpenGlView = remember { OpenGlView(context) }
    var frontCameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
    var backCameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }

    val activity = context as? ComponentActivity

=======

    val frontOpenGlView = remember { OpenGlView(context) }
    val backOpenGlView = remember { OpenGlView(context) }
    var isFrontCameraActive by remember { mutableStateOf(true) }
    var frontCameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
    var backCameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }

    val activity = context as? ComponentActivity

>>>>>>> f0358ee (security app)
    LaunchedEffect(Unit) {
        requestPermissions(context)
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        frontCameraHelper = RtmpCameraHelper(context, frontOpenGlView, CameraHelper.Facing.FRONT)
        frontCameraHelper?.startPreview()
        kotlinx.coroutines.delay(1000)
<<<<<<< HEAD
        frontCameraHelper?.startStream(rtmpUrl + "_front")
=======
        frontCameraHelper?.startStream(rtmpUrl)
>>>>>>> f0358ee (security app)
//
//        backCameraHelper = RtmpCameraHelper(context, backOpenGlView, CameraHelper.Facing.BACK)
//        backCameraHelper?.startPreview()
//        kotlinx.coroutines.delay(1000)
//        backCameraHelper?.startStream(rtmpUrl + "_back")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { frontOpenGlView },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
            AndroidView(
                factory = { backOpenGlView },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
<<<<<<< HEAD
                frontCameraHelper?.stopPreview()
                backCameraHelper = RtmpCameraHelper(context, backOpenGlView, CameraHelper.Facing.BACK)
                backCameraHelper?.startPreview()
//        backCameraHelper?.startStream(rtmpUrl + "_back")
            }) {
                Text("toggle")
            }
            Button(onClick = {
                frontCameraHelper?.startStream(rtmpUrl + "_front")
                backCameraHelper?.startStream(rtmpUrl + "_back")
                streamViewModel.setFrontStreaming(true)
=======
                 if (isFrontCameraActive) {
                     frontCameraHelper?.stopPreview()
                     backCameraHelper?.startPreview()
                     backCameraHelper?.startStream(rtmpUrl)
                } else {
                     backCameraHelper?.stopPreview()
                     frontCameraHelper?.startPreview()
                     frontCameraHelper?.startStream(rtmpUrl)
                }
                isFrontCameraActive = !isFrontCameraActive
            }) {
                Text("toggle")
            }
//            Button(onClick = {
//                frontCameraHelper?.stopPreview()
//                backCameraHelper = RtmpCameraHelper(context, backOpenGlView, CameraHelper.Facing.BACK)
//                backCameraHelper?.startPreview()
////        backCameraHelper?.startStream(rtmpUrl + "_back")
//            }) {
//                Text("start back camera")
//            }
            Button(onClick = {
                frontCameraHelper?.startStream(rtmpUrl)
                backCameraHelper?.startStream(rtmpUrl )
>>>>>>> f0358ee (security app)
            }) {
                Text("Start Stream")
            }

            Button(onClick = {
                frontCameraHelper?.stopStream()
                backCameraHelper?.stopStream()
<<<<<<< HEAD
                streamViewModel.setFrontStreaming(true)
=======
>>>>>>> f0358ee (security app)
            }) {
                Text("Stop Stream")
            }
        }
    }
}

fun requestPermissions(context: Context) {
    if (context is ComponentActivity) {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(context, notGranted.toTypedArray(), 1010)
        }
    }
}
