package com.faceattend.app.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Selfies live in app-internal storage, so they are private and removed on uninstall. */
class ImageFileStore(private val context: Context) {

    suspend fun saveJpeg(bitmap: Bitmap, subDir: String): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, subDir).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        file.absolutePath
    }

    companion object {
        const val DIR_ENROLLMENT = "enrollment"
        const val DIR_ATTENDANCE = "attendance"
    }
}
