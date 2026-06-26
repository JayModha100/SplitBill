package com.example.splitbill.data.repository

import com.example.splitbill.data.model.Group
import com.example.splitbill.data.model.JoinGroupResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class GroupRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun createGroup(groupName: String, currentUserId: String): Result<Group> {
        return try {
            var uniqueJoinCode: String? = null
            for (i in 1..5) {
                val code = generateJoinCode()
                val snapshot = withTimeout(10000L) {
                    groupsCollection.whereEqualTo("joinCode", code).limit(1).get().await()
                }
                if (snapshot.isEmpty) {
                    uniqueJoinCode = code
                    break
                }
            }

            if (uniqueJoinCode == null) {
                return Result.failure(Exception("Failed to generate a unique join code after multiple attempts. Please try again."))
            }

            val newGroup = Group(
                groupId = "",
                groupName = groupName,
                joinCode = uniqueJoinCode,
                createdBy = currentUserId,
                members = listOf(currentUserId)
            )

            val documentRef = groupsCollection.document()
            val groupToSave = newGroup.copy(groupId = documentRef.id)

            withTimeout(10000L) {
                documentRef.set(groupToSave).await()
            }

            Result.success(groupToSave)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(joinCode: String, currentUserId: String): Result<JoinGroupResult> {
        return try {
            val normalizedCode = joinCode.trim().uppercase()

            if (normalizedCode.length != 6) {
                return Result.failure(Exception("Join code must be exactly 6 characters."))
            }

            val querySnapshot = withTimeout(10000L) {
                groupsCollection
                    .whereEqualTo("joinCode", normalizedCode)
                    .limit(1)
                    .get()
                    .await()
            }

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("No group found with code \"$normalizedCode\"."))
            }

            val document = querySnapshot.documents.first()
            val group = document.toObject(Group::class.java)
                ?: return Result.failure(Exception("Failed to parse group data."))

            if (group.members.contains(currentUserId)) {
                return Result.success(
                    JoinGroupResult(
                        groupId = group.groupId,
                        groupName = group.groupName,
                        alreadyMember = true
                    )
                )
            }

            // arrayUnion is idempotent but we skip the write entirely if already a member
            withTimeout(10000L) {
                document.reference.update("members", FieldValue.arrayUnion(currentUserId)).await()
            }

            Result.success(
                JoinGroupResult(
                    groupId = group.groupId,
                    groupName = group.groupName,
                    alreadyMember = false
                )
            )
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroup(groupId: String): Result<Group> {
        return try {
            val snapshot = withTimeout(10000L) {
                groupsCollection.document(groupId).get().await()
            }
            val group = snapshot.toObject(Group::class.java)
            if (group != null) {
                Result.success(group)
            } else {
                Result.failure(Exception("Group not found."))
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

