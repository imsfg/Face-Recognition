package com.faceattend.app.data.repository

import com.faceattend.app.data.db.StaffDao
import com.faceattend.app.data.entities.Staff
import com.faceattend.app.face.EmbeddingCodec
import kotlinx.coroutines.flow.Flow

class StaffRepository(private val staffDao: StaffDao) {

    fun observeAll(): Flow<List<Staff>> = staffDao.observeAll()

    fun observeById(id: Long): Flow<Staff?> = staffDao.observeById(id)

    suspend fun getById(id: Long): Staff? = staffDao.getById(id)

    suspend fun getByEmployeeId(employeeId: String): Staff? = staffDao.getByEmployeeId(employeeId)

    suspend fun addStaff(name: String, employeeId: String): Result<Long> {
        if (staffDao.getByEmployeeId(employeeId) != null) {
            return Result.failure(DuplicateEmployeeIdException(employeeId))
        }
        return Result.success(staffDao.insert(Staff(employeeId = employeeId, name = name)))
    }

    suspend fun saveEnrollment(staffId: Long, imagePath: String, embedding: FloatArray) {
        staffDao.updateEnrollment(staffId, imagePath, EmbeddingCodec.encode(embedding))
    }

    suspend fun getEnrolledEmbedding(staffId: Long): FloatArray? =
        staffDao.getById(staffId)?.embeddingJson?.let(EmbeddingCodec::decode)
}

class DuplicateEmployeeIdException(employeeId: String) :
    Exception("Employee ID \"$employeeId\" is already in use")
