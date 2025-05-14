package com.sowmya.security.viewmodel

<<<<<<< HEAD
import android.app.Application
=======
import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
>>>>>>> f0358ee (security app)
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sowmya.security.data.ContactEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
<<<<<<< HEAD
import androidx.compose.runtime.mutableStateOf
=======
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
>>>>>>> f0358ee (security app)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loginWithEmailPassword(
        email: String,
        password: String,
        onSuccess: (isAdmin: Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val isAdmin = email.endsWith("@grietcollege.com")
                onSuccess(isAdmin)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun signUpWithEmailPassword(
        email: String,
        password: String,
        onSuccess: (isAdmin: Boolean) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                user?.let {
                    val userDoc = Firebase.firestore.collection("users").document(it.uid)
                    userDoc.set(
                        mapOf(
                            "email" to email,
                            "name" to it.displayName.orEmpty(),
                            "token" to "" // FCM token set later
                        )
                    )
                }
                val isAdmin = email.endsWith("@grietcollege.com")
                onSuccess(isAdmin)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun logout() {
        auth.signOut()
    }
    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                onSuccess()
            }
            ?.addOnFailureListener {
                onFailure(it)
            }
    }
}


class ContactViewModel : ViewModel() {

    private val _contacts = MutableStateFlow<List<ContactEntity>>(emptyList())
    val contacts: StateFlow<List<ContactEntity>> = _contacts
<<<<<<< HEAD

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
=======
    private val smsManager: SmsManager = SmsManager.getDefault()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    var currentUserId = auth.currentUser?.uid ?: "unknown"


    // Define SMS_PERMISSION_REQUEST_CODE
    companion object {
        private const val SMS_PERMISSION_REQUEST_CODE = 100
    }
>>>>>>> f0358ee (security app)

    init {
        loadContactsFromFirestore()
    }

    fun addContact(contact: ContactEntity) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .add(contact)
            .addOnSuccessListener { loadContactsFromFirestore() }
    }

    fun deleteContact(contact: ContactEntity) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .whereEqualTo("name", contact.name)
            .whereEqualTo("phoneNumber", contact.phone)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot) {
                    db.collection("users")
                        .document(uid)
                        .collection("emergencyContacts")
                        .document(doc.id)
                        .delete()
                        .addOnSuccessListener { loadContactsFromFirestore() }
                }
            }
    }

    private fun loadContactsFromFirestore() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .get()
            .addOnSuccessListener { result ->
                val contacts = result.map { it.toObject(ContactEntity::class.java) }
                _contacts.value = contacts
            }
    }
<<<<<<< HEAD
}


class StreamViewModel : ViewModel() {
    val isFrontStreaming = mutableStateOf(false)
    val isBackStreaming = mutableStateOf(false)
    val isLive: Boolean
        get() = isFrontStreaming.value || isBackStreaming.value

    fun setFrontStreaming(value: Boolean) {
        isFrontStreaming.value = value
    }

    fun setBackStreaming(value: Boolean) {
        isBackStreaming.value = value
    }
}
=======
    fun sendSosMessage(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val live = "http://16.170.228.168/stream.html?stream=$"
                val livelocation = "http://16.170.228.168/live-location.html?id=$currentUserId"
                val streamUrl = "rtmp://16.170.228.168/live/$currentUserId"
                val message = "🚨 SOS Alert! I need help!"
                val fullMessage = "$message \n Live Streaming: $live\n Live Location: $livelocation"
                val SENT = "SMS_SENT"
                val DELIVERED = "SMS_DELIVERED"

                val parts = smsManager.divideMessage(fullMessage)

                val sentIntents = List(parts.size) {
                    PendingIntent.getBroadcast(context, 0, Intent(SENT), PendingIntent.FLAG_IMMUTABLE)
                }
                val deliveredIntents = List(parts.size) {
                    PendingIntent.getBroadcast(context, 0, Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE)
                }

                // Send the message to each contact
                for (contact in contacts.value) {
                    try {
                        smsManager.sendMultipartTextMessage(
                            contact.phone,
                            null,
                            parts,
                            sentIntents as java.util.ArrayList<PendingIntent>,
                            deliveredIntents as ArrayList<PendingIntent>
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to send SMS to ${contact.phone}", Toast.LENGTH_SHORT).show()
                        Log.e("ContactViewModel", "Failed to send SMS to ${contact.phone}", e)
                    }
                }
            }
        }
    }
    fun checkSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request permission if not granted
    fun requestSmsPermission(context: Context) {
        if (!checkSmsPermission(context)) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }

    // Set SMS permission status


}



>>>>>>> f0358ee (security app)
