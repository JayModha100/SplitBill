package com.example.splitbill

import com.example.splitbill.domain.SplitCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun equal_withPennyRemainder_distributesCorrectly() {
        val members = listOf("user1", "user2", "user3")
        val amount = 100.0
        
        val shares = SplitCalculator.equal(amount, members)
        
        // 100.0 / 3 = 33.33...
        // 33.33 * 3 = 99.99
        // remainder = 0.01, goes to first user.
        assertEquals(33.34, shares["user1"]!!, 0.001)
        assertEquals(33.33, shares["user2"]!!, 0.001)
        assertEquals(33.33, shares["user3"]!!, 0.001)
        
        val sum = shares.values.sum()
        assertEquals(amount, sum, 0.001)
    }

    @Test
    fun exact_sumsCorrectly_returnsMap() {
        val amounts = mapOf("user1" to 50.0, "user2" to 50.0)
        val shares = SplitCalculator.exact(100.0, amounts)
        assertEquals(50.0, shares["user1"]!!, 0.001)
    }

    @Test
    fun exact_doesNotSumToTotal_throwsIllegalArgumentException() {
        val amounts = mapOf("user1" to 40.0, "user2" to 50.0)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.exact(100.0, amounts)
        }
    }

    @Test
    fun percentage_doesNotSumTo100_throwsIllegalArgumentException() {
        val percentages = mapOf("user1" to 50.0, "user2" to 40.0)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.percentage(100.0, percentages)
        }
    }

    @Test
    fun shares_withZeroWeights_throwsIllegalArgumentException() {
        val weights = mapOf("user1" to 0, "user2" to 0)
        
        assertThrows(IllegalArgumentException::class.java) {
            SplitCalculator.shares(100.0, weights)
        }
    }
}
