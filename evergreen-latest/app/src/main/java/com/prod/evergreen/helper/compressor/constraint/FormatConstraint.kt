package com.prod.evergreen.helper.compressor.constraint

import android.graphics.Bitmap
import com.prod.evergreen.helper.compressor.compressFormat
import com.prod.evergreen.helper.compressor.loadBitmap
import com.prod.evergreen.helper.compressor.overWrite
import com.prod.evergreen.helper.compressor.constraint.Compression
import java.io.File

class FormatConstraint(private val format: Bitmap.CompressFormat) : Constraint {

    override fun isSatisfied(imageFile: File): Boolean {
        return format == imageFile.compressFormat()
    }

    override fun satisfy(imageFile: File): File {
        return overWrite(imageFile, loadBitmap(imageFile), format)
    }
}

fun Compression.format(format: Bitmap.CompressFormat) {
    constraint(FormatConstraint(format))
}