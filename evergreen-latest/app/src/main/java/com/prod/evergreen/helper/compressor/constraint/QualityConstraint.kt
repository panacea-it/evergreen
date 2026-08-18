package com.prod.evergreen.helper.compressor.constraint

import com.prod.evergreen.helper.compressor.loadBitmap
import com.prod.evergreen.helper.compressor.overWrite
import com.prod.evergreen.helper.compressor.constraint.Compression
import java.io.File

/**
 * Created on : January 25, 2020
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class QualityConstraint(private val quality: Int) : Constraint {
    private var isResolved = false

    override fun isSatisfied(imageFile: File): Boolean {
        return isResolved
    }

    override fun satisfy(imageFile: File): File {
        val result = overWrite(imageFile, loadBitmap(imageFile), quality = quality)
        isResolved = true
        return result
    }
}

fun Compression.quality(quality: Int) {
    constraint(QualityConstraint(quality))
}

