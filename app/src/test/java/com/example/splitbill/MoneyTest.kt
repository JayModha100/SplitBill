package com.example.splitbill

import com.example.splitbill.domain.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {

    @Test
    fun toPaise_roundsHalfUp_correctly() {
        assertEquals(1L, Money.toPaise(0.005))
        assertEquals(0L, Money.toPaise(0.004))
        assertEquals(12345L, Money.toPaise(123.45))
        assertEquals(12345L, Money.toPaise(123.451))
        assertEquals(12346L, Money.toPaise(123.456))
    }

    @Test
    fun toRupees_convertsCorrectly() {
        assertEquals(123.45, Money.toRupees(12345L), 0.001)
        assertEquals(0.01, Money.toRupees(1L), 0.001)
    }

    @Test
    fun formatPaise_formatsWithUSDigitsAndRupeeSymbol() {
        assertEquals("₹123.45", Money.formatPaise(12345L))
        assertEquals("₹1,234.50", Money.formatPaise(123450L))
        assertEquals("₹0.01", Money.formatPaise(1L))
    }
}
