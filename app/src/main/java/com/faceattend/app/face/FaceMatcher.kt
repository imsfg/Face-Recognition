package com.faceattend.app.face

import kotlin.math.sqrt

/**
 * The recognition decision itself: compares two face embeddings.
 *
 * Deliberately free of any Android/ML dependency so it is unit-testable and so the
 * vector *producer* can be swapped without touching anything downstream.
 */
object FaceMatcher {

    /**
     * Cosine threshold for FaceNet 128-d embeddings, as published by the model author
     * alongside the bundled `facenet.tflite`. Raise it to trade false accepts for false
     * rejects — for attendance, a false accept is the worse failure.
     */
    const val DEFAULT_THRESHOLD = 0.4f

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) {
            "Embedding size mismatch: ${a.size} vs ${b.size}"
        }
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i].toDouble() * b[i]
            normA += a[i].toDouble() * a[i]
            normB += b[i].toDouble() * b[i]
        }
        if (normA == 0.0 || normB == 0.0) return 0f
        return (dot / (sqrt(normA) * sqrt(normB))).toFloat()
    }

    fun isMatch(similarity: Float, threshold: Float = DEFAULT_THRESHOLD): Boolean =
        similarity >= threshold

    fun compare(
        enrolled: FloatArray,
        live: FloatArray,
        threshold: Float = DEFAULT_THRESHOLD
    ): MatchResult {
        val similarity = cosineSimilarity(enrolled, live)
        return MatchResult(matched = isMatch(similarity, threshold), similarity = similarity)
    }
}

data class MatchResult(val matched: Boolean, val similarity: Float)
