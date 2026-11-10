package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CloudDesignProject
import com.example.data.model.UserProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud Firestore.")
    }

    suspend fun syncUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val path = docRef.path

        try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "email" to (user.email ?: ""),
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString(),
                    "projectCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updatePayload = mapOf(
                    "displayName" to user.displayName,
                    "photoUrl" to user.photoUrl?.toString(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeProjects(): Flow<List<CloudDesignProject>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/projects"
        emitAll(
            db.collection("users").document(uid).collection("projects")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(CloudDesignProject::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveProject(
        title: String,
        canvasWidth: Int,
        canvasHeight: Int,
        presetName: String,
        backgroundColor: Long?,
        layerCount: Int,
        layersJson: String,
        existingId: String? = null
    ): String {
        val uid = requireUserId()
        val projectsCol = db.collection("users").document(uid).collection("projects")
        val docRef = if (!existingId.isNullOrBlank()) {
            projectsCol.document(existingId)
        } else {
            projectsCol.document()
        }
        val path = docRef.path

        try {
            if (!existingId.isNullOrBlank()) {
                // Update
                val payload = mutableMapOf<String, Any?>(
                    "title" to title,
                    "canvasWidth" to canvasWidth.toLong(),
                    "canvasHeight" to canvasHeight.toLong(),
                    "presetName" to presetName,
                    "backgroundColor" to backgroundColor,
                    "layerCount" to layerCount.toLong(),
                    "layersJson" to layersJson,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(payload).await()
            } else {
                // Create
                val payload = mutableMapOf<String, Any?>(
                    "userId" to uid,
                    "title" to title,
                    "canvasWidth" to canvasWidth.toLong(),
                    "canvasHeight" to canvasHeight.toLong(),
                    "presetName" to presetName,
                    "backgroundColor" to backgroundColor,
                    "layerCount" to layerCount.toLong(),
                    "layersJson" to layersJson,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()

                // Increment user project count
                db.collection("users").document(uid).update(
                    mapOf(
                        "projectCount" to FieldValue.increment(1),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (existingId != null) OperationType.UPDATE else OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deleteProject(projectId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("projects").document(projectId)
        val path = docRef.path
        try {
            docRef.delete().await()
            db.collection("users").document(uid).update(
                mapOf(
                    "projectCount" to FieldValue.increment(-1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
