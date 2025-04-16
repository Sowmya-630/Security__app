package com.sowmya.security.data


data class ContactEntity(
    var name: String = "",
    var phone: String = "",
    var email: String = "",
)

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null
)
