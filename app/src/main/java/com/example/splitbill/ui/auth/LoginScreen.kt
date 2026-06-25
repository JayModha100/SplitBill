package com.example.splitbill.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.example.splitbill.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
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
                    // Safely get the activity from context
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
                        
                        // Firebase Auth
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess()
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
}
