package com.sowmya.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.library.rtmp.RtmpCamera1
import com.pedro.common.ConnectChecker

// --- RTMP Camera Helper Class ---
class RtmpCameraHelper(private val context: Context, private val surfaceView: SurfaceView) {
    private val connectChecker = ConnectCheckerRtmpImpl(context)
    private val rtmpCamera = RtmpCamera1(surfaceView, connectChecker)

    fun startStream(rtmpUrl: String) {
        if (!rtmpCamera.isStreaming) {
            val audioReady = rtmpCamera.prepareAudio()
            val videoReady = rtmpCamera.prepareVideo()

            Log.d("RTMP", "Audio Ready: $audioReady, Video Ready: $videoReady")

            if (audioReady && videoReady) {
                rtmpCamera.startStream(rtmpUrl)
            } else {
                Toast.makeText(context, "Failed to prepare camera or audio", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun stopStream() {
        if (rtmpCamera.isStreaming) {
            rtmpCamera.stopStream()
        }
    }

    fun isStreaming(): Boolean = rtmpCamera.isStreaming

    fun startPreview() {
        if (!rtmpCamera.isOnPreview) {
            rtmpCamera.startPreview()
        }
    }

    fun stopPreview() {
        if (rtmpCamera.isOnPreview) {
            rtmpCamera.stopPreview()
        }
    }

    fun getCameraView(): SurfaceView = surfaceView
}

// --- ConnectChecker Implementation ---
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

// --- Main Composable for Multi-User RTMP Streaming ---
@Composable
fun MultiUserStreamScreen() {
    val context = LocalContext.current
    val surfaceView = remember { SurfaceView(context) }

    // Sample users and their unique stream URLs
    val userStreamMap = mapOf(
        "sowmya" to "rtmp://16.170.228.168/live/sowmyaStream",
        "rajesh" to "rtmp://16.170.228.168/live/rajeshStream"
    )

    var selectedUser by remember { mutableStateOf("sowmya") }
    val rtmpUrl = userStreamMap[selectedUser] ?: ""

    var cameraHelper: RtmpCameraHelper? by remember { mutableStateOf(null) }

    // Request camera and mic permissions
    LaunchedEffect(Unit) {
        requestPermissions(context)
    }

    // Start preview when surface is ready
    LaunchedEffect(surfaceView) {
        cameraHelper = RtmpCameraHelper(context, surfaceView)
        cameraHelper?.startPreview()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { surfaceView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            userStreamMap.keys.forEach { user ->
                Button(onClick = {
                    selectedUser = user
                    Toast.makeText(context, "Switched to $user", Toast.LENGTH_SHORT).show()
                }) {
                    Text(user)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                cameraHelper?.startStream(rtmpUrl)
            }) {
                Text("Start ${selectedUser}'s Stream")
            }

            Button(onClick = {
                cameraHelper?.stopStream()
            }) {
                Text("Stop Stream")
            }
        }
    }
}

// --- Permission Helper ---
fun requestPermissions(context: Context) {
    if (context is ComponentActivity) {
        val requiredPermissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(context, notGranted.toTypedArray(), 1001)
        }
    }
}
