package com.faceattend.app.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "staff",
    indices = [Index(value = ["employeeId"], unique = true)]
)
data class Staff(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val name: String,
    val faceImagePath: String? = null,
    val embeddingJson: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val hasEnrolledFace: Boolean get() = embeddingJson != null
}
