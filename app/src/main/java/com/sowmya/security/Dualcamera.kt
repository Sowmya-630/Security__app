package com.sowmya.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.library.view.OpenGlView
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
import com.sowmya.security.viewmodel.CameraViewModel
import kotlinx.coroutines.delay

class CameraStreamHelper(
    private val context: Context,
    private val openGlView: OpenGlView,
    private val cameraFacing: CameraHelper.Facing
) {
    private val rtmpCamera2 = RtmpCamera2(openGlView, object : ConnectChecker {

        override fun onConnectionStarted(url: String) {
            Toast.makeText(context, "Connecting to RTMP server...", Toast.LENGTH_SHORT).show()
            Log.d("CameraStreamHelper", "Connection started: $url")
        }

        override fun onConnectionSuccess() {
            Toast.makeText(context, "RTMP Connection Successful", Toast.LENGTH_SHORT).show()
            Log.d("CameraStreamHelper", "RTMP connection successful")
        }

        override fun onConnectionFailed(reason: String) {
            Toast.makeText(context, "RTMP Connection Failed: $reason", Toast.LENGTH_LONG).show()
            Log.e("CameraStreamHelper", "Connection failed: $reason")
//            rtmpCamera2.stopStream() // auto stop on failure
        }

        override fun onDisconnect() {
            Toast.makeText(context, "RTMP Disconnected", Toast.LENGTH_SHORT).show()
            Log.d("CameraStreamHelper", "Disconnected from RTMP")
        }

        override fun onAuthError() {
            Toast.makeText(context, "RTMP Auth Error", Toast.LENGTH_SHORT).show()
            Log.e("CameraStreamHelper", "Authentication error")
        }

        override fun onAuthSuccess() {
            Toast.makeText(context, "RTMP Auth Success", Toast.LENGTH_SHORT).show()
            Log.d("CameraStreamHelper", "Authentication successful")
        }
    })

    fun startPreview() {
        if (!rtmpCamera2.isStreaming && !rtmpCamera2.isRecording) {
            rtmpCamera2.startPreview(
                if (cameraFacing == CameraHelper.Facing.FRONT) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK
            )
        }
    }

    fun stopPreview() {
        rtmpCamera2.stopPreview()
    }

    fun startStream(rtmpUrl: String) {
        if (!rtmpCamera2.isStreaming) {
            rtmpCamera2.startStream(rtmpUrl)
        }
    }

    fun stopStream() {
        if (rtmpCamera2.isStreaming) {
            rtmpCamera2.stopStream()
        }
    }

    fun isStreaming(): Boolean = rtmpCamera2.isStreaming

    fun release() {
        stopStream()
        stopPreview()
//        rtmpCamera2.clearFilters()
    }
}



@Composable
fun StreamScreen0() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val  cameraViewModel: CameraViewModel = viewModel()


    var isFrontCamera  by remember { mutableStateOf(false) }
    val openGlView = remember { OpenGlView(context) }

    var cameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
    var isStreaming = false

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: "test"
    val rtmpUrl = "rtmp://16.170.228.168/live/$currentUserId"


    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        requestPermissions(context)
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    LaunchedEffect(isFrontCamera) {
        requestPermissions(context)
        cameraHelper?.stopPreview()
        delay(1000)
        cameraHelper?.stopStream()
       delay(1000)

        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        cameraHelper?.release()
        cameraHelper = RtmpCameraHelper(
            context,
            openGlView,
            if (isFrontCamera) CameraHelper.Facing.FRONT else
                CameraHelper.Facing.BACK

        )
        cameraHelper?.startPreview()
        delay(1000)

            cameraHelper?.startStream(rtmpUrl)

    }

    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
        AndroidView(
            factory = { openGlView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 1000) {
                                isFrontCamera = !isFrontCamera
                                if (isFrontCamera) {
                                    cameraViewModel.setBackStreaming(true)
                                } else {

                                    cameraViewModel.setFrontStreaming(true)
                                }
                            }
                            lastTapTime = now
                        }
                    )
                }
        )

    }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {


            Button(onClick = {
                    cameraHelper?.startPreview()
                    cameraHelper?.startStream(rtmpUrl)
                    isStreaming = true

            }) {
                Text("Start Stream")
            }
            Button(onClick = {
                cameraHelper?.stopPreview()
                cameraHelper?.stopStream()
                cameraHelper = null
            }) {
                Text("Stop Stream")
            }
        }
    }
}




