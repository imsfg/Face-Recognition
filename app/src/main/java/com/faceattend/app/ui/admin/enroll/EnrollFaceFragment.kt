package com.faceattend.app.ui.admin.enroll

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.faceattend.app.R
import com.faceattend.app.ui.appViewModels
import com.faceattend.app.ui.capture.CaptureFragment

class EnrollFaceFragment : CaptureFragment() {

    private val args: EnrollFaceFragmentArgs by navArgs()

    private val viewModel by appViewModels { container ->
        EnrollFaceViewModel(
            container.staffRepository,
            container.faceRecognizer,
            container.imageFileStore,
            args.staffId
        )
    }

    override val hintText = R.string.capture_hint_enrol

    override fun onSelfieCaptured(selfie: Bitmap) = viewModel.enroll(selfie)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                EnrollState.Saved -> {
                    Toast.makeText(requireContext(), R.string.enrollment_saved, Toast.LENGTH_SHORT)
                        .show()
                    viewModel.onStateHandled()
                    findNavController().popBackStack()
                }

                EnrollState.NoFaceFound -> {
                    showMessage(R.string.error_no_face)
                    resetShutter()
                    viewModel.onStateHandled()
                }

                EnrollState.Failed -> {
                    showMessage(R.string.error_capture_failed)
                    resetShutter()
                    viewModel.onStateHandled()
                }

                EnrollState.Idle -> Unit
            }
        }
    }
}
