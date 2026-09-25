package com.faceattend.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.faceattend.app.face.ImageUtils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Front-camera preview plus a one-shot still capture, shared by face enrolment and
 * attendance check-in so both paths feed the recogniser identically framed images.
 */
class SelfieCapture(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    private var imageCapture: ImageCapture? = null

    suspend fun start() {
        val provider = awaitCameraProvider()
        val preview = Preview.Builder().build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }
        val capture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            capture
        )
        imageCapture = capture
    }

    suspend fun capture(): Bitmap {
        val capture = checkNotNull(imageCapture) { "start() must be called before capture()" }
        return suspendCancellableCoroutine { continuation ->
            capture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val bitmap = runCatching { image.decodeToBitmap() }
                        image.close()
                        bitmap.fold(continuation::resume, continuation::resumeWithException)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    private suspend fun awaitCameraProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                { runCatching(future::get).fold(continuation::resume, continuation::resumeWithException) },
                ContextCompat.getMainExecutor(context)
            )
        }

    private fun ImageProxy.decodeToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining()).also(buffer::get)
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Could not decode captured frame")
        return ImageUtils.rotate(decoded, imageInfo.rotationDegrees)
    }
}
