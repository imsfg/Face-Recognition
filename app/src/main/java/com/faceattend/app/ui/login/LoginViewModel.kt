package com.faceattend.app.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceattend.app.R
import com.faceattend.app.data.Credentials
import com.faceattend.app.data.repository.StaffRepository
import com.faceattend.app.util.SessionManager
import kotlinx.coroutines.launch

sealed interface LoginState {
    data object Idle : LoginState
    data object AdminSignedIn : LoginState
    data class StaffSignedIn(val staffId: Long) : LoginState
    data class Error(@StringRes val message: Int) : LoginState
}

class LoginViewModel(
    private val staffRepository: StaffRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    fun signIn(username: String, password: String) {
        val user = username.trim()
        if (user.isEmpty() || password.isEmpty()) {
            _state.value = LoginState.Error(R.string.error_empty_fields)
            return
        }

        if (user.equals(Credentials.ADMIN_USERNAME, ignoreCase = true)) {
            if (password == Credentials.ADMIN_PASSWORD) {
                sessionManager.signInAsAdmin()
                _state.value = LoginState.AdminSignedIn
            } else {
                _state.value = LoginState.Error(R.string.error_invalid_credentials)
            }
            return
        }

        viewModelScope.launch {
            val staff = staffRepository.getByEmployeeId(user)
            _state.value = if (staff != null && password == Credentials.STAFF_PASSWORD) {
                sessionManager.signInAsStaff(staff.id)
                LoginState.StaffSignedIn(staff.id)
            } else {
                LoginState.Error(R.string.error_invalid_credentials)
            }
        }
    }

    /** Clears one-shot navigation/error state so it is not replayed on rotation. */
    fun onStateHandled() {
        _state.value = LoginState.Idle
    }
}
