package com.biometricapp.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity // Changed to FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biometricapp.data.User
import com.biometricapp.data.UserRepository
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {
    var biometricStatus by mutableStateOf("Checking biometric availability...")
    var registrationStatus by mutableStateOf("")
    var retrievedData by mutableStateOf("")
    var showRegistrationDialog by mutableStateOf(false)
    private var currentName: String = ""
    private var currentId: String = ""

    fun checkBiometricAvailability(context: Context) {
        val biometricManager = BiometricManager.from(context)
        biometricStatus = when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometric authentication is available."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware available."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometrics enrolled."
            else -> "Biometric authentication not supported."
        }
    }

    fun startRegistration(context: FragmentActivity, name: String, id: String) {
        currentName = name
        currentId = id
        authenticate(context, isRegistration = true)
    }

    fun authenticateAndRetrieve(context: FragmentActivity, id: String) {
        currentId = id
        authenticate(context, isRegistration = false)
    }

    private fun authenticate(context: FragmentActivity, isRegistration: Boolean) {
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (isRegistration) {
                    registrationStatus = "Registration failed: $errString"
                } else {
                    retrievedData = "Authentication failed: $errString"
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModelScope.launch {
                    if (isRegistration) {
                        val user = User(id = currentId, name = currentName)
                        val result = userRepository.registerUser(user)
                        registrationStatus = when {
                            result.isSuccess -> "Registration successful for ${user.name}"
                            else -> "Registration failed: ${result.exceptionOrNull()?.message}"
                        }
                    } else {
                        val result = userRepository.getUser(currentId)
                        retrievedData = when {
                            result.isSuccess -> {
                                val user = result.getOrNull()
                                if (user != null) "Retrieved: Name = ${user.name}, ID = ${user.id}"
                                else "No data found for ID: $currentId"
                            }
                            else -> "Retrieval failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                if (isRegistration) {
                    registrationStatus = "Authentication failed."
                } else {
                    retrievedData = "Authentication failed."
                }
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(if (isRegistration) "Register with Biometric" else "Authenticate to Retrieve Data")
            .setSubtitle("Confirm your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}