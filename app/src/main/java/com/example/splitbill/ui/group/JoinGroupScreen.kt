package com.example.splitbill.ui.group

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.splitbill.data.repository.GroupRepository
import com.example.splitbill.ui.components.RetroButton
import com.example.splitbill.ui.components.RetroOutlineField
import com.example.splitbill.ui.components.RetroPanel
import com.example.splitbill.ui.components.RetroSecondaryButton
import com.example.splitbill.ui.components.RetroTitleBar
import com.example.splitbill.ui.components.verticalGradient
import com.example.splitbill.ui.theme.RetroTheme
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

    var joinCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    RetroTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradient(RetroTheme.colors.silver, RetroTheme.colors.panelGray))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RetroPanel {
                RetroTitleBar(title = "Join a Group")

                Spacer(modifier = Modifier.height(24.dp))

                RetroOutlineField(
                    value = joinCode,
                    onValueChange = { if (it.length <= 6) joinCode = it.uppercase() },
                    label = "Enter 6-digit code",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )

                Spacer(modifier = Modifier.height(24.dp))

                RetroButton(
                    text = if (isLoading) "Joining..." else "Join Group",
                    onClick = {
                        if (joinCode.isBlank()) {
                            Toast.makeText(context, "Enter a join code", Toast.LENGTH_SHORT).show()
                            return@RetroButton
                        }
                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                        if (uid == null) {
                            Toast.makeText(context, "You need to sign in first", Toast.LENGTH_LONG).show()
                            return@RetroButton
                        }
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = repo.joinGroup(joinCode, uid)
                            CoroutineScope(Dispatchers.Main).launch {
                                isLoading = false
                                result.onSuccess { joinResult ->
                                    if (joinResult.alreadyMember) {
                                        Toast.makeText(context, "You're already in \"${joinResult.groupName}\"", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Joined \"${joinResult.groupName}\"!", Toast.LENGTH_SHORT).show()
                                    }
                                    onGroupJoined(joinResult.groupId)
                                }.onFailure { e ->
                                    Log.e("JoinGroup", "Firestore query failed", e)
                                    Toast.makeText(context, e.message ?: "Join failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                RetroSecondaryButton(
                    text = "Back",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
