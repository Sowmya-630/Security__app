package com.sowmya.security.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sowmya.security.data.ContactEntity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.mutableStateOf

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

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
