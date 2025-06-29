package com.biometricapp.ui

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biometricapp.data.User
import com.biometricapp.data.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun checkBiometricAvailability(activity: androidx.activity.ComponentActivity) {
        val biometricManager = BiometricManager.from(activity)
        val result = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        Log.d("BiometricDebug", "canAuthenticate result: $result")
        _uiState.value = when (result) {
            BiometricManager.BIOMETRIC_SUCCESS -> _uiState.value.copy(canUseBiometric = true, message = "")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> _uiState.value.copy(message = "No biometric hardware available")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> _uiState.value.copy(message = "Biometric hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> _uiState.value.copy(message = "No fingerprints enrolled")
            else -> _uiState.value.copy(message = "Biometric authentication not available")
        }
    }

    fun registerWithBiometric(activity: androidx.activity.ComponentActivity) {
        viewModelScope.launch {
            val (name, age, id) = showRegistrationDialog(activity) ?: run {
                _uiState.value = _uiState.value.copy(message = "Registration cancelled")
                return@launch
            }
            val success = authenticateWithBiometric(activity)
            if (success) {
                val user = User(
                    name = name,
                    age = age,
                    id = id,
                    token = System.currentTimeMillis().toString(),
                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: "user_${System.currentTimeMillis()}"
                )
                val result = repository.registerUser(user)
                _uiState.value = result.fold(
                    onSuccess = { registeredUser ->
                        _uiState.value.copy(message = "Registered: ${registeredUser.token}", user = registeredUser)
                    },
                    onFailure = { error ->
                        _uiState.value.copy(message = "Registration failed: ${error.message}")
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(message = "Fingerprint authentication failed")
            }
        }
    }

    fun authenticateAndRetrieve(activity: androidx.activity.ComponentActivity) {
        viewModelScope.launch {
            val success = authenticateWithBiometric(activity)
            if (success) {
                val userId = _uiState.value.user?.userId ?: FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    val result = repository.getUser(userId)
                    _uiState.value = result.fold(
                        onSuccess = { user ->
                            _uiState.value.copy(message = "Retrieved: Name=${user.name}, Age=${user.age}, ID=${user.id}, Token=${user.token}", user = user)
                        },
                        onFailure = { error ->
                            _uiState.value.copy(message = "Retrieval failed: ${error?.message ?: "Unknown error"}")
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(message = "No user registered or signed in")
                }
            } else {
                _uiState.value = _uiState.value.copy(message = "Fingerprint authentication failed")
            }
        }
    }

    private suspend fun authenticateWithBiometric(activity: androidx.activity.ComponentActivity): Boolean = suspendCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity as FragmentActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                continuation.resume(true)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                continuation.resume(false)
            }

            override fun onAuthenticationFailed() {
                continuation.resume(false)
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate Fingerprint")
            .setSubtitle("Scan your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private suspend fun showRegistrationDialog(activity: androidx.activity.ComponentActivity): Triple<String?, Int?, String?>? = suspendCoroutine { cont ->
        val dialog = androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Enter User Details")
            .setView(com.biometricapp.R.layout.dialog_registration)
            .setPositiveButton("Save") { _, _ ->
                val name = (activity.findViewById(com.biometricapp.R.id.nameEditText) as? android.widget.EditText)?.text?.toString()
                val age = (activity.findViewById(com.biometricapp.R.id.ageEditText) as? android.widget.EditText)?.text?.toString()?.toIntOrNull()
                val id = (activity.findViewById(com.biometricapp.R.id.idEditText) as? android.widget.EditText)?.text?.toString()
                cont.resume(Triple(name, age, id))
            }
            .setNegativeButton("Cancel") { _, _ -> cont.resume(null) }
            .create()

        dialog.show()
    }
}

data class UiState(
    val canUseBiometric: Boolean = false,
    val message: String = "",
    val user: User? = null
)