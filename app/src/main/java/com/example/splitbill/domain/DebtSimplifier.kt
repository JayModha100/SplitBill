package com.example.splitbill.domain

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import kotlin.math.abs
import kotlin.math.min

object DebtSimplifier {
    fun computeBalances(members: List<Member>, expenses: List<Expense>, settlements: List<Settlement>): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()
        members.forEach { balances[it.id] = 0L }

        expenses.forEach { expense ->
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0L) + expense.amountPaise
            expense.sharesPaise.forEach { (memberId, portion) ->
                balances[memberId] = (balances[memberId] ?: 0L) - portion
            }
        }

        settlements.forEach { settlement ->
            balances[settlement.fromMemberId] = (balances[settlement.fromMemberId] ?: 0L) + settlement.amountPaise
            balances[settlement.toMemberId] = (balances[settlement.toMemberId] ?: 0L) - settlement.amountPaise
        }

        return balances
    }

    fun computeBalances(members: List<Member>, expenses: List<Expense>): Map<String, Long> {
        return computeBalances(members, expenses, emptyList())
    }

    fun simplifyFromBalances(balances: Map<String, Long>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()
        
        val debtors = balances.filterValues { it < 0L }.mapValues { abs(it.value) }.toMutableMap()
        val creditors = balances.filterValues { it > 0L }.toMutableMap()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.maxByOrNull { it.value }!!
            val creditor = creditors.maxByOrNull { it.value }!!

            val minAmountPaise = min(debtor.value, creditor.value)

            if (minAmountPaise > 0L) {
                settlements.add(Settlement(debtor.key, creditor.key, minAmountPaise))
            }

            debtors[debtor.key] = debtor.value - minAmountPaise
            creditors[creditor.key] = creditor.value - minAmountPaise

            if (debtors[debtor.key]!! == 0L) debtors.remove(debtor.key)
            if (creditors[creditor.key]!! == 0L) creditors.remove(creditor.key)
        }

        return settlements
    }

    fun simplify(members: List<Member>, expenses: List<Expense>): List<Settlement> {
        val balances = computeBalances(members, expenses)
        return simplifyFromBalances(balances)
    }

    fun Map<String, Long>.isSettled(): Boolean {
        return this.values.all { it == 0L }
    }
}
