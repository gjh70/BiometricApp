package com.biometricapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.biometricapp.ui.theme.BiometricAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.e("MainActivity", "Anonymous sign-in failed", task.exception)
            }
        }
        val enrollLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.checkBiometricAvailability(this)
        }
        setContent {
            BiometricAppTheme {
                BiometricScreen(enrollLauncher = enrollLauncher, viewModel = viewModel)
            }
        }
    }
}