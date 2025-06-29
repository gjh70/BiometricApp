package com.biometricapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity // Changed to FragmentActivity

@Composable
fun BiometricScreen(viewModel: MainViewModel, context: FragmentActivity) {
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var id by remember { mutableStateOf(TextFieldValue("")) }
    var retrieveId by remember { mutableStateOf(TextFieldValue("")) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = viewModel.biometricStatus)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.checkBiometricAvailability(context) }) {
            Text("Check Biometric Availability")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.showRegistrationDialog = true }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = retrieveId,
            onValueChange = { retrieveId = it },
            label = { Text("ID to Retrieve") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { if (retrieveId.text.isNotBlank()) viewModel.authenticateAndRetrieve(context, retrieveId.text) },
            enabled = retrieveId.text.isNotBlank()
        ) {
            Text("Authenticate and Retrieve")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = viewModel.registrationStatus)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = viewModel.retrievedData)
        if (viewModel.showRegistrationDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.showRegistrationDialog = false },
                title = { Text("Enter Details") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.text.isNotBlank() && id.text.isNotBlank()) {
                                viewModel.startRegistration(context, name.text, id.text)
                                viewModel.showRegistrationDialog = false
                                name = TextFieldValue("")
                                id = TextFieldValue("")
                            }
                        },
                        enabled = name.text.isNotBlank() && id.text.isNotBlank()
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { viewModel.showRegistrationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}