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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun CreateGroupScreen(
    onGroupCreated: (groupId: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { GroupRepository() }

    var groupName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var createdJoinCode by remember { mutableStateOf<String?>(null) }
    var createdGroupId by remember { mutableStateOf<String?>(null) }

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
                RetroTitleBar(title = "Create a Group")

                Spacer(modifier = Modifier.height(24.dp))

                if (createdJoinCode != null) {
                    Text("Group created!", color = RetroTheme.colors.textDark, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Share this code with your friends:",
                        color = RetroTheme.colors.textDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = createdJoinCode!!,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = RetroTheme.colors.xpBlueDark,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    RetroButton(
                        text = "Done",
                        onClick = { createdGroupId?.let { onGroupCreated(it) } ?: onBack() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    RetroOutlineField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = "Group Name",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RetroButton(
                        text = if (isLoading) "Creating..." else "Create Group",
                        onClick = {
                            val uid = com.example.splitbill.util.CurrentUser.uid()
                            if (uid == null) {
                                Toast.makeText(context, "You need to sign in first", Toast.LENGTH_LONG).show()
                                return@RetroButton
                            }
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val result = repo.createGroup(groupName.trim(), uid)
                                CoroutineScope(Dispatchers.Main).launch {
                                    isLoading = false
                                    result.onSuccess { group ->
                                        createdJoinCode = group.joinCode
                                        createdGroupId = group.groupId
                                    }.onFailure { e ->
                                        Log.e("CreateGroup", "Firestore write failed", e)
                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && groupName.isNotBlank(),
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
}

