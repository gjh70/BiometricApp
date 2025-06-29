package com.biometricapp.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity // Changed to FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.biometricapp.data.UserRepository
import com.google.firebase.auth.FirebaseAuth

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(UserRepository()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnFailureListener { viewModel.biometricStatus = "Firebase auth failed: ${it.message}" }
        setContent {
            BiometricAppTheme {
                BiometricScreen(viewModel = viewModel, context = this)
            }
        }
    }
}