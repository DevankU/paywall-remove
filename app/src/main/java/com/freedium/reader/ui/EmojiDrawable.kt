package com.freedium.reader.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

class EmojiDrawable(
    private val emoji: String = "\uD83D\uDD95", // Middle finger emoji
    private val backgroundColor: Int = Color.parseColor("#1976D2")
) : Drawable() {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
        style = Paint.Style.FILL
    }

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        // Draw blue background
        canvas.drawRect(bounds, backgroundPaint)

        // Draw emoji centered
        textPaint.textSize = width * 0.6f

        val x = width / 2
        val y = height / 2 - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(emoji, x, y, textPaint)
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.OPAQUE

    companion object {
        fun createBitmap(size: Int): android.graphics.Bitmap {
            val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val drawable = EmojiDrawable()
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
