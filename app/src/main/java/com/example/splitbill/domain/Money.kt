package com.example.splitbill.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

object Money {
    fun toPaise(rupees: Double): Long {
        return (rupees * 100.0).roundToLong()
    }

    fun toRupees(paise: Long): Double {
        return paise / 100.0
    }

    fun formatPaise(paise: Long): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        numberFormat.minimumFractionDigits = 2
        numberFormat.maximumFractionDigits = 2
        return "₹${numberFormat.format(toRupees(paise))}"
    }
}
