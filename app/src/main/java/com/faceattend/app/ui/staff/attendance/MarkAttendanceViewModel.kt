package com.faceattend.app.ui.staff.attendance

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faceattend.app.data.repository.AttendanceRepository
import com.faceattend.app.data.repository.StaffRepository
import com.faceattend.app.face.FaceMatcher
import com.faceattend.app.face.FaceRecognizer
import com.faceattend.app.face.FaceResult
import com.faceattend.app.util.ImageFileStore
import com.faceattend.app.util.LocationHelper
import kotlinx.coroutines.launch

sealed interface MarkAttendanceState {
    data object Idle : MarkAttendanceState
    data class Recorded(val attendanceId: Long) : MarkAttendanceState
    data object NotEnrolled : MarkAttendanceState
    data object NoFaceFound : MarkAttendanceState
    data object Failed : MarkAttendanceState
}

class MarkAttendanceViewModel(
    private val staffRepository: StaffRepository,
    private val attendanceRepository: AttendanceRepository,
    private val faceRecognizer: FaceRecognizer,
    private val imageFileStore: ImageFileStore,
    private val locationHelper: LocationHelper,
    private val staffId: Long
) : ViewModel() {

    private val _state = MutableLiveData<MarkAttendanceState>(MarkAttendanceState.Idle)
    val state: LiveData<MarkAttendanceState> = _state

    fun submit(selfie: Bitmap) {
        viewModelScope.launch {
            val enrolled = staffRepository.getEnrolledEmbedding(staffId)
            if (enrolled == null) {
                _state.value = MarkAttendanceState.NotEnrolled
                return@launch
            }

            when (val result = faceRecognizer.analyse(selfie)) {
                is FaceResult.Success -> {
                    val match = FaceMatcher.compare(enrolled, result.embedding)
                    // The failed attempt is persisted too, so there is an audit trail of
                    // who tried to check in as whom.
                    val path = imageFileStore.saveJpeg(result.faceCrop, ImageFileStore.DIR_ATTENDANCE)
                    val location = locationHelper.currentLocation()
                    val record = attendanceRepository.record(
                        staffId = staffId,
                        selfieImagePath = path,
                        latitude = location?.latitude,
                        longitude = location?.longitude,
                        similarity = match.similarity,
                        matched = match.matched
                    )
                    _state.value = MarkAttendanceState.Recorded(record.id)
                }

                FaceResult.NoFaceFound -> _state.value = MarkAttendanceState.NoFaceFound
                is FaceResult.Failed -> _state.value = MarkAttendanceState.Failed
            }
        }
    }

    fun onStateHandled() {
        _state.value = MarkAttendanceState.Idle
    }
}
