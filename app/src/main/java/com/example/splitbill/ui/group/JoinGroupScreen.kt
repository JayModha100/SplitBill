package com.example.splitbill.ui.group

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.splitbill.data.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun JoinGroupScreen(
    onGroupJoined: (groupId: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { GroupRepository() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var joinCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Join a Group", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = joinCode,
            onValueChange = { if (it.length <= 6) joinCode = it.uppercase() },
            label = { Text("Enter 6-digit code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (joinCode.isBlank()) {
                    Toast.makeText(context, "Enter a join code", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (currentUserId.isEmpty()) {
                    Toast.makeText(context, "Not signed in", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                CoroutineScope(Dispatchers.IO).launch {
                    val result = repo.joinGroup(joinCode, currentUserId)
                    CoroutineScope(Dispatchers.Main).launch {
                        isLoading = false
                        result.onSuccess { joinResult ->
                            if (joinResult.alreadyMember) {
                                Toast.makeText(
                                    context,
                                    "You're already in \"${joinResult.groupName}\"",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Joined \"${joinResult.groupName}\"!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onGroupJoined(joinResult.groupId)
                        }.onFailure { e ->
                            Toast.makeText(context, e.message ?: "Join failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.width(300.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF005EBD),
                contentColor = Color.White
            )
        ) {
            Text(if (isLoading) "Joining..." else "Join Group")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBack,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.width(300.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Gray,
                contentColor = Color.White
            )
        ) {
            Text("Back")
        }
    }
}
