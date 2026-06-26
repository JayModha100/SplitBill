package com.example.splitbill.domain

import kotlin.math.abs
import kotlin.math.roundToInt

object SplitCalculator {

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    fun equal(amount: Double, memberIds: List<String>): Map<String, Double> {
        if (memberIds.isEmpty()) return emptyMap()
        if (amount < 0.0) throw IllegalArgumentException("Amount must not be negative.")

        val share = roundToTwoDecimals(amount / memberIds.size)
        val result = mutableMapOf<String, Double>()
        
        for (id in memberIds) {
            result[id] = share
        }

        val totalWithoutAdjust = share * memberIds.size
        val remainder = roundToTwoDecimals(amount - totalWithoutAdjust)
        
        if (remainder != 0.0) {
            val firstMember = memberIds.first()
            result[firstMember] = roundToTwoDecimals(result[firstMember]!! + remainder)
        }

        return result
    }

    fun exact(amount: Double, exactAmounts: Map<String, Double>): Map<String, Double> {
        val sum = exactAmounts.values.sum()
        if (abs(sum - amount) > 0.01) {
            throw IllegalArgumentException("Exact amounts must sum to the total amount (expected $amount, got $sum).")
        }
        return exactAmounts
    }

    fun percentage(amount: Double, percents: Map<String, Double>): Map<String, Double> {
        val sumPercents = percents.values.sum()
        if (abs(sumPercents - 100.0) > 0.01) {
            throw IllegalArgumentException("Percentages must sum to 100 (got $sumPercents).")
        }

        val result = mutableMapOf<String, Double>()
        var sum = 0.0

        for ((id, percent) in percents) {
            val share = roundToTwoDecimals((amount * percent) / 100.0)
            result[id] = share
            sum += share
        }

        if (result.isNotEmpty()) {
            val remainder = roundToTwoDecimals(amount - sum)
            if (remainder != 0.0) {
                val firstMember = percents.keys.first()
                result[firstMember] = roundToTwoDecimals(result[firstMember]!! + remainder)
            }
        }

        return result
    }

    fun shares(amount: Double, weights: Map<String, Int>): Map<String, Double> {
        val totalWeight = weights.values.sum()
        if (totalWeight <= 0) {
            throw IllegalArgumentException("Total share weights must be greater than zero.")
        }

        val result = mutableMapOf<String, Double>()
        var sum = 0.0

        for ((id, weight) in weights) {
            val share = roundToTwoDecimals((amount * weight.toDouble()) / totalWeight)
            result[id] = share
            sum += share
        }

        if (result.isNotEmpty()) {
            val remainder = roundToTwoDecimals(amount - sum)
            if (remainder != 0.0) {
                val firstMember = weights.keys.first()
                result[firstMember] = roundToTwoDecimals(result[firstMember]!! + remainder)
            }
        }

        return result
    }
}
