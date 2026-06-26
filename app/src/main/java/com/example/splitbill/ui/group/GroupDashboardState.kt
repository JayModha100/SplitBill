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

class GroupDashboardState(
    groupName: String = "",
    inviteCode: String = "",
    val currentUserId: String = ""
) {
    var groupName by mutableStateOf(groupName)
    var inviteCode by mutableStateOf(inviteCode)

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
        expenses.add(expense)
        val payerName = memberName(expense.paidBy)
        activity.add(
            ActivityEvent(
                kind = ActivityKind.EXPENSE,
                message = "$payerName paid ₹${expense.amount} for ${expense.description}."
            )
        )
        activity.add(
            ActivityEvent(
                kind = ActivityKind.SPLIT,
                message = "Expense split ${expense.splitType.label} among ${expense.shares.size} members."
            )
        )
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
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val result = com.example.splitbill.data.repository.GroupRepository().getGroup(groupId)
            result.onSuccess { group ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    groupName = group.groupName
                    inviteCode = group.joinCode
                    members.clear()
                    group.members.forEach { memberId ->
                        val name = if (memberId == currentUserId) "You" else "User ${memberId.take(4)}"
                        members.add(Member(id = memberId, name = name))
                    }
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
