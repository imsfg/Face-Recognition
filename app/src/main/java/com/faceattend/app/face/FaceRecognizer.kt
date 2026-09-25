package com.faceattend.app.face

import android.content.Context
import android.graphics.Bitmap
import java.io.Closeable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface FaceResult {
    data class Success(val embedding: FloatArray, val faceCrop: Bitmap) : FaceResult
    data object NoFaceFound : FaceResult
    data class Failed(val cause: Throwable) : FaceResult
}

/**
 * Single entry point for the detect -> crop -> embed pipeline, so ViewModels never touch
 * ML Kit or TFLite directly.
 */
class FaceRecognizer(context: Context) : Closeable {

    private val detector = FaceDetectorHelper()
    private val embedder = FaceEmbeddingHelper(context)

    suspend fun analyse(selfie: Bitmap): FaceResult = withContext(Dispatchers.Default) {
        runCatching {
            val bounds = detector.detectLargestFace(selfie) ?: return@withContext FaceResult.NoFaceFound
            val crop = ImageUtils.cropToFace(selfie, bounds)
            FaceResult.Success(embedder.embed(crop), crop)
        }.getOrElse { FaceResult.Failed(it) }
    }

    override fun close() {
        detector.close()
        embedder.close()
    }
}
