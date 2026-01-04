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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

sealed class AIAction(val label: String) {
    data object Explain : AIAction("Explain this")
    data object Summarize : AIAction("Summarize")
    data object Translate : AIAction("Translate")
    data object AskChatGPT : AIAction("Ask ChatGPT")
}

@Composable
fun LiquidAIPopup(
    visible: Boolean,
    selectedText: String,
    backdrop: Backdrop,
    onAction: (AIAction, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightTheme = !isSystemInDarkTheme()
    val surfaceColor = if (isLightTheme) Color.White.copy(alpha = 0.85f) else Color(0xFF2C2C2E).copy(alpha = 0.9f)
    val textColor = if (isLightTheme) Color.Black else Color.White
    val density = LocalDensity.current

    val actions = listOf(
        AIAction.Explain,
        AIAction.Summarize,
        AIAction.Translate,
        AIAction.AskChatGPT
    )

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
                            blur(24f.dp.toPx())
                            lens(5f.dp.toPx(), 10f.dp.toPx())
                        }
                    },
                    highlight = {
                        Highlight.Ambient.copy(
                            alpha = if (isLightTheme) 0.6f else 0.25f
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 20.dp,
                            color = Color.Black.copy(alpha = 0.25f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 4.dp,
                            alpha = 0.2f
                        )
                    },
                    onDrawSurface = {
                        drawRect(surfaceColor)
                    }
                )
                .padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                actions.forEach { action ->
                    AIActionItem(
                        action = action,
                        textColor = textColor,
                        onClick = {
                            val prompt = when (action) {
                                AIAction.Explain -> "Explain this: $selectedText"
                                AIAction.Summarize -> "Summarize this: $selectedText"
                                AIAction.Translate -> "Translate this to English: $selectedText"
                                AIAction.AskChatGPT -> selectedText
                            }
                            onAction(action, prompt)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AIActionItem(
    action: AIAction,
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
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = action.label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
