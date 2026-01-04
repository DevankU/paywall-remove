package com.freedium.reader.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.launch

@Composable
fun LiquidGlassBottomBar(
    backdrop: Backdrop,
    isDarkMode: Boolean,
    isReadingMode: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDarkModeToggle: () -> Unit,
    onReadingModeToggle: () -> Unit,
    onScreenshot: () -> Unit,
    onExportPdf: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val density = LocalDensity.current
    
    // Animation for expansion (0 = collapsed, 1 = expanded)
    val expansionProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 300f
        ),
        label = "expansion"
    )
    
    // Colors
    val surfaceColor = if (isLightTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)
    val iconColor = if (isLightTheme) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f)
    val activeIconColor = Color(0xFF0088FF)
    val blueTint = Color(0xFF0088FF)
    
    // Bar dimensions
    val collapsedWidth = 56.dp
    val expandedWidth = 300.dp
    val barHeight = 56.dp
    val iconButtonSize = 44.dp
    
    // Interpolated width
    val currentWidth = with(density) {
        lerp(collapsedWidth.toPx(), expandedWidth.toPx(), expansionProgress).toDp()
    }
    
    // Export backdrop for nested glass elements
    val barBackdrop = rememberLayerBackdrop()
    
    Box(
        modifier = modifier
            .height(barHeight)
            .width(currentWidth),
        contentAlignment = Alignment.CenterEnd
    ) {
        // Main glass bar container
        Box(
            Modifier
                .matchParentSize()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousCapsule },
                    effects = {
                        with(density) {
                            vibrancy()
                            blur(20f.dp.toPx())
                            lens(12f.dp.toPx(), 24f.dp.toPx())
                        }
                    },
                    exportedBackdrop = barBackdrop,
                    highlight = {
                        Highlight.Ambient.copy(alpha = if (isLightTheme) 0.4f else 0.2f)
                    },
                    shadow = {
                        Shadow(
                            radius = 12.dp,
                            color = Color.Black.copy(alpha = 0.15f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(radius = 3.dp, alpha = 0.08f)
                    },
                    onDrawSurface = {
                        drawRect(surfaceColor)
                    }
                )
        )
        
        // Content Row
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon buttons (only visible when expanded)
            if (expansionProgress > 0.1f) {
                // Share button
                LiquidIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    onClick = onShare,
                    backdrop = barBackdrop,
                    iconColor = iconColor,
                    isLightTheme = isLightTheme,
                    alpha = expansionProgress
                )
                
                // PDF Export button
                LiquidIconButton(
                    icon = Icons.Default.PictureAsPdf,
                    contentDescription = "Export PDF",
                    onClick = onExportPdf,
                    backdrop = barBackdrop,
                    iconColor = iconColor,
                    isLightTheme = isLightTheme,
                    alpha = expansionProgress
                )
                
                // Screenshot button
                LiquidIconButton(
                    icon = Icons.Default.CameraAlt,
                    contentDescription = "Screenshot",
                    onClick = onScreenshot,
                    backdrop = barBackdrop,
                    iconColor = iconColor,
                    isLightTheme = isLightTheme,
                    alpha = expansionProgress
                )
                
                // Reading Mode button
                LiquidIconButton(
                    icon = if (isReadingMode) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    contentDescription = "Reading Mode",
                    onClick = onReadingModeToggle,
                    backdrop = barBackdrop,
                    iconColor = if (isReadingMode) activeIconColor else iconColor,
                    isActive = isReadingMode,
                    isLightTheme = isLightTheme,
                    alpha = expansionProgress
                )
                
                // Dark Mode button
                LiquidIconButton(
                    icon = if (isDarkMode) Icons.Default.Brightness4 else Icons.Default.Brightness7,
                    contentDescription = "Dark Mode",
                    onClick = onDarkModeToggle,
                    backdrop = barBackdrop,
                    iconColor = if (isDarkMode) activeIconColor else iconColor,
                    isActive = isDarkMode,
                    isLightTheme = isLightTheme,
                    alpha = expansionProgress
                )
            }
            
            // 3-dot menu button (always visible, blue when expanded)
            ThreeDotButton(
                isExpanded = isExpanded,
                onClick = onToggleExpanded,
                backdrop = barBackdrop,
                blueTint = blueTint,
                iconColor = iconColor,
                isLightTheme = isLightTheme
            )
        }
    }
}

@Composable
private fun LiquidIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    iconColor: Color,
    isLightTheme: Boolean,
    alpha: Float,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    val pressAnimation = remember { Animatable(0f) }
    
    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                scaleX = lerp(0.5f, 1f, alpha)
                scaleY = lerp(0.5f, 1f, alpha)
            }
            .size(44.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousCapsule },
                effects = {
                    with(density) {
                        vibrancy()
                        blur(8f.dp.toPx())
                        lens(4f.dp.toPx(), 8f.dp.toPx())
                    }
                },
                layerBlock = {
                    val progress = pressAnimation.value
                    val maxScale = 1.15f
                    val scale = lerp(1f, maxScale, progress)
                    scaleX = scale
                    scaleY = scale
                },
                highlight = {
                    Highlight.Ambient.copy(alpha = if (isLightTheme) 0.3f else 0.15f)
                },
                shadow = null,
                innerShadow = {
                    InnerShadow(radius = 1.dp, alpha = 0.05f)
                },
                onDrawSurface = {
                    val color = if (isActive) {
                        Color(0xFF0088FF).copy(alpha = 0.2f)
                    } else {
                        if (isLightTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f)
                    }
                    drawRect(color)
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .pointerInput(animationScope) {
                val animationSpec = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
                awaitEachGesture {
                    awaitFirstDown()
                    animationScope.launch { pressAnimation.animateTo(1f, animationSpec) }
                    waitForUpOrCancellation()
                    animationScope.launch { pressAnimation.animateTo(0f, animationSpec) }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ThreeDotButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    backdrop: Backdrop,
    blueTint: Color,
    iconColor: Color,
    isLightTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val animationScope = rememberCoroutineScope()
    val pressAnimation = remember { Animatable(0f) }
    
    // Animate tint based on expanded state
    val tintProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "tint"
    )
    
    Box(
        modifier = modifier
            .size(44.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousCapsule },
                effects = {
                    with(density) {
                        vibrancy()
                        blur(8f.dp.toPx())
                        lens(6f.dp.toPx(), 12f.dp.toPx())
                    }
                },
                layerBlock = {
                    val progress = pressAnimation.value
                    val maxScale = 1.2f
                    val scale = lerp(1f, maxScale, progress)
                    scaleX = scale
                    scaleY = scale
                },
                highlight = {
                    Highlight.Ambient.copy(alpha = if (isLightTheme) 0.4f else 0.2f)
                },
                shadow = null,
                innerShadow = {
                    InnerShadow(radius = 2.dp, alpha = 0.1f)
                },
                onDrawSurface = {
                    if (tintProgress > 0f) {
                        // Blue tint when expanded
                        drawRect(blueTint, blendMode = BlendMode.Hue)
                        drawRect(blueTint.copy(alpha = 0.6f * tintProgress))
                    } else {
                        // Normal glass surface
                        val color = if (isLightTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)
                        drawRect(color)
                    }
                }
            )
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .pointerInput(animationScope) {
                val animationSpec = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
                awaitEachGesture {
                    awaitFirstDown()
                    animationScope.launch { pressAnimation.animateTo(1f, animationSpec) }
                    waitForUpOrCancellation()
                    animationScope.launch { pressAnimation.animateTo(0f, animationSpec) }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu",
            tint = if (tintProgress > 0.5f) Color.White else iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}
