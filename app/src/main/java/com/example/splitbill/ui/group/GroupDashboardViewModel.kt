package com.example.splitbill.ui.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitbill.data.model.ActivityEvent
import com.example.splitbill.data.model.ActivityKind
import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import com.example.splitbill.data.model.SplitType
import com.example.splitbill.data.repository.ExpenseRepository
import com.example.splitbill.data.repository.GroupRepository
import com.example.splitbill.data.repository.UserRepository
import com.example.splitbill.domain.DebtSimplifier
import com.example.splitbill.domain.Money
import kotlinx.coroutines.launch
import java.util.UUID

class GroupDashboardViewModel(
    val currentUserId: String = "",
    private val expenseRepository: ExpenseRepository = ExpenseRepository(),
    private val groupRepository: GroupRepository = GroupRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    var groupName by mutableStateOf("")
    var inviteCode by mutableStateOf("")

    private var currentGroupId: String = ""

    val members = mutableStateListOf<Member>()
    val expenses = mutableStateListOf<Expense>()
    val settlements = mutableStateListOf<Settlement>()
    val activity = mutableStateListOf<ActivityEvent>()

    fun memberName(id: String): String {
        return members.find { it.id == id }?.name ?: "Unknown"
    }

    fun balances(): Map<String, Long> {
        return DebtSimplifier.computeBalances(members, expenses, settlements)
    }

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            val expenseToSave = expense.copy(groupId = currentGroupId)
            val result = expenseRepository.addExpense(expenseToSave)
            result.onSuccess { savedExpense ->
                expenses.add(savedExpense)
                val payerName = memberName(savedExpense.paidBy)
                activity.add(
                    ActivityEvent(
                        kind = ActivityKind.EXPENSE,
                        message = "$payerName paid ${Money.formatPaise(savedExpense.amountPaise)} for ${savedExpense.description}."
                    )
                )
                activity.add(
                    ActivityEvent(
                        kind = ActivityKind.SPLIT,
                        message = "Expense split ${savedExpense.splitType.label} among ${savedExpense.sharesPaise.size} members."
                    )
                )
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    private fun seedExpenseLocally(expense: Expense) {
        expenses.add(expense)
        val payerName = memberName(expense.paidBy)
        activity.add(
            ActivityEvent(
                kind = ActivityKind.EXPENSE,
                message = "$payerName paid ${Money.formatPaise(expense.amountPaise)} for ${expense.description}."
            )
        )
        activity.add(
            ActivityEvent(
                kind = ActivityKind.SPLIT,
                message = "Expense split ${expense.splitType.label} among ${expense.sharesPaise.size} members."
            )
        )
    }

    fun recordSettlement(settlement: Settlement) {
        viewModelScope.launch {
            val settlementToSave = settlement.copy(groupId = currentGroupId, confirmed = true)
            val result = expenseRepository.addSettlement(settlementToSave)
            result.onSuccess { savedSettlement ->
                settlements.add(savedSettlement)
                val fromName = memberName(savedSettlement.fromMemberId)
                val toName = memberName(savedSettlement.toMemberId)
                activity.add(
                    ActivityEvent(
                        kind = ActivityKind.SETTLEMENT,
                        message = "$fromName settled ${Money.formatPaise(savedSettlement.amountPaise)} with $toName."
                    )
                )
            }.onFailure { e ->
                e.printStackTrace()
            }
        }
    }

    fun requestPayment(fromMemberId: String, toMemberId: String, amountPaise: Long) {
        val fromName = memberName(fromMemberId)
        val toName = memberName(toMemberId)
        activity.add(
            ActivityEvent(
                kind = ActivityKind.REQUEST,
                message = "$toName requested ${Money.formatPaise(amountPaise)} from $fromName."
            )
        )
    }

    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        viewModelScope.launch {
            val result = groupRepository.getGroup(groupId)
            val expensesResult = expenseRepository.getExpenses(groupId)
            val settlementsResult = expenseRepository.getSettlements(groupId)
            
            result.onSuccess { group ->
                val profilesResult = userRepository.getProfiles(group.members)
                val profilesMap = profilesResult.getOrNull()?.associateBy { it.uid } ?: emptyMap()

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

            expensesResult.onSuccess { loadedExpenses ->
                expenses.clear()
                expenses.addAll(loadedExpenses)
            }
            settlementsResult.onSuccess { loadedSettlements ->
                settlements.clear()
                settlements.addAll(loadedSettlements)
            }
        }
    }

    companion object {
        fun demo(): GroupDashboardViewModel {
            val jayId = UUID.randomUUID().toString()
            val aryanId = UUID.randomUUID().toString()
            val neelId = UUID.randomUUID().toString()
            val riyaId = UUID.randomUUID().toString()

            val vm = GroupDashboardViewModel(currentUserId = jayId)
            
            vm.groupName = "Goa Trip"
            vm.inviteCode = "GOA123"

            vm.members.apply {
                add(Member(id = jayId, name = "Jay (You)", online = true))
                add(Member(id = aryanId, name = "Aryan", avatarColor = 0xFF4CAF50, online = true))
                add(Member(id = neelId, name = "Neel", avatarColor = 0xFFC62828, online = false))
                add(Member(id = riyaId, name = "Riya", avatarColor = 0xFF9E9E9E, online = true))
            }

            vm.activity.add(
                ActivityEvent(
                    kind = ActivityKind.WELCOME,
                    message = "Welcome to Goa Trip, Jay!"
                )
            )
            vm.activity.add(
                ActivityEvent(
                    kind = ActivityKind.MEMBER_JOINED,
                    message = "Aryan joined the group."
                )
            )

            vm.seedExpenseLocally(
                Expense(
                    description = "Lunch",
                    category = "Food",
                    amountPaise = 75000L,
                    paidBy = jayId,
                    splitType = SplitType.EQUAL,
                    sharesPaise = mapOf(
                        jayId to 18750L,
                        aryanId to 18750L,
                        neelId to 18750L,
                        riyaId to 18750L
                    )
                )
            )

            return vm
        }
    }
}
