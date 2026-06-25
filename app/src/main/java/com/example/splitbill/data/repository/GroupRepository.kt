package com.example.splitbill.data.repository

import com.example.splitbill.data.model.Group
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GroupRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    private fun generateJoinCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun createGroup(groupName: String, currentUserId: String): Result<Group> {
        return try {
            val joinCode = generateJoinCode()
            
            val newGroup = Group(
                groupId = "", 
                groupName = groupName,
                joinCode = joinCode,
                createdBy = currentUserId,
                members = listOf(currentUserId)
            )

            val documentRef = groupsCollection.document()
            val groupToSave = newGroup.copy(groupId = documentRef.id)

            documentRef.set(groupToSave).await()

            Result.success(groupToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(joinCode: String, currentUserId: String): Result<Group> {
        return try {
            val querySnapshot = groupsCollection
                .whereEqualTo("joinCode", joinCode)
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Group not found with the provided join code."))
            }

            val document = querySnapshot.documents.first()
            val group = document.toObject(Group::class.java) 
                ?: return Result.failure(Exception("Failed to parse group data."))

            document.reference.update("members", FieldValue.arrayUnion(currentUserId)).await()

            val updatedMembers = if (!group.members.contains(currentUserId)) group.members + currentUserId else group.members
            val updatedGroup = group.copy(members = updatedMembers)
            
            Result.success(updatedGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroup(groupId: String): Result<Group> {
        return try {
            val snapshot = groupsCollection.document(groupId).get().await()
            val group = snapshot.toObject(Group::class.java)
            if (group != null) {
                Result.success(group)
            } else {
                Result.failure(Exception("Group not found."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
