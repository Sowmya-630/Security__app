package com.sowmya.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleService
import java.util.concurrent.Executor

class BiometricHelper(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onFailure: () -> Unit
) {
    fun authenticate() {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> showPrompt()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                val intent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators)
                }
                activity.startActivity(intent)
            }
            else -> onFailure()
        }
    }

    private fun showPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(activity)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use Face, Fingerprint, or Device PIN")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }

                override fun onAuthenticationFailed() {
                    onFailure()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}
class BiometricLoopManager(private val context: Context) {  // Change FragmentActivity to Context
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var failureCount = 0

    private val biometricHelper = BiometricHelper(
        context as FragmentActivity,
        onSuccess = {
            failureCount = 0
            handler.postDelayed({ startLoop() }, 10 * 1000) // Retry in 10 seconds
        },
        onFailure = {
            failureCount++
            if (failureCount < 3) {
                handler.postDelayed({ startLoop() }, 5 * 1000) // Retry in 5 seconds
            } else {
                // Notify after 3 failed attempts
                showAuthenticationNotification(context)
            }
        }
    )

    fun startLoop() {
            isRunning = true
            biometricHelper.authenticate()
    }

    fun stopLoop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun showAuthenticationNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val notificationChannelId = "authentication_channel"

        // Create a notification channel (required for Android 8+)
        val channel = NotificationChannel(
            notificationChannelId,
            "Authentication Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val intent = null
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Create the notification
        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle("Authentication Required")
            .setContentText("Tap to authenticate with biometrics")
            .setSmallIcon(android.R.drawable.ic_lock_power_off)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}


class BiometricForegroundService : LifecycleService() {

    private lateinit var biometricLoopManager: BiometricLoopManager

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        biometricLoopManager = BiometricLoopManager(this)  // Pass Context, not FragmentActivity
        biometricLoopManager.startLoop()
    }

    private fun startForegroundService() {
        val channelId = "biometric_monitor"
        val channelName = "Biometric Monitor Service"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Biometric Monitoring Active")
            .setContentText("Monitoring your presence every minute.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1001, notification)
    }

    private fun sendSOS() {
        // Display a Toast message instead of starting SosActivity
        Toast.makeText(this, "SOS Triggered", Toast.LENGTH_SHORT).show()
    }


    override fun onDestroy() {
        super.onDestroy()
        biometricLoopManager.stopLoop()
    }
}
