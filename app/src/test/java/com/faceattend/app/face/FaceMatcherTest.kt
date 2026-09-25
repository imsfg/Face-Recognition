package com.faceattend.app.face

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FaceMatcherTest {

    @Test
    fun `identical embeddings are perfectly similar`() {
        val v = floatArrayOf(0.1f, -0.4f, 0.8f, 0.3f)
        assertEquals(1f, FaceMatcher.cosineSimilarity(v, v), 1e-5f)
    }

    @Test
    fun `scaled embeddings stay perfectly similar`() {
        val a = floatArrayOf(0.2f, 0.4f, 0.6f)
        val b = floatArrayOf(0.4f, 0.8f, 1.2f)
        assertEquals(1f, FaceMatcher.cosineSimilarity(a, b), 1e-5f)
    }

    @Test
    fun `orthogonal embeddings are not similar`() {
        val a = floatArrayOf(1f, 0f)
        val b = floatArrayOf(0f, 1f)
        assertEquals(0f, FaceMatcher.cosineSimilarity(a, b), 1e-5f)
    }

    @Test
    fun `opposite embeddings are negatively similar`() {
        val a = floatArrayOf(1f, 2f)
        val b = floatArrayOf(-1f, -2f)
        assertEquals(-1f, FaceMatcher.cosineSimilarity(a, b), 1e-5f)
    }

    @Test
    fun `zero embedding yields zero similarity rather than NaN`() {
        val a = floatArrayOf(0f, 0f, 0f)
        val b = floatArrayOf(0.3f, 0.2f, 0.1f)
        assertEquals(0f, FaceMatcher.cosineSimilarity(a, b), 1e-5f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `mismatched embedding sizes are rejected`() {
        FaceMatcher.cosineSimilarity(floatArrayOf(1f, 2f), floatArrayOf(1f))
    }

    @Test
    fun `threshold is inclusive at the boundary`() {
        assertTrue(FaceMatcher.isMatch(0.4f, threshold = 0.4f))
        assertFalse(FaceMatcher.isMatch(0.3999f, threshold = 0.4f))
    }

    @Test
    fun `compare reports match for the same face vector`() {
        val enrolled = floatArrayOf(0.5f, 0.5f, 0.5f, 0.5f)
        val result = FaceMatcher.compare(enrolled, enrolled)
        assertTrue(result.matched)
        assertEquals(1f, result.similarity, 1e-5f)
    }

    @Test
    fun `compare reports no match for an unrelated face vector`() {
        val enrolled = floatArrayOf(1f, 0f, 0f, 0f)
        val live = floatArrayOf(0f, 1f, 0f, 0f)
        val result = FaceMatcher.compare(enrolled, live)
        assertFalse(result.matched)
    }
}
