package com.faceattend.app.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.faceattend.app.R
import com.faceattend.app.databinding.FragmentCaptureBinding
import com.faceattend.app.util.SelfieCapture
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Camera preview, permission gate and shutter, shared by face enrolment and attendance
 * check-in so both produce identically processed selfies.
 */
abstract class CaptureFragment : Fragment() {

    private var _binding: FragmentCaptureBinding? = null
    protected val binding get() = _binding!!

    private lateinit var selfieCapture: SelfieCapture

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startCamera() else showPermissionPanel() }

    @get:StringRes
    protected abstract val hintText: Int

    protected abstract fun onSelfieCaptured(selfie: Bitmap)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.hintText.setText(hintText)
        selfieCapture = SelfieCapture(requireContext(), viewLifecycleOwner, binding.previewView)

        binding.grantPermissionButton.setOnClickListener {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        binding.captureButton.setOnClickListener { capture() }

        if (hasCameraPermission()) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    protected fun showProgress(visible: Boolean) {
        _binding?.progressOverlay?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    protected fun showMessage(@StringRes message: Int) {
        _binding?.let { Snackbar.make(it.root, message, Snackbar.LENGTH_LONG).show() }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        binding.permissionPanel.visibility = View.GONE
        binding.captureButton.isEnabled = true
        viewLifecycleOwner.lifecycleScope.launch { selfieCapture.start() }
    }

    private fun showPermissionPanel() {
        binding.permissionPanel.visibility = View.VISIBLE
        binding.captureButton.isEnabled = false
    }

    private fun capture() {
        showProgress(true)
        binding.captureButton.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            // A camera HAL that dies mid-capture never invokes either callback, which would
            // otherwise leave the shutter disabled and the spinner up with no way back.
            runCatching { withTimeout(CAPTURE_TIMEOUT_MS) { selfieCapture.capture() } }
                .onSuccess { onSelfieCaptured(it) }
                .onFailure {
                    resetShutter()
                    showMessage(R.string.error_capture_failed)
                }
        }
    }

    /** Re-arms the shutter after a failed attempt so the user can retry. */
    protected fun resetShutter() {
        showProgress(false)
        _binding?.captureButton?.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val CAPTURE_TIMEOUT_MS = 10_000L
    }
}
