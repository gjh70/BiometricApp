package com.biometricapp.data
import com.google.firebase.firestore.PropertyName
data class User(
    @PropertyName("id") val id: String = "",
    @PropertyName("name") val name: String = ""
)