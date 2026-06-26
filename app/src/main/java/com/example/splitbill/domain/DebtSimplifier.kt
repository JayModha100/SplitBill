package com.example.splitbill.domain

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import kotlin.math.abs
import kotlin.math.min

/**
 * Greedy debt simplification.
 *
 * Implements the approach described for SplitBill:
 *   1. Compute every member's net balance after all expenses.
 *   2. Split members into creditors (should receive) and debtors (owe).
 *   3. Sort both by magnitude.
 *   4. Repeatedly match the largest debtor with the largest creditor and
 *      transfer the maximum possible amount.
 *   5. Continue until every balance is (effectively) zero.
 *
 * This minimises the number of settlement transactions.
 */
object DebtSimplifier {

    /** Amounts within this many currency units are treated as settled. */
    private const val EPSILON = 0.01

    /**
     * Net balance per member id.
     * Positive => the group owes this member (creditor).
     * Negative => this member owes the group (debtor).
     */
    fun computeBalances(members: List<Member>, expenses: List<Expense>): Map<String, Double> {
        val balances = members.associate { it.id to 0.0 }.toMutableMap()
        for (expense in expenses) {
            // The payer fronted the full amount, so they are owed it.
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0.0) + expense.amount
            // Each participant owes their resolved share.
            for ((memberId, share) in expense.shares) {
                balances[memberId] = (balances[memberId] ?: 0.0) - share
            }
        }
        return balances
    }

    /**
     * Produce the minimal set of [Settlement]s that clears all balances.
     * Each settlement is rounded to 2 decimal places.
     */
    fun simplify(members: List<Member>, expenses: List<Expense>): List<Settlement> {
        val balances = computeBalances(members, expenses)
        return simplifyFromBalances(balances)
    }

    /** Overload usable directly from precomputed balances (and from unit tests). */
    fun simplifyFromBalances(balances: Map<String, Double>): List<Settlement> {
        // Mutable working lists, largest magnitude first.
        val creditors = balances
            .filter { it.value > EPSILON }
            .map { MutableEntry(it.key, it.value) }
            .sortedByDescending { it.amount }
            .toMutableList()
        val debtors = balances
            .filter { it.value < -EPSILON }
            .map { MutableEntry(it.key, -it.value) } // store owed amount as positive
            .sortedByDescending { it.amount }
            .toMutableList()

        val settlements = mutableListOf<Settlement>()

        var ci = 0
        var di = 0
        while (ci < creditors.size && di < debtors.size) {
            val creditor = creditors[ci]
            val debtor = debtors[di]
            val transfer = min(creditor.amount, debtor.amount)

            if (transfer > EPSILON) {
                settlements.add(
                    Settlement(
                        fromMemberId = debtor.id,
                        toMemberId = creditor.id,
                        amount = round2(transfer)
                    )
                )
                creditor.amount -= transfer
                debtor.amount -= transfer
            }

            if (creditor.amount <= EPSILON) ci++
            if (debtor.amount <= EPSILON) di++
        }

        return settlements
    }

    private fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0

    private class MutableEntry(val id: String, var amount: Double)
}

/** True if a balance map is settled (every entry within tolerance of zero). */
fun Map<String, Double>.isSettled(epsilon: Double = 0.01): Boolean =
    values.all { abs(it) <= epsilon }
