package com.example.splitbill.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.ui.components.*
import com.example.splitbill.ui.theme.RetroTheme
import java.util.Locale

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

    val balances = state.balances()

    RetroTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradient(RetroTheme.colors.silver, RetroTheme.colors.panelGray))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RetroTitleBar(title = state.groupName)

            RetroPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Join Code:", color = RetroTheme.colors.textDark, fontWeight = FontWeight.Bold)
                    Text(
                        text = state.inviteCode,
                        color = Color.White,
                        modifier = Modifier
                            .background(RetroTheme.colors.xpBlue, RetroTheme.shapes.beveled)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text("Members", fontWeight = FontWeight.Bold, color = RetroTheme.colors.textDark, fontSize = 18.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.members) { member ->
                    MemberAvatar(member, balances[member.id] ?: 0L)
                }
            }

            Text("Expense History", fontWeight = FontWeight.Bold, color = RetroTheme.colors.textDark, fontSize = 18.sp)
            RetroPanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.expenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses yet.", color = RetroTheme.colors.textDark.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val sortedExpenses = state.expenses.sortedByDescending { it.timestampMillis }
                        items(sortedExpenses) { expense ->
                            ExpenseRow(expense, state.memberName(expense.paidBy))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RetroButton(
                    text = "Add Expense",
                    onClick = onPay,
                    modifier = Modifier.weight(1f)
                )
                RetroSecondaryButton(
                    text = "Settle Up",
                    onClick = onSettleUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MemberAvatar(member: Member, balancePaise: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(member.avatarColor))
                .border(2.dp, if (member.online) RetroTheme.colors.online else RetroTheme.colors.offline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Text(
            text = member.name,
            fontSize = 12.sp,
            color = RetroTheme.colors.textDark,
            maxLines = 1
        )
        val balanceText = if (balancePaise > 0L) "+${com.example.splitbill.domain.Money.formatPaise(balancePaise).removePrefix("₹")}₹" 
                          else if (balancePaise < 0L) "-${com.example.splitbill.domain.Money.formatPaise(-balancePaise)}" 
                          else "₹0.00"
        // Wait formatPaise returns "₹12.34".
        // If I want "+₹12.34", it's better to do:
        val formatted = com.example.splitbill.domain.Money.formatPaise(if (balancePaise < 0L) -balancePaise else balancePaise)
        val finalBalanceText = if (balancePaise > 0L) "+$formatted" else if (balancePaise < 0L) "-$formatted" else "₹0.00"
        val balanceColor = if (balancePaise > 0L) RetroTheme.colors.green else if (balancePaise < 0L) RetroTheme.colors.red else RetroTheme.colors.textDark
        Text(
            text = finalBalanceText,
            fontSize = 12.sp,
            color = balanceColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExpenseRow(expense: Expense, payerName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RetroTheme.shapes.beveled)
            .background(Color.White)
            .border(1.dp, RetroTheme.colors.silverDark, RetroTheme.shapes.beveled)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = expense.description,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = RetroTheme.colors.textDark
            )
            Text(
                text = "Paid by $payerName",
                fontSize = 12.sp,
                color = RetroTheme.colors.textDark.copy(alpha = 0.7f)
            )
            Text(
                text = expense.category,
                fontSize = 10.sp,
                color = RetroTheme.colors.xpBlue
            )
        }
        Text(
            text = com.example.splitbill.domain.Money.formatPaise(expense.amountPaise),
            fontWeight = FontWeight.Bold,
            color = RetroTheme.colors.red,
            fontSize = 16.sp
        )
    }
}
