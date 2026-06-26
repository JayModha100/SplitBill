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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GroupDashboardScreen(
    groupId: String,
    viewModel: GroupDashboardViewModel,
    onPay: () -> Unit,
    onSettleUp: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    val balances = viewModel.balances()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredExpenses = remember(viewModel.expenses.toList(), searchQuery, selectedCategory) {
        val lowerSearch = searchQuery.lowercase()
        viewModel.expenses.filter { expense ->
            val matchesSearch = if (lowerSearch.isEmpty()) true else {
                expense.description.lowercase().contains(lowerSearch) ||
                expense.category.lowercase().contains(lowerSearch) ||
                viewModel.memberName(expense.paidBy).lowercase().contains(lowerSearch)
            }
            val matchesCategory = if (selectedCategory == "All") true else {
                expense.category == selectedCategory
            }
            matchesSearch && matchesCategory
        }.sortedByDescending { it.timestampMillis }
    }

    val categories = remember(viewModel.expenses.toList()) {
        listOf("All") + viewModel.expenses.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    RetroTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(verticalGradient(RetroTheme.colors.silver, RetroTheme.colors.panelGray))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RetroTitleBar(title = viewModel.groupName)

            RetroPanel(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Join Code:", color = RetroTheme.colors.textDark, fontWeight = FontWeight.Bold)
                    Text(
                        text = viewModel.inviteCode,
                        color = Color.White,
                        modifier = Modifier
                            .background(RetroTheme.colors.xpBlue, RetroTheme.shapes.beveled)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text("Members", fontWeight = FontWeight.Bold, color = RetroTheme.colors.textDark, fontSize = 18.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.members) { member ->
                    MemberAvatar(member, balances[member.id] ?: 0L)
                }
            }

            Text("Expense History", fontWeight = FontWeight.Bold, color = RetroTheme.colors.textDark, fontSize = 18.sp)
            
            RetroOutlineField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search by description, category or name"
            )

            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    if (selectedCategory == category) {
                        RetroButton(
                            text = category,
                            onClick = { selectedCategory = category }
                        )
                    } else {
                        RetroSecondaryButton(
                            text = category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            RetroPanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (filteredExpenses.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses match.", color = RetroTheme.colors.textDark.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredExpenses) { expense ->
                            ExpenseRow(expense, viewModel.memberName(expense.paidBy))
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RetroSecondaryButton(
                    text = "Export CSV",
                    onClick = {
                        val uri = com.example.splitbill.util.CsvExporter.exportToCsv(
                            context = context,
                            groupName = viewModel.groupName,
                            expenses = viewModel.expenses,
                            getMemberName = { viewModel.memberName(it) }
                        )
                        if (uri != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share CSV"))
                        } else {
                            android.widget.Toast.makeText(context, "Failed to export CSV", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                RetroSecondaryButton(
                    text = "Export PDF",
                    onClick = {
                        val uri = com.example.splitbill.util.PdfExporter.exportToPdf(
                            context = context,
                            groupName = viewModel.groupName,
                            members = viewModel.members,
                            balances = balances,
                            expenses = viewModel.expenses,
                            getMemberName = { viewModel.memberName(it) }
                        )
                        if (uri != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share PDF"))
                        } else {
                            android.widget.Toast.makeText(context, "Failed to export PDF", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
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
