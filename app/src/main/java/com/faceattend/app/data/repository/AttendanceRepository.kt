package com.faceattend.app.data.repository

import com.faceattend.app.data.db.AttendanceDao
import com.faceattend.app.data.entities.AttendanceRecord
import com.faceattend.app.data.entities.RESULT_MATCHED
import com.faceattend.app.data.entities.RESULT_NOT_MATCHED
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    fun observeForStaff(staffId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.observeForStaff(staffId)

    suspend fun getById(id: Long): AttendanceRecord? = attendanceDao.getById(id)

    suspend fun record(
        staffId: Long,
        selfieImagePath: String,
        latitude: Double?,
        longitude: Double?,
        similarity: Float,
        matched: Boolean
    ): AttendanceRecord {
        val record = AttendanceRecord(
            staffId = staffId,
            timestamp = System.currentTimeMillis(),
            selfieImagePath = selfieImagePath,
            latitude = latitude,
            longitude = longitude,
            matchSimilarity = similarity,
            result = if (matched) RESULT_MATCHED else RESULT_NOT_MATCHED
        )
        val id = attendanceDao.insert(record)
        return record.copy(id = id)
    }
}
