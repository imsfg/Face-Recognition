package com.faceattend.app.ui.staff.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceattend.app.data.entities.AttendanceRecord
import com.faceattend.app.data.repository.AttendanceRepository
import kotlinx.coroutines.launch

class AttendanceResultViewModel(
    attendanceRepository: AttendanceRepository,
    attendanceId: Long
) : ViewModel() {

    private val _record = MutableLiveData<AttendanceRecord?>()
    val record: LiveData<AttendanceRecord?> = _record

    init {
        viewModelScope.launch { _record.value = attendanceRepository.getById(attendanceId) }
    }
}
