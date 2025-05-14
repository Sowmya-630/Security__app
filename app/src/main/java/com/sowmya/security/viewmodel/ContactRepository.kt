package com.sowmya.security.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sowmya.security.data.ContactEntity
import kotlinx.coroutines.tasks.await

object ContactRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val contactsCollection get() = db.collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("contacts")

    suspend fun addContact(contact: ContactEntity) {
        contactsCollection.document(contact.phone).set(contact).await()
    }

    suspend fun deleteContact(phoneNumber: String) {
        contactsCollection.document(phoneNumber).delete().await()
    }

    suspend fun getContacts(): List<ContactEntity> {
        return contactsCollection.get().await().toObjects(ContactEntity::class.java)
    }
}