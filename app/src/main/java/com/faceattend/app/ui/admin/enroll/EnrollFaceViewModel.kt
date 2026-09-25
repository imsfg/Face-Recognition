package com.faceattend.app.ui.admin.enroll

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceattend.app.data.repository.StaffRepository
import com.faceattend.app.face.FaceRecognizer
import com.faceattend.app.face.FaceResult
import com.faceattend.app.util.ImageFileStore
import kotlinx.coroutines.launch

sealed interface EnrollState {
    data object Idle : EnrollState
    data object Saved : EnrollState
    data object NoFaceFound : EnrollState
    data object Failed : EnrollState
}

class EnrollFaceViewModel(
    private val staffRepository: StaffRepository,
    private val faceRecognizer: FaceRecognizer,
    private val imageFileStore: ImageFileStore,
    private val staffId: Long
) : ViewModel() {

    private val _state = MutableLiveData<EnrollState>(EnrollState.Idle)
    val state: LiveData<EnrollState> = _state

    fun enroll(selfie: Bitmap) {
        viewModelScope.launch {
            _state.value = when (val result = faceRecognizer.analyse(selfie)) {
                is FaceResult.Success -> {
                    val path = imageFileStore.saveJpeg(result.faceCrop, ImageFileStore.DIR_ENROLLMENT)
                    staffRepository.saveEnrollment(staffId, path, result.embedding)
                    EnrollState.Saved
                }

                FaceResult.NoFaceFound -> EnrollState.NoFaceFound
                is FaceResult.Failed -> EnrollState.Failed
            }
        }
    }

    fun onStateHandled() {
        _state.value = EnrollState.Idle
    }
}
