package com.faceattend.app.face

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EmbeddingCodecTest {

    @Test
    fun `round trips an embedding`() {
        val original = floatArrayOf(0.1f, -0.25f, 3.5f, 0f)
        assertArrayEquals(original, EmbeddingCodec.decode(EmbeddingCodec.encode(original)), 1e-6f)
    }

    @Test
    fun `encodes as a json array`() {
        assertEquals("[1.0,2.0]", EmbeddingCodec.encode(floatArrayOf(1f, 2f)))
    }

    @Test
    fun `decodes an empty array`() {
        assertEquals(0, EmbeddingCodec.decode("[]").size)
    }
}
