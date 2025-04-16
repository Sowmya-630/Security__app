package com.sowmya.security

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()
    private val contactsCollection = db.collection("contacts")

    // Save contact to Firestorm
    fun saveContact(contact: Contact, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        contactsCollection.add(contact)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    // Retrieve all contacts from Firestorm
    fun getContacts(onResult: (List<Contact>) -> Unit, onFailure: (Exception) -> Unit) {
        contactsCollection.get()
            .addOnSuccessListener { result ->
                val contacts = result.documents.mapNotNull { it.toObject(Contact::class.java) }
                onResult(contacts)
            }
            .addOnFailureListener { onFailure(it) }
    }
}