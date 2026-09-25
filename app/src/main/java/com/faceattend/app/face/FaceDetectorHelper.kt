package com.faceattend.app.face

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.Closeable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Wraps ML Kit face detection. Returns the largest face found, which is the subject of
 * a selfie — smaller faces in frame are bystanders.
 */
class FaceDetectorHelper : Closeable {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()
    )

    suspend fun detectLargestFace(bitmap: Bitmap): Rect? {
        val faces = detectAll(bitmap)
        return faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }?.boundingBox
    }

    private suspend fun detectAll(bitmap: Bitmap): List<Face> =
        suspendCancellableCoroutine { continuation ->
            detector.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }

    override fun close() = detector.close()
}
