package com.faceattend.app.ui.staff.attendance

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.faceattend.app.R
import com.faceattend.app.appContainer
import com.faceattend.app.ui.appViewModels
import com.faceattend.app.ui.capture.CaptureFragment

class MarkAttendanceFragment : CaptureFragment() {

    private val viewModel by appViewModels { container ->
        MarkAttendanceViewModel(
            container.staffRepository,
            container.attendanceRepository,
            container.faceRecognizer,
            container.imageFileStore,
            container.locationHelper,
            container.sessionManager.staffId
        )
    }

    // Asked for once the user is actually checking in; a denial does not block attendance,
    // it only leaves the location field empty.
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override val hintText = R.string.capture_hint_attendance

    override fun onSelfieCaptured(selfie: Bitmap) = viewModel.submit(selfie)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!requireContext().appContainer.locationHelper.hasPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MarkAttendanceState.Recorded -> {
                    viewModel.onStateHandled()
                    findNavController().navigate(
                        MarkAttendanceFragmentDirections.actionMarkAttendanceToResult(state.attendanceId)
                    )
                }

                MarkAttendanceState.NotEnrolled -> {
                    showMessage(R.string.error_not_enrolled)
                    resetShutter()
                    viewModel.onStateHandled()
                }

                MarkAttendanceState.NoFaceFound -> {
                    showMessage(R.string.error_no_face)
                    resetShutter()
                    viewModel.onStateHandled()
                }

                MarkAttendanceState.Failed -> {
                    showMessage(R.string.error_capture_failed)
                    resetShutter()
                    viewModel.onStateHandled()
                }

                MarkAttendanceState.Idle -> Unit
            }
        }
    }
}
