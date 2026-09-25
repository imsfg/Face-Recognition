package com.faceattend.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

const val RESULT_MATCHED = "MATCHED"
const val RESULT_NOT_MATCHED = "NOT_MATCHED"

@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Staff::class,
            parentColumns = ["id"],
            childColumns = ["staffId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("staffId")]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val timestamp: Long,
    val selfieImagePath: String,
    val latitude: Double?,
    val longitude: Double?,
    val matchSimilarity: Float,
    val result: String
)
