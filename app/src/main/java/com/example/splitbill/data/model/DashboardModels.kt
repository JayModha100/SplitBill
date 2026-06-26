package com.example.splitbill.data.model

import java.util.UUID

/**
 * In-memory domain models that back the group dashboard, the Pay flow and the
 * Settle Up flow. These are intentionally decoupled from the Firestore [Group]
 * model so the UI, the debt simplification algorithm and the settlement flows
 * are fully functional and demoable now. Each model maps cleanly onto a future
 * Firestore subcollection (members / expenses / activity), so swapping the
 * in-memory state for a repository later is a localised change.
 */

/** A person in the group. [balance] is positive if the group owes them money. */
data class Member(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatarColor: Long = 0xFF245EDC,
    val online: Boolean = false,
    /** UPI virtual payment address, e.g. "jay@okhdfcbank". Optional. */
    val upiId: String? = null
)

/** How an expense is divided between members. */
enum class SplitType(val label: String) {
    EQUAL("Equal"),
    EXACT("Exact Amount"),
    PERCENTAGE("Percentage"),
    SHARES("Shares")
}

/**
 * A single expense paid by one member on behalf of some participants.
 *
 * [shares] maps a member id to that member's owed portion of [amount], already
 * resolved from the chosen [splitType]. Keeping the resolved per-member amounts
 * on the expense means balance calculation stays trivial and split-type logic
 * lives in one place (the Pay flow), not scattered across the app.
 */
data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val category: String,
    val amount: Double,
    val paidBy: String, // Member id
    val splitType: SplitType,
    val shares: Map<String, Double>,
    val notes: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

/** The kind of event shown in the activity timeline, used to pick an icon/tint. */
enum class ActivityKind {
    WELCOME, MEMBER_JOINED, EXPENSE, SPLIT, SETTLEMENT, REQUEST
}

/** A chronological entry in the group's activity feed. */
data class ActivityEvent(
    val id: String = UUID.randomUUID().toString(),
    val kind: ActivityKind,
    val message: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * A single "X pays Y amount" instruction produced by the debt simplifier.
 * The dashboard turns these into the Pay / Notify cards.
 */
data class Settlement(
    val fromMemberId: String,
    val toMemberId: String,
    val amount: Double
)
