package com.example.splitbill.data.repository

import com.example.splitbill.data.model.Expense
import com.example.splitbill.data.model.Settlement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class ExpenseRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addExpense(expense: Expense): Result<Expense> {
        return try {
            val collectionRef = firestore.collection("groups").document(expense.groupId).collection("expenses")
            val documentRef = collectionRef.document()
            val expenseToSave = expense.copy(id = documentRef.id)

            withTimeout(10000L) {
                documentRef.set(expenseToSave).await()
            }

            Result.success(expenseToSave)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenses(groupId: String): Result<List<Expense>> {
        return try {
            val collectionRef = firestore.collection("groups").document(groupId).collection("expenses")
            
            val snapshot = withTimeout(10000L) {
                collectionRef.orderBy("timestampMillis", Query.Direction.DESCENDING).get().await()
            }
            
            val expenses = snapshot.toObjects(Expense::class.java)
            Result.success(expenses)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSettlement(settlement: Settlement): Result<Settlement> {
        return try {
            val collectionRef = firestore.collection("groups").document(settlement.groupId).collection("settlements")
            val documentRef = collectionRef.document()
            val settlementToSave = settlement.copy(id = documentRef.id)

            withTimeout(10000L) {
                documentRef.set(settlementToSave).await()
            }

            Result.success(settlementToSave)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSettlements(groupId: String): Result<List<Settlement>> {
        return try {
            val collectionRef = firestore.collection("groups").document(groupId).collection("settlements")
            
            val snapshot = withTimeout(10000L) {
                // Not enforcing order on settlements for now since it's not requested explicitly,
                // but usually ordering by timestamp is good if available. 
                // Let's just do a basic get as per instructions.
                collectionRef.get().await()
            }
            
            val settlements = snapshot.toObjects(Settlement::class.java)
            Result.success(settlements)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
