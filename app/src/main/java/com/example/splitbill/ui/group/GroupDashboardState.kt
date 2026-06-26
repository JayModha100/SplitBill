package com.example.splitbill.ui.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.splitbill.data.model.ActivityEvent
import com.example.splitbill.data.model.ActivityKind
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import com.example.splitbill.data.model.SplitType
import com.example.splitbill.domain.DebtSimplifier
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDashboardState(
    groupName: String = "",
    inviteCode: String = "",
    val currentUserId: String = ""
) {
    var groupName by mutableStateOf(groupName)
    var inviteCode by mutableStateOf(inviteCode)

    private val expenseRepository = com.example.splitbill.data.repository.ExpenseRepository()
    private var currentGroupId: String = ""

    val members = mutableStateListOf<Member>()
    val expenses = mutableStateListOf<Expense>()
    val activity = mutableStateListOf<ActivityEvent>()

    fun memberName(id: String): String {
        return members.find { it.id == id }?.name ?: "Unknown"
    }

    fun balances(): Map<String, Double> {
        return DebtSimplifier.computeBalances(members, expenses)
    }

    fun addExpense(expense: Expense) {
        CoroutineScope(Dispatchers.IO).launch {
            val expenseToSave = expense.copy(groupId = currentGroupId)
            val result = expenseRepository.addExpense(expenseToSave)
            result.onSuccess { savedExpense ->
                withContext(Dispatchers.Main) {
                    expenses.add(savedExpense)
                    val payerName = memberName(savedExpense.paidBy)
                    activity.add(
                        ActivityEvent(
                            kind = ActivityKind.EXPENSE,
                            message = "$payerName paid ₹${savedExpense.amount} for ${savedExpense.description}."
                        )
                    )
                    activity.add(
                        ActivityEvent(
                            kind = ActivityKind.SPLIT,
                            message = "Expense split ${savedExpense.splitType.label} among ${savedExpense.shares.size} members."
                        )
                    )
                }
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    fun recordSettlement(settlement: Settlement) {
        val fromName = memberName(settlement.fromMemberId)
        val toName = memberName(settlement.toMemberId)
        activity.add(
            ActivityEvent(
                kind = ActivityKind.SETTLEMENT,
                message = "$fromName settled ₹${settlement.amount} with $toName."
            )
        )
    }

    fun requestPayment(fromMemberId: String, toMemberId: String, amount: Double) {
        // requester is toMember (the one owed)
        val fromName = memberName(fromMemberId)
        val toName = memberName(toMemberId)
        activity.add(
            ActivityEvent(
                kind = ActivityKind.REQUEST,
                message = "$toName requested ₹$amount from $fromName."
            )
        )
    }

    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        CoroutineScope(Dispatchers.IO).launch {
            val result = com.example.splitbill.data.repository.GroupRepository().getGroup(groupId)
            val expensesResult = expenseRepository.getExpenses(groupId)
            
            result.onSuccess { group ->
                val profilesResult = com.example.splitbill.data.repository.UserRepository().getProfiles(group.members)
                val profilesMap = profilesResult.getOrNull()?.associateBy { it.uid } ?: emptyMap()

                withContext(Dispatchers.Main) {
                    groupName = group.groupName
                    inviteCode = group.joinCode
                    members.clear()
                    group.members.forEach { memberId ->
                        val profile = profilesMap[memberId]
                        val name = if (memberId == currentUserId) {
                            "You"
                        } else if (profile != null && profile.displayName.isNotBlank()) {
                            profile.displayName
                        } else {
                            "User ${memberId.take(4)}"
                        }
                        members.add(Member(id = memberId, name = name, upiId = profile?.upiId))
                    }
                }
            }

            withContext(Dispatchers.Main) {
                expensesResult.onSuccess { loadedExpenses ->
                    expenses.clear()
                    expenses.addAll(loadedExpenses)
                }
            }
        }
    }

    companion object {
        fun demo(): GroupDashboardState {
            val jayId = UUID.randomUUID().toString()
            val aryanId = UUID.randomUUID().toString()
            val neelId = UUID.randomUUID().toString()
            val riyaId = UUID.randomUUID().toString()

            val state = GroupDashboardState(
                groupName = "Goa Trip",
                inviteCode = "GOA123",
                currentUserId = jayId
            )

            state.members.apply {
                add(Member(id = jayId, name = "Jay (You)", online = true))
                add(Member(id = aryanId, name = "Aryan", avatarColor = 0xFF4CAF50, online = true))
                add(Member(id = neelId, name = "Neel", avatarColor = 0xFFC62828, online = false))
                add(Member(id = riyaId, name = "Riya", avatarColor = 0xFF9E9E9E, online = true))
            }

            // Seed Welcome and Member Joined events
            state.activity.add(
                ActivityEvent(
                    kind = ActivityKind.WELCOME,
                    message = "Welcome to Goa Trip, Jay!"
                )
            )
            state.activity.add(
                ActivityEvent(
                    kind = ActivityKind.MEMBER_JOINED,
                    message = "Aryan joined the group."
                )
            )

            // Seed an expense
            state.addExpense(
                Expense(
                    description = "Lunch",
                    category = "Food",
                    amount = 750.0,
                    paidBy = jayId,
                    splitType = SplitType.EQUAL,
                    shares = mapOf(
                        jayId to 187.5,
                        aryanId to 187.5,
                        neelId to 187.5,
                        riyaId to 187.5
                    )
                )
            )

            return state
        }
    }
}

@Composable
fun rememberGroupDashboardState(): GroupDashboardState = remember { GroupDashboardState.demo() }
