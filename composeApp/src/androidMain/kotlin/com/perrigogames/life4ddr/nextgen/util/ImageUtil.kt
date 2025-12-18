package com.perrigogames.life4ddr.nextgen.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.view.Surface
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.atan2

fun getSensorRotation(x: Float, y: Float): Int {
    val angle = atan2(-x, y) * (180 / Math.PI).toFloat()
    return when (angle) {
        in 45f..135f -> Surface.ROTATION_270
        in -45f..45f -> Surface.ROTATION_0
        in -135f..-45f -> Surface.ROTATION_90
        else -> Surface.ROTATION_180
    }
}

fun correctImageOrientation(file: File) {
    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    val exif = ExifInterface(file.absolutePath)

    val rotationAngle = when (exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f // No rotation needed
    }

    val uprightBitmap = if (rotationAngle != 0f) {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle)
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }

    FileOutputStream(file).use { fos ->
        uprightBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    }
}
