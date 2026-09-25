package com.faceattend.app.face

/**
 * Serialises face embeddings for storage in Room as a JSON array string.
 * Kept free of Android/org.json dependencies so it runs in plain JVM unit tests.
 */
object EmbeddingCodec {

    fun encode(embedding: FloatArray): String =
        embedding.joinToString(prefix = "[", postfix = "]", separator = ",")

    fun decode(json: String): FloatArray {
        val body = json.trim().removePrefix("[").removeSuffix("]").trim()
        if (body.isEmpty()) return FloatArray(0)
        return body.split(",").map { it.trim().toFloat() }.toFloatArray()
    }
}
