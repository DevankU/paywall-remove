package com.freedium.reader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousCapsule

@Composable
fun LiquidMenuButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val surfaceColor = if (isLightTheme) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f)
    val iconColor = if (isLightTheme) Color.Black else Color.White
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .drawBackdrop(
                backdrop = backdrop,
                shape = { ContinuousCapsule },
                effects = {
                    with(density) {
                        blur(12f.dp.toPx())
                        lens(4f.dp.toPx(), 8f.dp.toPx())
                    }
                },
                highlight = {
                    Highlight.Ambient.copy(
                        alpha = if (isLightTheme) 0.6f else 0.3f
                    )
                },
                shadow = {
                    Shadow(
                        radius = 8.dp,
                        color = Color.Black.copy(alpha = 0.15f)
                    )
                },
                innerShadow = {
                    InnerShadow(
                        radius = 2.dp,
                        alpha = 0.1f
                    )
                },
                onDrawSurface = {
                    drawRect(surfaceColor)
                }
            )
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu",
            tint = iconColor
        )
    }
}
