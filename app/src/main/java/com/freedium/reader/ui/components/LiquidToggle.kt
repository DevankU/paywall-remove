package com.freedium.reader.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
    val trackColor = if (isLightTheme) Color(0xFF787878).copy(0.2f) else Color(0xFF787880).copy(0.36f)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20f.dp.toPx() }

    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }

    LaunchedEffect(checked) {
        fraction = if (checked) 1f else 0f
    }

    val trackBackdrop = rememberLayerBackdrop()

    Box(
        modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        // Track
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .clip(ContinuousCapsule)
                .drawBehind {
                    drawRect(lerp(trackColor, accentColor, fraction))
                }
                .size(52f.dp, 28f.dp)
                .semantics { role = Role.Switch }
                .graphicsLayer {
                    // Clickable area
                }
                .then(
                    Modifier.drawBehind { }
                )
        )

        // Thumb
        Box(
            Modifier
                .graphicsLayer {
                    val padding = 2f.dp.toPx()
                    translationX = if (isLtr) {
                        lerp(padding, padding + dragWidth, fraction)
                    } else {
                        lerp(-padding, -(padding + dragWidth), fraction)
                    }
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            drawBackdrop()
                        }
                    ),
                    shape = { ContinuousCapsule },
                    effects = {
                        with(density) {
                            blur(8f.dp.toPx())
                            lens(3f.dp.toPx(), 6f.dp.toPx())
                        }
                    },
                    highlight = {
                        Highlight.Ambient.copy(alpha = 0.5f)
                    },
                    shadow = {
                        Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(alpha = 0.1f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(radius = 2.dp, alpha = 0.1f)
                    },
                    onDrawSurface = {
                        drawRect(Color.White)
                    }
                )
                .size(24f.dp)
        )
    }

    // Handle click on entire toggle
    Box(
        modifier = Modifier
            .size(52.dp, 28.dp)
            .graphicsLayer { alpha = 0f }
            .semantics { role = Role.Switch }
    ) {
        // This is just for click handling
    }
}

@Composable
fun SimpleLiquidToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor = if (isLightTheme) Color(0xFF34C759) else Color(0xFF30D158)
    val trackColor = if (isLightTheme) Color(0xFFE0E0E0) else Color(0xFF3A3A3C)

    val fraction = if (checked) 1f else 0f
    val density = LocalDensity.current
    val dragWidth = with(density) { 24f.dp.toPx() }
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

    Box(
        modifier
            .size(52.dp, 28.dp)
            .clip(ContinuousCapsule)
            .drawBehind {
                drawRect(lerp(trackColor, accentColor, fraction))
            }
            .semantics { role = Role.Switch },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .graphicsLayer {
                    val padding = 2f.dp.toPx()
                    translationX = if (isLtr) {
                        lerp(padding, padding + dragWidth, fraction)
                    } else {
                        lerp(-padding, -(padding + dragWidth), fraction)
                    }
                }
                .size(24.dp)
                .clip(ContinuousCapsule)
                .drawBehind {
                    drawRect(Color.White)
                }
        )
    }
}
