package com.faceattend.app.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.faceattend.app.data.entities.Staff
import com.faceattend.app.data.repository.StaffRepository

/** Observes a single staff member — used by the admin profile and the staff home screen. */
class StaffDetailViewModel(
    staffRepository: StaffRepository,
    staffId: Long
) : ViewModel() {
    val staff: LiveData<Staff?> = staffRepository.observeById(staffId).asLiveData()
}
