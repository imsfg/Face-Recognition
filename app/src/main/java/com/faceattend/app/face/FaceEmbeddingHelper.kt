package com.faceattend.app.face

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.io.Closeable
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Turns a cropped face bitmap into a FaceNet embedding.
 *
 * Model contract (bundled `facenet.tflite`): 160x160x3 float input, 128-d float output.
 * Preprocessing must be per-image standardisation — the model was trained that way and
 * fixed (x-127.5)/128 normalisation produces unusable embeddings.
 */
class FaceEmbeddingHelper(context: Context) : Closeable {

    private val interpreter = Interpreter(
        FileUtil.loadMappedFile(context, MODEL_ASSET),
        Interpreter.Options().apply {
            numThreads = 4
            setUseXNNPACK(true)
        }
    )

    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
        .add(StandardizeOp())
        .build()

    fun embed(faceBitmap: Bitmap): FloatArray {
        val input = imageProcessor.process(TensorImage.fromBitmap(faceBitmap)).buffer
        val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
        interpreter.run(input, output)
        return output[0]
    }

    override fun close() = interpreter.close()

    private class StandardizeOp : TensorOperator {
        override fun apply(input: TensorBuffer): TensorBuffer {
            val pixels = input.floatArray
            val mean = pixels.average().toFloat()
            val variance = pixels.map { (it - mean).pow(2) }.sum() / pixels.size
            val std = max(sqrt(variance), 1f / sqrt(pixels.size.toFloat()))
            for (i in pixels.indices) {
                pixels[i] = (pixels[i] - mean) / std
            }
            return TensorBufferFloat.createFixedSize(input.shape, DataType.FLOAT32)
                .apply { loadArray(pixels) }
        }
    }

    companion object {
        const val MODEL_ASSET = "facenet.tflite"
        const val INPUT_SIZE = 160
        const val EMBEDDING_SIZE = 128
    }
}
