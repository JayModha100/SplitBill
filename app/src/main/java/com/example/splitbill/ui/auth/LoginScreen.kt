package com.example.splitbill.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.splitbill.R
import com.example.splitbill.data.model.UserProfile
import com.example.splitbill.data.repository.UserRepository
import com.example.splitbill.ui.components.RetroButton
import com.example.splitbill.ui.components.RetroOutlineField
import com.example.splitbill.ui.components.RetroPanel
import com.example.splitbill.ui.components.RetroSecondaryButton
import com.example.splitbill.ui.theme.RetroTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val credentialManager = CredentialManager.create(context)
    val userRepository = remember { UserRepository() }

    var showProfileDialog by remember { mutableStateOf(false) }
    var userToSetup by remember { mutableStateOf<FirebaseUser?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to SplitBill",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                coroutineScope.launch {
                    val googleIdOption = com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
                        .Builder(context.getString(R.string.default_web_client_id))
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    try {
                        var activityContext = context
                        while (activityContext is android.content.ContextWrapper) {
                            if (activityContext is Activity) break
                            activityContext = activityContext.baseContext
                        }
                        
                        val result: GetCredentialResponse = credentialManager.getCredential(
                            request = request,
                            context = activityContext
                        )
                        val credential = result.credential

                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser
                                        if (user != null) {
                                            coroutineScope.launch {
                                                val profileResult = userRepository.getProfile(user.uid)
                                                if (profileResult.isSuccess) {
                                                    onLoginSuccess()
                                                } else {
                                                    userToSetup = user
                                                    showProfileDialog = true
                                                }
                                            }
                                        } else {
                                            onLoginSuccess()
                                        }
                                    } else {
                                        Log.e("LoginScreen", "Firebase sign-in failed", task.exception)
                                        android.widget.Toast.makeText(context, "Firebase error: ${task.exception?.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            android.widget.Toast.makeText(context, "Unexpected credential type", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: GetCredentialException) {
                        Log.e("LoginScreen", "Google Sign-In failed", e)
                        android.widget.Toast.makeText(context, "Sign-in failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error", e)
                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text(text = "Continue with Google")
            }
        }

        if (showProfileDialog && userToSetup != null) {
            var displayName by remember { mutableStateOf(userToSetup!!.displayName ?: "") }
            var upiId by remember { mutableStateOf("") }
            val isUpiValid = upiId.isBlank() || upiId.contains("@")
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    // Block taps behind dialog
                    .clickable(enabled = true, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                RetroTheme {
                    RetroPanel(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Complete Profile",
                            fontSize = 20.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = RetroTheme.colors.textDark
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RetroOutlineField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = "Display Name"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RetroOutlineField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = "UPI ID (optional, e.g. name@bank)"
                        )
                        if (!isUpiValid) {
                            Text("Invalid UPI ID. Must contain '@'.", color = RetroTheme.colors.red, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            RetroSecondaryButton(
                                text = "Skip",
                                onClick = {
                                    coroutineScope.launch {
                                        val profile = UserProfile(
                                            uid = userToSetup!!.uid,
                                            displayName = displayName.ifBlank { "User ${userToSetup!!.uid.take(4)}" }
                                        )
                                        userRepository.upsertProfile(profile)
                                        showProfileDialog = false
                                        onLoginSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            RetroButton(
                                text = "Save",
                                enabled = isUpiValid && displayName.isNotBlank(),
                                onClick = {
                                    coroutineScope.launch {
                                        val profile = UserProfile(
                                            uid = userToSetup!!.uid,
                                            displayName = displayName,
                                            upiId = upiId.takeIf { it.isNotBlank() }
                                        )
                                        userRepository.upsertProfile(profile)
                                        showProfileDialog = false
                                        onLoginSuccess()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
