package com.example.healthhive.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthhive.viewmodels.ProfileField
import com.example.healthhive.viewmodels.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val profileState by viewModel.profileState.collectAsState()

    if (profileState.isFetching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Section
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Editable Fields
            OutlinedTextField(
                value = profileState.name,
                onValueChange = { viewModel.updateField(ProfileField.NAME, it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = profileState.email,
                onValueChange = {},
                enabled = false, // Email is not editable
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = profileState.mobileNumber,
                onValueChange = { viewModel.updateField(ProfileField.MOBILE_NUMBER, it) },
                label = { Text("Mobile Number") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = profileState.age,
                onValueChange = { viewModel.updateField(ProfileField.AGE, it) },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = profileState.address,
                onValueChange = { viewModel.updateField(ProfileField.ADDRESS, it) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { viewModel.saveProfile() },
                enabled = !profileState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (profileState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}




