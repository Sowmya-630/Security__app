package com.sowmya.security.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sowmya.security.data.ContactEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class ContactViewModel(application: Application) : ViewModel() {

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
            .whereEqualTo("phoneNumber", contact.phoneNumber)
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
