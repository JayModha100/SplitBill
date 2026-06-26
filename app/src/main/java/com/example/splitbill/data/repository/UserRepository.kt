package com.example.splitbill.data.repository

import com.example.splitbill.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun upsertProfile(profile: UserProfile): Result<UserProfile> {
        return try {
            withTimeout(10000L) {
                usersCollection.document(profile.uid).set(profile).await()
            }
            Result.success(profile)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(uid: String): Result<UserProfile> {
        return try {
            val snapshot = withTimeout(10000L) {
                usersCollection.document(uid).get().await()
            }
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Failed to parse user profile."))
                }
            } else {
                Result.failure(Exception("User profile not found."))
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfiles(uids: List<String>): Result<List<UserProfile>> {
        return try {
            if (uids.isEmpty()) return Result.success(emptyList())

            val profiles = mutableListOf<UserProfile>()
            val chunks = uids.chunked(10) // Firestore 'in' query limit is 10

            withTimeout(10000L) {
                for (chunk in chunks) {
                    val snapshot = usersCollection.whereIn("uid", chunk).get().await()
                    profiles.addAll(snapshot.toObjects(UserProfile::class.java))
                }
            }
            Result.success(profiles)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Database connection timed out. Please check your internet or ensure Firestore is enabled in the Firebase Console."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
