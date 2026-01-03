package com.freedium.reader.ui

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * Utility class for Apple iOS "Liquid Glass" style blur effects and animations.
 * Uses RenderEffect for API 31+ with graceful degradation for older devices.
 */
object GlassEffect {

    private const val DEFAULT_BLUR_RADIUS = 25f
    private const val GLASS_ALPHA = 0.7f

    /**
     * Apply frosted glass blur effect to a view.
     * Only works on API 31+ (Android 12+), gracefully degrades on older devices.
     */
    fun applyBlur(view: View, radiusX: Float = DEFAULT_BLUR_RADIUS, radiusY: Float = DEFAULT_BLUR_RADIUS) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(radiusX, radiusY, Shader.TileMode.CLAMP)
            view.setRenderEffect(blurEffect)
        }
        // For older devices, the glass background drawable provides visual approximation
    }

    /**
     * Remove blur effect from a view.
     */
    fun removeBlur(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
    }

    /**
     * Create iOS-style spring animation for translation Y (show/hide toolbar).
     */
    fun createSpringAnimationY(
        view: View,
        finalPosition: Float,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    ): SpringAnimation {
        return SpringAnimation(view, DynamicAnimation.TRANSLATION_Y, finalPosition).apply {
            spring = SpringForce(finalPosition).apply {
                this.stiffness = stiffness
                this.dampingRatio = dampingRatio
            }
        }
    }

    /**
     * Create iOS-style spring animation for alpha (fade in/out).
     */
    fun createSpringAnimationAlpha(
        view: View,
        finalAlpha: Float,
        stiffness: Float = SpringForce.STIFFNESS_LOW,
        dampingRatio: Float = SpringForce.DAMPING_RATIO_NO_BOUNCY
    ): SpringAnimation {
        return SpringAnimation(view, DynamicAnimation.ALPHA, finalAlpha).apply {
            spring = SpringForce(finalAlpha).apply {
                this.stiffness = stiffness
                this.dampingRatio = dampingRatio
            }
        }
    }

    /**
     * Create iOS-style spring animation for scale.
     */
    fun createSpringAnimationScale(
        view: View,
        finalScale: Float,
        stiffness: Float = SpringForce.STIFFNESS_MEDIUM,
        dampingRatio: Float = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
    ): Pair<SpringAnimation, SpringAnimation> {
        val scaleX = SpringAnimation(view, DynamicAnimation.SCALE_X, finalScale).apply {
            spring = SpringForce(finalScale).apply {
                this.stiffness = stiffness
                this.dampingRatio = dampingRatio
            }
        }
        val scaleY = SpringAnimation(view, DynamicAnimation.SCALE_Y, finalScale).apply {
            spring = SpringForce(finalScale).apply {
                this.stiffness = stiffness
                this.dampingRatio = dampingRatio
            }
        }
        return Pair(scaleX, scaleY)
    }

    /**
     * Animate view with bouncy spring effect (iOS feel).
     */
    fun animateShow(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.scaleX = 0.8f
        view.scaleY = 0.8f

        createSpringAnimationAlpha(view, 1f).start()
        val (scaleX, scaleY) = createSpringAnimationScale(view, 1f)
        scaleX.start()
        scaleY.start()
    }

    /**
     * Animate view hide with spring effect.
     */
    fun animateHide(view: View, onEnd: (() -> Unit)? = null) {
        createSpringAnimationAlpha(view, 0f).apply {
            addEndListener { _, _, _, _ ->
                view.visibility = View.GONE
                onEnd?.invoke()
            }
        }.start()

        val (scaleX, scaleY) = createSpringAnimationScale(view, 0.8f)
        scaleX.start()
        scaleY.start()
    }
}
