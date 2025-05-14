package com.sowmya.security.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.sowmya.security.BiometricLoopManager
import androidx.fragment.app.FragmentActivity
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import com.pedro.encoder.input.video.CameraHelper
import com.pedro.library.view.OpenGlView
import com.pedro.library.rtmp.RtmpCamera2
import com.pedro.common.ConnectChecker

import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sowmya.security.AppVisibilityTracker
import com.sowmya.security.data.ContactEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class SecurityViewModel : ViewModel() {
    private var biometricLoopManager: BiometricLoopManager? = null
    var isLoopStarted = false
        private set
    val elapsedTimeLiveData = MutableLiveData<Int>()
//   var activity: FragmentActivity=AppVisibilityTracker.currentActivity as FragmentActivity
    // Start the loop
    fun startLoop(activity: FragmentActivity, onTrigger: () -> Unit) {
        if (biometricLoopManager == null) {
            biometricLoopManager = BiometricLoopManager(
                activity,
                onTrigger = {
                    Log.d("BiometricLoop", "Triggered inside ViewModel")
                    // TODO: Add SOS or action here
//                    val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//                    val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
//                    ringtone.play()
                    onTrigger()
                }
            )
        }
        biometricLoopManager?.startLoop()
        Log.d("BiometricLoop", "Loop started")
    // Observe elapsed time
    biometricLoopManager?.elapsedTimeLiveData?.observeForever {
        elapsedTimeLiveData.postValue(it)
    }
        isLoopStarted = true
    }

    // Stop the loop
    fun stopLoop() {
        biometricLoopManager?.stopLoop()
        Log.d("BiometricLoop", "Loop stopped")
        isLoopStarted = false
    }


}



class CameraViewModel : ViewModel() {
    private var rtmpCamera2: RtmpCamera2? = null
    private var openGlView: OpenGlView? = null
    private var isStreaming = true
    val isFrontStreaming = mutableStateOf(true)
    val isBackStreaming = mutableStateOf(false)
    val isLive: Boolean
        get() = isFrontStreaming.value || isBackStreaming.value

    fun setFrontStreaming(value: Boolean) {
        isFrontStreaming.value = value
        isBackStreaming.value = !value
    }

    fun setBackStreaming(value: Boolean) {
        isBackStreaming.value = value
        isFrontStreaming.value = !value
    }


    fun startStream(url: String) {
        if (!isStreaming && rtmpCamera2 != null) {
            rtmpCamera2!!.startStream(url)
            isStreaming = true
        }
    }

    fun stopStream() {
        if (isStreaming && rtmpCamera2 != null) {
            rtmpCamera2!!.stopStream()
            isStreaming = false
        }
    }

    fun isStreaming(): Boolean = isStreaming

    fun getOpenGlView(): OpenGlView? = openGlView

    override fun onCleared() {
        super.onCleared()
        rtmpCamera2?.stopStream()
        rtmpCamera2?.stopPreview()
    }
}
