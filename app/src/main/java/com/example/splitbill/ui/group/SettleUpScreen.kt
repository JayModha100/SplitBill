package com.example.splitbill.ui.group

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitbill.domain.DebtSimplifier
import com.example.splitbill.ui.components.RetroButton
import com.example.splitbill.ui.components.RetroPanel
import com.example.splitbill.ui.components.RetroSecondaryButton
import com.example.splitbill.ui.theme.RetroTheme
import com.example.splitbill.util.UpiLauncher

@Composable
fun SettleUpScreen(state: GroupDashboardState, onDone: () -> Unit) {
    val context = LocalContext.current
    
    // Compute settlements from balances
    val settlements = remember(state.members.toList(), state.expenses.toList()) {
        DebtSimplifier.simplify(state.members.toList(), state.expenses.toList())
    }
    
    // Filter settlements involving current user
    val userSettlements = settlements.filter { 
        it.fromMemberId == state.currentUserId || it.toMemberId == state.currentUserId 
    }

    RetroTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settle Up",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.colors.xpBlueDark
            )

            if (userSettlements.isEmpty()) {
                RetroPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "All settled up!",
                        color = RetroTheme.colors.green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            } else {
                userSettlements.forEach { settlement ->
                    RetroPanel(modifier = Modifier.fillMaxWidth()) {
                        if (settlement.fromMemberId == state.currentUserId) {
                            // Current user owes someone
                            val toMember = state.members.find { it.id == settlement.toMemberId }
                            val toName = toMember?.name ?: "Unknown"
                            
                            Text(
                                text = "You should pay:\n$toName — ₹${settlement.amount}",
                                color = RetroTheme.colors.textDark,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RetroButton(
                                text = "Pay via UPI",
                                onClick = {
                                    val vpa = toMember?.upiId ?: ""
                                    if (vpa.isNotBlank()) {
                                        UpiLauncher.launchPayment(
                                            context = context,
                                            payeeVpa = vpa,
                                            payeeName = toName,
                                            amount = settlement.amount,
                                            note = "SplitBill ${state.groupName}"
                                        )
                                    } else {
                                        Toast.makeText(context, "No UPI ID for $toName", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else if (settlement.toMemberId == state.currentUserId) {
                            // Current user is owed money
                            val fromName = state.memberName(settlement.fromMemberId)
                            
                            Text(
                                text = "$fromName owes you ₹${settlement.amount}",
                                color = RetroTheme.colors.textDark,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            RetroButton(
                                text = "Notify",
                                onClick = {
                                    state.requestPayment(settlement.fromMemberId, settlement.toMemberId, settlement.amount)
                                    Toast.makeText(context, "Notification sent to $fromName", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            RetroSecondaryButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
