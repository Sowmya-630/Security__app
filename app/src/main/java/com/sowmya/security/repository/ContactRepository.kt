package com.sowmya.security.repository

import com.sowmya.security.data.ContactDao
import com.sowmya.security.data.ContactEntity

class ContactRepository(private val dao: ContactDao) {
    fun getAllContacts() = dao.getAllContacts()
    suspend fun addContact(contact: ContactEntity) = dao.insert(contact)
    suspend fun deleteContact(contact: ContactEntity) = dao.delete(contact)
}