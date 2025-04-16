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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.common.ConnectChecker
import com.pedro.library.rtmp.RtmpCamera1


class CameraStreamHelper(
    private val context: Context,
    private val openGlView: OpenGlView,
    private val cameraFacing: CameraHelper.Facing
) {
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
        }
    }

    fun stopPreview() {
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
    }
}


@Composable
fun StreamScreen0() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
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
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
            }
        }
    }
}


