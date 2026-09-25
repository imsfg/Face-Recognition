package com.faceattend.app.face

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect

object ImageUtils {

    /** Extra area kept around the detector's box — FaceNet expects some head margin. */
    private const val CROP_MARGIN = 0.15f

    fun cropToFace(source: Bitmap, faceBounds: Rect): Bitmap {
        val marginX = (faceBounds.width() * CROP_MARGIN).toInt()
        val marginY = (faceBounds.height() * CROP_MARGIN).toInt()

        val left = (faceBounds.left - marginX).coerceAtLeast(0)
        val top = (faceBounds.top - marginY).coerceAtLeast(0)
        val right = (faceBounds.right + marginX).coerceAtMost(source.width)
        val bottom = (faceBounds.bottom + marginY).coerceAtMost(source.height)

        return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
    }

    fun rotate(source: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return source
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
