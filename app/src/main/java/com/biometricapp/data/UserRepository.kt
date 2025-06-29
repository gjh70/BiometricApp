package com.biometricapp.data
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    suspend fun registerUser(user: User): Result<User> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
    suspend fun getUser(id: String): Result<User?> {
        return try {
            val snapshot = usersCollection.document(id).get().await()
            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }
}