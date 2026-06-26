package com.example.splitbill.ui.group

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.SplitType
import com.example.splitbill.domain.SplitCalculator
import com.example.splitbill.ui.components.RetroButton
import com.example.splitbill.ui.components.RetroOutlineField
import com.example.splitbill.ui.components.RetroPanel
import com.example.splitbill.ui.components.RetroSecondaryButton
import com.example.splitbill.ui.theme.RetroTheme
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PayScreen(state: GroupDashboardViewModel, onDone: () -> Unit) {
    val context = LocalContext.current

    RetroTheme {
        var amountText by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var paidById by remember { mutableStateOf<String?>(state.currentUserId.takeIf { it.isNotBlank() }) }
        var selectedSplitType by remember { mutableStateOf(SplitType.EQUAL) }
        var selectedRecurrence by remember { mutableStateOf("NONE") }
        val recurrences = listOf("NONE", "DAILY", "WEEKLY", "MONTHLY")

        var exactAmounts by remember { mutableStateOf(mapOf<String, String>()) }
        var percentages by remember { mutableStateOf(mapOf<String, String>()) }
        var shareWeights by remember { mutableStateOf(mapOf<String, String>()) }

        val amountDouble = amountText.toDoubleOrNull() ?: 0.0
        val amountPaise = com.example.splitbill.domain.Money.toPaise(amountDouble)
        val percentTotal = percentages.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
        
        val isValid = amountPaise > 0L && paidById != null && (
            selectedSplitType != SplitType.PERCENTAGE || abs(percentTotal - 100.0) <= 0.01
        )

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

                Text("Recurrence:", color = RetroTheme.colors.textDark, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recurrences.forEach { rec ->
                        if (selectedRecurrence == rec) {
                            RetroButton(text = rec, onClick = { selectedRecurrence = rec })
                        } else {
                            RetroSecondaryButton(text = rec, onClick = { selectedRecurrence = rec })
                        }
                    }
                }

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

                if (selectedSplitType != SplitType.EQUAL) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when (selectedSplitType) {
                            SplitType.EXACT -> "Enter Exact Amounts:"
                            SplitType.PERCENTAGE -> "Enter Percentages (Total: ${String.format(Locale.US, "%.2f", percentTotal)}%):"
                            SplitType.SHARES -> "Enter Share Weights:"
                            else -> ""
                        },
                        color = RetroTheme.colors.textDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    state.members.forEach { member ->
                        val value = when (selectedSplitType) {
                            SplitType.EXACT -> exactAmounts[member.id] ?: ""
                            SplitType.PERCENTAGE -> percentages[member.id] ?: ""
                            SplitType.SHARES -> shareWeights[member.id] ?: ""
                            else -> ""
                        }
                        val kbdType = if (selectedSplitType == SplitType.SHARES) KeyboardType.Number else KeyboardType.Decimal
                        
                        RetroOutlineField(
                            value = value,
                            onValueChange = { newValue ->
                                when (selectedSplitType) {
                                    SplitType.EXACT -> exactAmounts = exactAmounts + (member.id to newValue)
                                    SplitType.PERCENTAGE -> percentages = percentages + (member.id to newValue)
                                    SplitType.SHARES -> shareWeights = shareWeights + (member.id to newValue)
                                    else -> {}
                                }
                            },
                            label = member.name,
                            keyboardOptions = KeyboardOptions(keyboardType = kbdType)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                            val sharesPaise: Map<String, Long>
                            
                            try {
                                sharesPaise = when (selectedSplitType) {
                                    SplitType.EQUAL -> SplitCalculator.equal(amountPaise, activeMembers.map { it.id })
                                    SplitType.EXACT -> {
                                        val amountsMap = exactAmounts.mapValues { com.example.splitbill.domain.Money.toPaise(it.value.toDoubleOrNull() ?: 0.0) }
                                        SplitCalculator.exact(amountPaise, amountsMap)
                                    }
                                    SplitType.PERCENTAGE -> {
                                        val percentMap = percentages.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
                                        SplitCalculator.percentage(amountPaise, percentMap)
                                    }
                                    SplitType.SHARES -> {
                                        val sharesMap = shareWeights.mapValues { it.value.toIntOrNull() ?: 0 }
                                        SplitCalculator.shares(amountPaise, sharesMap)
                                    }
                                }
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                                return@RetroButton
                            }

                            val nextRunMillis = if (selectedRecurrence != "NONE") {
                                val now = System.currentTimeMillis()
                                val oneDayMillis = 24L * 60 * 60 * 1000
                                when (selectedRecurrence) {
                                    "DAILY" -> now + oneDayMillis
                                    "WEEKLY" -> now + 7L * oneDayMillis
                                    "MONTHLY" -> now + 30L * oneDayMillis
                                    else -> 0L
                                }
                            } else 0L

                            val expense = Expense(
                                description = description.ifBlank { "Expense" },
                                category = category.ifBlank { "General" },
                                amountPaise = amountPaise,
                                paidBy = paidById!!,
                                splitType = selectedSplitType,
                                sharesPaise = sharesPaise,
                                recurrence = selectedRecurrence,
                                nextRunMillis = nextRunMillis
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
