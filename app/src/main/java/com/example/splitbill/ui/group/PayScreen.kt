package com.example.splitbill.ui.group

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.SplitType
import com.example.splitbill.ui.components.RetroButton
import com.example.splitbill.ui.components.RetroOutlineField
import com.example.splitbill.ui.components.RetroPanel
import com.example.splitbill.ui.components.RetroSecondaryButton
import com.example.splitbill.ui.theme.RetroTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PayScreen(state: GroupDashboardState, onDone: () -> Unit) {
    RetroTheme {
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var paidById by remember { mutableStateOf<String?>(state.currentUserId.takeIf { it.isNotBlank() }) }
        var selectedSplitType by remember { mutableStateOf(SplitType.EQUAL) }

        val amount = amountText.toDoubleOrNull() ?: 0.0
        val isValid = amount > 0 && paidById != null

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Expense",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.colors.xpBlueDark
            )

            RetroPanel {
                RetroOutlineField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount (₹)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(12.dp))

                RetroOutlineField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (e.g. Lunch)"
                )

                Spacer(modifier = Modifier.height(12.dp))

                RetroOutlineField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Category (e.g. Food)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Paid by:", color = RetroTheme.colors.textDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    state.members.forEach { member ->
                        if (paidById == member.id) {
                            RetroButton(
                                text = member.name,
                                onClick = { paidById = member.id }
                            )
                        } else {
                            RetroSecondaryButton(
                                text = member.name,
                                onClick = { paidById = member.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Split type:", color = RetroTheme.colors.textDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SplitType.entries.forEach { splitType ->
                        if (selectedSplitType == splitType) {
                            RetroButton(
                                text = splitType.label,
                                onClick = { selectedSplitType = splitType }
                            )
                        } else {
                            RetroSecondaryButton(
                                text = splitType.label,
                                onClick = { selectedSplitType = splitType }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RetroSecondaryButton(
                    text = "Cancel",
                    onClick = onDone,
                    modifier = Modifier.weight(1f)
                )

                RetroButton(
                    text = "Submit",
                    enabled = isValid,
                    onClick = {
                        if (isValid) {
                            val activeMembers = state.members
                            val sharePerPerson = if (activeMembers.isNotEmpty()) amount / activeMembers.size else 0.0
                            
                            val shares = activeMembers.associate { member ->
                                // TODO: Replace equal split with actual calculated shares for EXACT, PERCENTAGE, SHARES
                                member.id to sharePerPerson
                            }

                            val expense = Expense(
                                description = description.ifBlank { "Expense" },
                                category = category.ifBlank { "General" },
                                amount = amount,
                                paidBy = paidById!!,
                                splitType = selectedSplitType,
                                shares = shares
                            )
                            state.addExpense(expense)
                            onDone()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
