package com.example.splitbill.domain

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round

object DebtSimplifier {
    fun computeBalances(members: List<Member>, expenses: List<Expense>, settlements: List<Settlement>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>()
        members.forEach { balances[it.id] = 0.0 }

        expenses.forEach { expense ->
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            expense.shares.forEach { (memberId, portion) ->
                balances[memberId] = (balances[memberId] ?: 0.0) - portion
            }
        }

        settlements.forEach { settlement ->
            balances[settlement.fromMemberId] = (balances[settlement.fromMemberId] ?: 0.0) + settlement.amount
            balances[settlement.toMemberId] = (balances[settlement.toMemberId] ?: 0.0) - settlement.amount
        }

        return balances
    }

    fun computeBalances(members: List<Member>, expenses: List<Expense>): Map<String, Double> {
        return computeBalances(members, expenses, emptyList())
    }

    fun simplifyFromBalances(balances: Map<String, Double>): List<Settlement> {
        val settlements = mutableListOf<Settlement>()
        
        val debtors = balances.filterValues { it < -0.01 }.mapValues { abs(it.value) }.toMutableMap()
        val creditors = balances.filterValues { it > 0.01 }.toMutableMap()

        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            val debtor = debtors.maxByOrNull { it.value }!!
            val creditor = creditors.maxByOrNull { it.value }!!

            val minAmount = min(debtor.value, creditor.value)
            val roundedAmount = round(minAmount * 100) / 100.0

            if (roundedAmount > 0.0) {
                settlements.add(Settlement(debtor.key, creditor.key, roundedAmount))
            }

            debtors[debtor.key] = debtor.value - minAmount
            creditors[creditor.key] = creditor.value - minAmount

            if (debtors[debtor.key]!! < 0.01) debtors.remove(debtor.key)
            if (creditors[creditor.key]!! < 0.01) creditors.remove(creditor.key)
        }

        return settlements
    }

    fun simplify(members: List<Member>, expenses: List<Expense>): List<Settlement> {
        val balances = computeBalances(members, expenses)
        return simplifyFromBalances(balances)
    }

    fun Map<String, Double>.isSettled(epsilon: Double = 0.01): Boolean {
        return this.values.all { abs(it) < epsilon }
    }
}
