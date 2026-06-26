package com.example.splitbill.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.splitbill.data.repository.ExpenseRepository
import com.example.splitbill.data.repository.GroupRepository

class RecurringExpenseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val uid = CurrentUser.uid() ?: return Result.success()

        val groupRepository = GroupRepository()
        val expenseRepository = ExpenseRepository()

        val groupsResult = groupRepository.getGroupsForUser(uid)
        val groups = groupsResult.getOrNull() ?: return Result.success()

        val nowMillis = System.currentTimeMillis()
        
        val oneDayMillis = 24L * 60 * 60 * 1000
        val oneWeekMillis = 7L * oneDayMillis
        val oneMonthMillis = 30L * oneDayMillis

        for (group in groups) {
            val expensesResult = expenseRepository.getDueRecurringExpenses(group.groupId, nowMillis)
            val dueExpenses = expensesResult.getOrNull() ?: continue

            for (expense in dueExpenses) {
                var currentNextRun = expense.nextRunMillis
                if (currentNextRun == 0L) currentNextRun = nowMillis // fallback

                val periodMillis = when (expense.recurrence) {
                    "DAILY" -> oneDayMillis
                    "WEEKLY" -> oneWeekMillis
                    "MONTHLY" -> oneMonthMillis
                    else -> continue
                }

                while (currentNextRun <= nowMillis) {
                    // Create one-off expense for this missed period
                    val newExpense = expense.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        recurrence = "NONE",
                        nextRunMillis = 0L,
                        timestampMillis = currentNextRun // Use the time it was actually due
                    )
                    expenseRepository.addExpense(newExpense)

                    // Advance to next period
                    currentNextRun += periodMillis
                }

                // Update the original recurring expense template with the new future nextRunMillis
                expenseRepository.updateNextRun(expense, currentNextRun)
            }
        }

        return Result.success()
    }
}
