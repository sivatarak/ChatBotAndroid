package com.chatgptlite.wanted.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chatgptlite.wanted.ui.theme.BackGroundMessageHuman
import com.chatgptlite.wanted.ui.theme.ColorTextGPT
import kotlinx.coroutines.delay

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    circleSize: Dp = 10.dp,
    circleColor: Color = ColorTextGPT,
    spaceBetween: Dp = 590.dp,
    travelDistance: Dp = 20.dp,
    offsetX: Dp = 20.dp // New parameter for horizontal offset
) {
    val circles = listOf(
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) },
        remember { Animatable(initialValue = 0f) }
    )

    circles.forEachIndexed { index, animatable ->
        LaunchedEffect(key1 = animatable) {
            delay(index * 100L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 with LinearOutSlowInEasing
                        1.0f at 300 with LinearOutSlowInEasing
                        0.0f at 600 with LinearOutSlowInEasing
                        0.0f at 1200 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    val circleValues = circles.map { it.value }
    val distance = with(LocalDensity.current) { travelDistance.toPx() }

    Box(
        modifier = modifier
            .padding(top = spaceBetween)
            .offset(x = offsetX) // Apply horizontal offset
    ) {
        Row {
            circleValues.forEach { value ->
                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .graphicsLayer {
                            translationX = -value * distance
                        }
                        .background(
                            color = circleColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}