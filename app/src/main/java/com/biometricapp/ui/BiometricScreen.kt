package com.biometricapp.ui

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricScreen(enrollLauncher: ActivityResultLauncher<Intent>, viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val executor = ContextCompat.getMainExecutor(context)
    val biometricManager = BiometricManager.from(context)

    val activity = context as? FragmentActivity

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Authenticate using your biometric credential")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    val biometricPrompt = remember(activity, executor) {
        if (activity == null) {
            Log.e("BiometricScreen", "Host activity is not a FragmentActivity, BiometricPrompt cannot be initialized.")
            return@remember null
        }
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e("BiometricScreen", "Authentication error: $errorCode - $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.i("BiometricScreen", "Authentication succeeded. Type: ${result.authenticationType}")
                }

                override fun onAuthenticationFailed() {
                    Log.w("BiometricScreen", "Authentication failed (biometric not recognized)")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                viewModel.checkBiometricAvailability(context as ComponentActivity)
                if (biometricPrompt == null) {
                    Log.e("BiometricScreen", "BiometricPrompt not initialized. Cannot authenticate.")
                    return@Button
                }

                val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                when (val canAuthResult = biometricManager.canAuthenticate(authenticators)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        biometricPrompt.authenticate(promptInfo)
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        viewModel.uiState.value.copy(message = "No biometric hardware available")
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        viewModel.uiState.value.copy(message = "Biometric hardware currently unavailable. Try again later.")
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        viewModel.uiState.value.copy(message = "No biometrics enrolled. Please set up biometrics in your device settings.")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        } else {
                            viewModel.uiState.value.copy(message = viewModel.uiState.value.message + " Please go to Settings > Security to enroll.")
                        }
                    }
                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                        viewModel.uiState.value.copy(message = "A security update is required for biometric authentication.")
                    }
                    else -> {
                        viewModel.uiState.value.copy(message = "Biometric authentication not available (Code: $canAuthResult).")
                    }
                }
            },
            enabled = biometricPrompt != null
        ) {
            Text("Check Biometric Availability")
        }

        Button(
            onClick = { viewModel.registerWithBiometric(context as ComponentActivity) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Register with Biometric")
        }

        Button(
            onClick = { viewModel.authenticateAndRetrieve(context as ComponentActivity) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Retrieve User Data")
        }

        Text(
            text = uiState.value.message,
            modifier = Modifier.padding(top = 16.dp)
        )

        if (uiState.value.user != null) {
            Text(
                text = "User: ${uiState.value.user!!.name}, Age: ${uiState.value.user!!.age}, ID: ${uiState.value.user!!.id}",
                modifier = Modifier.padding(top = 8.dp),
                color = androidx.compose.ui.graphics.Color.Green
            )
        }
    }
}