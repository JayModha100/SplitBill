package com.example.splitbill.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    val credentialManager = CredentialManager.create(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome, ${user?.displayName ?: user?.email ?: "User"}!",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

            Button(onClick =
                {

                }, shape = RoundedCornerShape(5.dp), modifier = Modifier.width(300.dp), colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF005EBD), contentColor = Color.White))
            {
                Text("+ Create a group")
            }
            Spacer(Modifier.width(10.dp))
            Button(onClick = {

            }, shape = RoundedCornerShape(5.dp), modifier = Modifier.width(300.dp),colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF005EBD), contentColor = Color.White)) {
                Text("Join an existing group")
            }

//        Button(onClick = {
//            CoroutineScope(Dispatchers.IO).launch {
//                auth.signOut()
//                try {
//                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                CoroutineScope(Dispatchers.Main).launch {
//                    onLogout()
//                }
//            }
//        }) {
//            Text(text = "Logout")
//        }
    }
}
