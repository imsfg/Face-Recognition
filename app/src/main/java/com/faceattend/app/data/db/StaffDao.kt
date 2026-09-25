package com.faceattend.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.faceattend.app.data.entities.Staff
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Insert
    suspend fun insert(staff: Staff): Long

    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun observeAll(): Flow<List<Staff>>

    @Query("SELECT * FROM staff WHERE id = :id")
    fun observeById(id: Long): Flow<Staff?>

    @Query("SELECT * FROM staff WHERE id = :id")
    suspend fun getById(id: Long): Staff?

    @Query("SELECT * FROM staff WHERE employeeId = :employeeId LIMIT 1")
    suspend fun getByEmployeeId(employeeId: String): Staff?

    @Query("UPDATE staff SET faceImagePath = :imagePath, embeddingJson = :embeddingJson WHERE id = :id")
    suspend fun updateEnrollment(id: Long, imagePath: String, embeddingJson: String)
}
