package com.example.splitbill.ui.group

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun GroupDashboardScreen(
    groupId: String,
    state: GroupDashboardState,
    onPay: () -> Unit,
    onSettleUp: () -> Unit
) {
    LaunchedEffect(groupId) {
        state.loadGroup(groupId)
    }
    Text("Dashboard Stub for ${state.groupName}")
}
