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
import androidx.compose.ui.unit.dp
import com.example.splitbill.data.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CreateGroupScreen(
    onGroupCreated: (groupId: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { GroupRepository() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var groupName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var createdJoinCode by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create a Group", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        if (createdJoinCode != null) {
            Text("Group created!", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Share this code with your friends:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                createdJoinCode!!,
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF005EBD)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.width(300.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF005EBD),
                    contentColor = Color.White
                )
            ) {
                Text("Done")
            }
        } else {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        Toast.makeText(context, "Enter a group name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (currentUserId.isEmpty()) {
                        Toast.makeText(context, "Not signed in", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    CoroutineScope(Dispatchers.IO).launch {
                        val result = repo.createGroup(groupName.trim(), currentUserId)
                        CoroutineScope(Dispatchers.Main).launch {
                            isLoading = false
                            result.onSuccess { group ->
                                createdJoinCode = group.joinCode
                            }.onFailure { e ->
                                Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
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
                Text(if (isLoading) "Creating..." else "Create Group")
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
}
