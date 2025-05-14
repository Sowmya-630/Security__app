package com.sowmya.security.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sowmya.security.data.UserProfile

class ProfileViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                document?.let {
                    val profile = UserProfile(
                        name = it.getString("name") ?: "",
                        email = it.getString("email") ?: "",
                        profileImageUrl = it.getString("profileImageUrl")
                    )
                    userProfile = profile
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileViewModel", "Failed to fetch profile", e)
            }
    }
    fun uploadProfilePicture(
        uri: Uri,
        context: Context,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val imageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update("profileImageUrl", downloadUrl.toString())
                        .addOnSuccessListener { onSuccess(downloadUrl.toString()) }
                        .addOnFailureListener(onError)
                }
            }
            .addOnFailureListener(onError)
    }

    fun loadUserProfileImage(onComplete: (Uri?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val url = document.getString("profileImageUrl")
                onComplete(url?.let { Uri.parse(it) })
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }



}


