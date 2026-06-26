package com.example.splitbill

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Member
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
        
        val amount = 100.0
        val shares = SplitCalculator.equal(amount, members.map { it.id })
        
        val expense = Expense(
            amount = amount,
            paidBy = "user1",
            splitType = SplitType.EQUAL,
            shares = shares
        )
        
        val balances = DebtSimplifier.computeBalances(members, listOf(expense))
        
        val netBalance = balances.values.sum()
        assertEquals(0.0, netBalance, 0.001)
        
        assertEquals(66.66, balances["user1"]!!, 0.001)
        assertEquals(-33.33, balances["user2"]!!, 0.001)
        assertEquals(-33.33, balances["user3"]!!, 0.001)
    }
}
