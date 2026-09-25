package com.faceattend.app.ui.admin.addstaff

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceattend.app.R
import com.faceattend.app.data.repository.StaffRepository
import kotlinx.coroutines.launch

sealed interface AddStaffState {
    data object Idle : AddStaffState
    data object Saved : AddStaffState
    data class FieldError(@StringRes val message: Int) : AddStaffState
    data class Failure(val message: String) : AddStaffState
}

class AddStaffViewModel(private val staffRepository: StaffRepository) : ViewModel() {

    private val _state = MutableLiveData<AddStaffState>(AddStaffState.Idle)
    val state: LiveData<AddStaffState> = _state

    fun save(name: String, employeeId: String) {
        val trimmedName = name.trim()
        val trimmedId = employeeId.trim()

        if (trimmedName.isEmpty()) {
            _state.value = AddStaffState.FieldError(R.string.error_name_required)
            return
        }
        if (trimmedId.isEmpty()) {
            _state.value = AddStaffState.FieldError(R.string.error_employee_id_required)
            return
        }

        viewModelScope.launch {
            _state.value = staffRepository.addStaff(trimmedName, trimmedId).fold(
                onSuccess = { AddStaffState.Saved },
                onFailure = { AddStaffState.Failure(it.message.orEmpty()) }
            )
        }
    }

    fun onStateHandled() {
        _state.value = AddStaffState.Idle
    }
}
