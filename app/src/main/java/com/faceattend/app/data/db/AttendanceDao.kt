package com.faceattend.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.faceattend.app.data.entities.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insert(record: AttendanceRecord): Long

    @Query("SELECT * FROM attendance WHERE staffId = :staffId ORDER BY timestamp DESC")
    fun observeForStaff(staffId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE id = :id")
    suspend fun getById(id: Long): AttendanceRecord?
}
