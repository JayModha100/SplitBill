package com.example.splitbill

import com.example.splitbill.domain.SplitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun equal_withPennyRemainder_distributesCorrectly() {
        val members = listOf("user1", "user2", "user3")
        val amountPaise = 10000L
        
        val shares = SplitCalculator.equal(amountPaise, members)
        
        // 10000 / 3 = 3333
        // 3333 * 3 = 9999
        // remainder = 1, goes to first user.
        assertEquals(3334L, shares["user1"]!!)
        assertEquals(3333L, shares["user2"]!!)
        assertEquals(3333L, shares["user3"]!!)
        
        val sum = shares.values.sum()
        assertEquals(amountPaise, sum)
    }

    @Test
    fun exact_sumsCorrectly_returnsMap() {
        val amountsPaise = mapOf("user1" to 5000L, "user2" to 5000L)
        val shares = SplitCalculator.exact(10000L, amountsPaise)
        assertEquals(5000L, shares["user1"]!!)
    }

    @Test
    fun exact_doesNotSumToTotal_throwsIllegalArgumentException() {
        val amountsPaise = mapOf("user1" to 4000L, "user2" to 5000L)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.exact(10000L, amountsPaise)
        }
    }

    @Test
    fun percentage_doesNotSumTo100_throwsIllegalArgumentException() {
        val percentages = mapOf("user1" to 50.0, "user2" to 40.0)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.percentage(10000L, percentages)
        }
    }

    @Test
    fun shares_withZeroWeights_throwsIllegalArgumentException() {
        val weights = mapOf("user1" to 0, "user2" to 0)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.shares(10000L, weights)
        }
    }
}
