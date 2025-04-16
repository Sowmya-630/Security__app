package com.sowmya.security.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun saveEmergencyContact(name: String, phone: String, relation: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        val contact = mapOf(
            "name" to name,
            "phone" to phone,
            "relation" to relation
        )

        db.collection("users")
            .document(uid)
            .collection("emergencyContacts")
            .add(contact)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
