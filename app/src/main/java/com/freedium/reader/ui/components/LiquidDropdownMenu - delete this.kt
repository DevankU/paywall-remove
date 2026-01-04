package com.freedium.reader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.capsule.ContinuousRoundedRectangle

data class MenuItem(
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun LiquidDropdownMenu(
    visible: Boolean,
    items: List<MenuItem>,
    backdrop: Backdrop,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    darkModeToggle: @Composable (() -> Unit)? = null
) {
    val isLightTheme = !isSystemInDarkTheme()
    val surfaceColor = if (isLightTheme) Color.White.copy(alpha = 0.8f) else Color(0xFF1C1C1E).copy(alpha = 0.85f)
    val textColor = if (isLightTheme) Color.Black else Color.White
    val density = LocalDensity.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                scaleIn(
                    spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
                    transformOrigin = TransformOrigin(1f, 0f)
                ),
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) +
                scaleOut(
                    spring(stiffness = Spring.StiffnessHigh),
                    transformOrigin = TransformOrigin(1f, 0f)
                ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(16.dp) },
                    effects = {
                        with(density) {
                            blur(20f.dp.toPx())
                            lens(6f.dp.toPx(), 12f.dp.toPx())
                        }
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            alpha = if (isLightTheme) 0.5f else 0.2f
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 16.dp,
                            color = Color.Black.copy(alpha = 0.2f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 3.dp,
                            alpha = 0.15f
                        )
                    },
                    onDrawSurface = {
                        drawRect(surfaceColor)
                    }
                )
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEach { item ->
                    LiquidMenuItem(
                        title = item.title,
                        textColor = textColor,
                        onClick = {
                            item.onClick()
                            onDismiss()
                        }
                    )
                }

                if (darkModeToggle != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Mode",
                            color = textColor,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        darkModeToggle()
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidMenuItem(
    title: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
