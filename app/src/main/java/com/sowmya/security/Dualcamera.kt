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
<<<<<<< HEAD
=======
import androidx.compose.foundation.gestures.detectTapGestures
>>>>>>> f0358ee (security app)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
=======
import androidx.compose.ui.input.pointer.pointerInput
>>>>>>> f0358ee (security app)
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
<<<<<<< HEAD
=======
import androidx.lifecycle.viewmodel.compose.viewModel
>>>>>>> f0358ee (security app)
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1
<<<<<<< HEAD

=======
import com.sowmya.security.viewmodel.CameraViewModel
>>>>>>> f0358ee (security app)

class CameraStreamHelper(
    private val context: Context,
    private val openGlView: OpenGlView,
    private val cameraFacing: CameraHelper.Facing
) {
<<<<<<< HEAD
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler
    private var backgroundThread: HandlerThread

    init {
        backgroundThread = HandlerThread("CameraBackgroundThread").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    fun startPreview() {
        val cameraId = getCameraId(cameraFacing)
        if (cameraId != null) {
            openGlView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    openCamera(cameraId)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    // Handle surface size changes here if necessary
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    stopPreview()
                }
            })
        } else {
            Toast.makeText(context, "Camera ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCameraId(facing: CameraHelper.Facing): String? {
        return cameraManager.cameraIdList.find { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            lensFacing == if (facing == CameraHelper.Facing.FRONT) CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK
        }
    }

    private fun openCamera(cameraId: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                    Toast.makeText(context, "Camera disconnected", Toast.LENGTH_SHORT).show()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                    Toast.makeText(context, "Camera error: $error", Toast.LENGTH_SHORT).show()
                }
            }, backgroundHandler)
        } else {
            Toast.makeText(context, "Camera permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPreviewSession() {
        try {
            val previewSurface = openGlView.holder.surface
            val camera = cameraDevice ?: return

            // Create a capture request for preview
            val requestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            }

            // Create capture session
            camera.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        session.setRepeatingRequest(requestBuilder.build(), null, backgroundHandler)
                        Toast.makeText(context, "Preview started", Toast.LENGTH_SHORT).show()
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Toast.makeText(context, "Failed to configure camera preview session", Toast.LENGTH_SHORT).show()
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Toast.makeText(context, "Error setting up preview: ${e.message}", Toast.LENGTH_LONG).show()
=======
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
>>>>>>> f0358ee (security app)
        }
    }

    fun stopPreview() {
<<<<<<< HEAD
        try {
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            Toast.makeText(context, "Preview stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error stopping preview: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun release() {
        stopPreview()
        backgroundThread.quitSafely()
=======
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
>>>>>>> f0358ee (security app)
    }
}


<<<<<<< HEAD
=======

>>>>>>> f0358ee (security app)
@Composable
fun StreamScreen0() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
<<<<<<< HEAD
    val frontOpenGlView = remember { OpenGlView(context) }
    val backOpenGlView = remember { OpenGlView(context) }

    var isFrontCamera by remember { mutableStateOf(true) }
    var cameraHelper by remember {
        mutableStateOf<CameraStreamHelper?>(null)
    }

    // Automatically start preview in LaunchedEffect
    LaunchedEffect(Unit) {
        requestPermissions(context)
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize and start preview once the surface is ready
        cameraHelper = CameraStreamHelper(
            context,
            if (isFrontCamera) frontOpenGlView else backOpenGlView,
            if (isFrontCamera) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK
        )
        cameraHelper?.startPreview()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                if (isFrontCamera) frontOpenGlView else backOpenGlView
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
=======
    val  cameraViewModel: CameraViewModel = viewModel()


    var isFrontCamera  =true
    val openGlView = remember { OpenGlView(context) }

    var cameraHelper by remember { mutableStateOf<RtmpCameraHelper?>(null) }
    var isStreaming = false

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: "test"
    val rtmpUrl = "rtmp://16.170.228.168/live/$currentUserId"


    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(isFrontCamera) {
        requestPermissions(context)
        cameraHelper?.stopStream()
        cameraHelper?.stopPreview()
        activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


//        cameraHelper?.release()
        cameraHelper = RtmpCameraHelper(
            context,
            openGlView,
            if (isFrontCamera) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK
        )
        cameraHelper?.startPreview()
        cameraHelper?.startStream(rtmpUrl)

    }

    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { openGlView },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 400) {
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
>>>>>>> f0358ee (security app)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
<<<<<<< HEAD
            Button(onClick = {
                cameraHelper?.stopPreview()

                isFrontCamera = !isFrontCamera

                cameraHelper = CameraStreamHelper(
                    context,
                    if (isFrontCamera) frontOpenGlView else backOpenGlView,
                    if (isFrontCamera) CameraHelper.Facing.FRONT else CameraHelper.Facing.BACK
                )
                cameraHelper?.startPreview()
            }) {
                Text("Toggle Camera")
            }

            Button(onClick = {
                cameraHelper?.startPreview()
            }) {
                Text("Start Preview")
            }

            Button(onClick = {
                cameraHelper?.stopPreview()
            }) {
                Text("Stop Preview")
=======


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
                Text("Stop Camera")
>>>>>>> f0358ee (security app)
            }
        }
    }
}


<<<<<<< HEAD
=======


>>>>>>> f0358ee (security app)
