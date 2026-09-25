package com.faceattend.app.ui.admin.stafflist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.faceattend.app.data.entities.Staff
import com.faceattend.app.data.repository.StaffRepository

class StaffListViewModel(staffRepository: StaffRepository) : ViewModel() {
    val staff: LiveData<List<Staff>> = staffRepository.observeAll().asLiveData()
}
