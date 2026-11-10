package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String? = null,
    val email: String = "",
    val photoUrl: String? = null,
    val projectCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudDesignProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val canvasWidth: Long = 1080L,
    val canvasHeight: Long = 1080L,
    val presetName: String = "Square 1:1",
    val backgroundColor: Long? = null,
    val layerCount: Long = 0L,
    val layersJson: String = "[]",
    val thumbnailBase64: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
