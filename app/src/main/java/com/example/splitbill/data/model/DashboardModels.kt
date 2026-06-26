package com.example.splitbill.data.model

import java.util.UUID

data class Member(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarColor: Long = 0xFF245EDC,
    val online: Boolean = false,
    val upiId: String? = null
)

enum class SplitType(val label: String) {
    EQUAL("Equal"),
    EXACT("Exact Amount"),
    PERCENTAGE("Percentage"),
    SHARES("Shares")
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String = "",
    val description: String = "",
    val category: String = "",
    val amountPaise: Long = 0L,
    val paidBy: String = "",
    val splitType: SplitType = SplitType.EQUAL,
    val sharesPaise: Map<String, Long> = emptyMap(),
    val notes: String = "",
    val timestampMillis: Long = 0L
)

enum class ActivityKind {
    WELCOME, MEMBER_JOINED, EXPENSE, SPLIT, SETTLEMENT, REQUEST
}

data class ActivityEvent(
    val id: String = UUID.randomUUID().toString(),
    val kind: ActivityKind,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class Settlement(
    val fromMemberId: String = "",
    val toMemberId: String = "",
    val amountPaise: Long = 0L,
    val id: String = UUID.randomUUID().toString(),
    val groupId: String = "",
    val confirmed: Boolean = false,
    val timestampMillis: Long = 0L
)
