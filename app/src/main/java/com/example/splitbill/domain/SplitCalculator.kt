package com.example.splitbill.domain

import kotlin.math.abs

object SplitCalculator {

    fun equal(amountPaise: Long, memberIds: List<String>): Map<String, Long> {
        if (memberIds.isEmpty()) return emptyMap()
        if (amountPaise < 0L) throw IllegalArgumentException("Amount must not be negative.")

        val share = amountPaise / memberIds.size
        val result = mutableMapOf<String, Long>()
        
        for (id in memberIds) {
            result[id] = share
        }

        val totalWithoutAdjust = share * memberIds.size
        val remainder = amountPaise - totalWithoutAdjust
        
        if (remainder != 0L) {
            val firstMember = memberIds.first()
            result[firstMember] = result[firstMember]!! + remainder
        }

        return result
    }

    fun exact(amountPaise: Long, exactAmountsPaise: Map<String, Long>): Map<String, Long> {
        val sum = exactAmountsPaise.values.sum()
        if (sum != amountPaise) {
            throw IllegalArgumentException("Exact amounts must sum to the total amount (expected $amountPaise, got $sum).")
        }
        return exactAmountsPaise
    }

    fun percentage(amountPaise: Long, percents: Map<String, Double>): Map<String, Long> {
        val sumPercents = percents.values.sum()
        if (abs(sumPercents - 100.0) > 0.01) {
            throw IllegalArgumentException("Percentages must sum to 100 (got $sumPercents).")
        }

        val result = mutableMapOf<String, Long>()
        var sum = 0L

        for ((id, percent) in percents) {
            val share = ((amountPaise * percent) / 100.0).toLong()
            result[id] = share
            sum += share
        }

        if (result.isNotEmpty()) {
            val remainder = amountPaise - sum
            if (remainder != 0L) {
                val firstMember = percents.keys.first()
                result[firstMember] = result[firstMember]!! + remainder
            }
        }

        return result
    }

    fun shares(amountPaise: Long, weights: Map<String, Int>): Map<String, Long> {
        val totalWeight = weights.values.sum()
        if (totalWeight <= 0) {
            throw IllegalArgumentException("Total share weights must be greater than zero.")
        }

        val result = mutableMapOf<String, Long>()
        var sum = 0L

        for ((id, weight) in weights) {
            val share = ((amountPaise * weight.toDouble()) / totalWeight).toLong()
            result[id] = share
            sum += share
        }

        if (result.isNotEmpty()) {
            val remainder = amountPaise - sum
            if (remainder != 0L) {
                val firstMember = weights.keys.first()
                result[firstMember] = result[firstMember]!! + remainder
            }
        }

        return result
    }
}
