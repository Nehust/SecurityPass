package com.example.passwordmanager.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Icon
import kotlin.math.absoluteValue

object AvatarGenerator {

    private val colors = listOf(
        "#F44336", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFC107", "#FF9800", "#FF5722", "#795548",
        "#9E9E9E", "#607D8B"
    )

    fun generateAvatarBitmap(name: String, sizePx: Int = 100): Bitmap {
        val letter = if (name.isNotEmpty()) name.substring(0, 1).uppercase() else "?"
        val colorHash = name.hashCode().absoluteValue
        val backgroundColor = Color.parseColor(colors[colorHash % colors.size])

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw circle background
        val bgPaint = Paint().apply {
            color = backgroundColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, bgPaint)

        // Draw text
        val textPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
            textSize = sizePx * 0.5f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        val bounds = Rect()
        textPaint.getTextBounds(letter, 0, 1, bounds)
        
        val x = sizePx / 2f
        val y = (sizePx / 2f) + (bounds.height() / 2f)
        
        canvas.drawText(letter, x, y, textPaint)

        return bitmap
    }
    
    fun generateAvatarIcon(name: String, sizePx: Int = 100): Icon {
        return Icon.createWithBitmap(generateAvatarBitmap(name, sizePx))
    }
}
