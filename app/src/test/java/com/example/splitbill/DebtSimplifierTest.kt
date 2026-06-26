package com.example.splitbill

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
import com.example.splitbill.data.model.Settlement
import com.example.splitbill.data.model.SplitType
import com.example.splitbill.domain.DebtSimplifier
import com.example.splitbill.domain.SplitCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class DebtSimplifierTest {

    @Test
    fun computeBalances_netsToZero_whenSharesSumToAmount() {
        val members = listOf(
            Member(id = "user1", name = "User 1"),
            Member(id = "user2", name = "User 2"),
            Member(id = "user3", name = "User 3")
        )
        
        val amountPaise = 10000L
        val sharesPaise = SplitCalculator.equal(amountPaise, members.map { it.id })
        
        val expense = Expense(
            amountPaise = amountPaise,
            paidBy = "user1",
            splitType = SplitType.EQUAL,
            sharesPaise = sharesPaise
        )
        
        val balances = DebtSimplifier.computeBalances(members, listOf(expense))
        
        val netBalance = balances.values.sum()
        assertEquals(0L, netBalance)
        
        assertEquals(6666L, balances["user1"]!!)
        assertEquals(-3333L, balances["user2"]!!)
        assertEquals(-3333L, balances["user3"]!!)
    }

    @Test
    fun computeBalances_withConfirmingSettlement_netsToZero() {
        val members = listOf(
            Member(id = "user1", name = "User 1"),
            Member(id = "user2", name = "User 2")
        )

        val expense = Expense(
            amountPaise = 10000L,
            paidBy = "user1",
            splitType = SplitType.EQUAL,
            sharesPaise = mapOf("user1" to 5000L, "user2" to 5000L)
        )

        // user2 owes user1 5000 paise. So user2 sends user1 5000 paise.
        val settlement = Settlement(
            fromMemberId = "user2",
            toMemberId = "user1",
            amountPaise = 5000L
        )

        val balances = DebtSimplifier.computeBalances(members, listOf(expense), listOf(settlement))

        assertEquals(0L, balances["user1"]!!)
        assertEquals(0L, balances["user2"]!!)
    }
}
