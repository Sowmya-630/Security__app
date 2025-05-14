package com.sowmya.security

import android.Manifest
import android.app.*
import android.content.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.speech.*
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.biometric.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import java.util.concurrent.Executor

// --- Biometric Helper ---
class BiometricHelper(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onFailure: () -> Unit
) {
    fun authenticate() {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

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
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
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

// --- Biometric Monitor Loop ---
class BiometricLoopManager(
    private val activity: FragmentActivity,
    private val onTrigger: () -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    private var elapsedSeconds = 0
    val elapsedTimeLiveData = MutableLiveData<Int>() // To observe elapsed time
    var failurecount=0
//    val context=LocalContext.current

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            elapsedSeconds++
            elapsedTimeLiveData.postValue(elapsedSeconds) // Post updated value
            if (elapsedSeconds >= 60 ) {
                sendSOS()
            }
//            Toast.makeText(context, "Monitoring $elapsedSeconds", Toast.LENGTH_SHORT).show()
            Log.e("BiometricLoop", "Monitoring $elapsedSeconds")
            handler.postDelayed(this, 1000)
        }
    }

    private val biometricHelper = BiometricHelper(
        activity,
        onSuccess = {
            elapsedSeconds = 0
            elapsedTimeLiveData.postValue(elapsedSeconds) // Reset and post value
            handler.post(timerRunnable)
            handler.postDelayed({ startLoop() }, 10 * 1000)
        },
        onFailure = {
            if (!AppVisibilityTracker.isInForeground) {
                showAuthenticationNotification(activity)
            }
            failurecount++
            if(failurecount>=3){
                sendSOS()
            }
            handler.postDelayed({ startLoop() }, 5000)
        }
    )

    fun startLoop() {
        if (!isRunning) {
            isRunning = true
        }
        if (AppVisibilityTracker.isInForeground) {
            biometricHelper.authenticate()
        } else {
            showAuthenticationNotification(activity)
            handler.postDelayed({ startLoop() }, 10_000)
        }
    }

    fun stopLoop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        elapsedSeconds = 0
        elapsedTimeLiveData.postValue(elapsedSeconds) // Reset and post value
        Toast.makeText(activity, "Monitoring stopped", Toast.LENGTH_SHORT).show()
    }

    private fun sendSOS() {
//        Toast.makeText(activity, "SOS Triggered! loop", Toast.LENGTH_LONG).show()
        onTrigger()
    }

    private fun showAuthenticationNotification(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId = "biometric_notification"

        val channel = NotificationChannel(
            channelId, "Biometric Alert", NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val retryIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(context, 0, retryIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Re-authentication Required")
            .setContentText("Biometric failed in background.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(101, notification)
    }
}


// --- Foreground Service ---
class BiometricForegroundService : LifecycleService() {

    private lateinit var biometricLoopManager: BiometricLoopManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // You must pass a valid FragmentActivity here. Use your MainActivity if needed.
        val activity = getCurrentActivity() ?: return
        biometricLoopManager = BiometricLoopManager(activity) {
            sendSOS()
        }

        startForeground(1001, buildPersistentNotification())
        biometricLoopManager.startLoop()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "biometric_monitor",
            "Biometric Monitor Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private fun buildPersistentNotification(): Notification {
        val exitIntent = Intent(this, ExitReceiver::class.java)
        val pendingExitIntent = PendingIntent.getBroadcast(
            this, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, "biometric_monitor")
            .setContentTitle("Biometric Monitor Active")
            .setContentText("App is monitoring biometric presence")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .addAction(android.R.drawable.ic_delete, "Exit", pendingExitIntent)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == "AUTHENTICATE_NOW") {
            biometricLoopManager.startLoop()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricLoopManager.stopLoop()
    }

//    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun sendSOS() {
        Toast.makeText(this, "SOS Triggered Foreground", Toast.LENGTH_LONG).show()
//        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            vibrator.vibrate(
//                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
//            )
//        } else {
//            vibrator.vibrate(1000)
//        }

        // ✅ Play the default alarm sound
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        ringtone.play()
        // Place SOS logic here: location, camera, SMS, etc.


    }

    // This is a workaround to get a FragmentActivity for biometric prompts
    private fun getCurrentActivity(): FragmentActivity? {
        val activity = AppVisibilityTracker.currentActivity
        return if (activity is FragmentActivity && AppVisibilityTracker.isInForeground) {
            activity
        } else {
            null
        }
    }

}

// --- Voice Command Listener ---
fun startVoiceCommand(context: Context, biometricLoopManager: BiometricLoopManager) {
    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val match = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.lowercase()
            if (match?.contains("stop") == true || match?.contains("exit monitoring") == true) {
                biometricLoopManager.stopLoop()
            }
            recognizer.startListening(intent)
        }

        override fun onError(error: Int) {
            recognizer.startListening(intent)
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })

    recognizer.startListening(intent)
}

// --- Track Foreground Activity ---
object AppVisibilityTracker : Application.ActivityLifecycleCallbacks {
    var isInForeground = false
    var currentActivity: Activity? = null

    override fun onActivityResumed(activity: Activity) {
        isInForeground = true
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        isInForeground = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

class ExitReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Stop the foreground service
        context.stopService(Intent(context, BiometricForegroundService::class.java))

        // Optional: show a toast to confirm
        Toast.makeText(context, "Biometric monitoring stopped", Toast.LENGTH_SHORT).show()
    }
}

fun startLiveStreaming(cameraHelper: RtmpCameraHelper?, streamUrl: String): Boolean {
    return try {
        cameraHelper?.startPreview()
        cameraHelper?.startStream(streamUrl)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}



