package com.sowmya.security.ui.theam

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.tooling.preview.Preview


val darkGlowColor = Color(0xFF1A1A1A) // rich dark gray



private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FFFF),     // Neon Cyan
    onPrimary = Color.Black,
    background = Color(0xFF0A0A0A),  // Deep dark gray
    surface = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White
)
@Composable
fun NeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

@Preview
@Composable
fun GlowingCurvedLines() {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val width = size.width
        val height = size.height

        val glowingLine = Path().apply {
            moveTo(0f, height * 0.3f)
            quadraticBezierTo(
                width * 0.4f, height * 0.1f,
                width, height * 0.4f
            )
        }
        val bottomGlowingLine = Path().apply {
            moveTo(0f, height * 0.7f) // Mirrored start point
            quadraticBezierTo(
                width * 0.5f, height * 0.9f, // Mirrored control point
                width, height * 0.7f // Mirrored end point
            )
        }
//
        drawPath(
            path = glowingLine,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF00B4D8), // Neon blue
                    Color.White.copy(alpha = 0.8f),
                    Color.Transparent
                )
            ),
            style = Stroke(
                width = 7f,
                cap = StrokeCap.Round
            ),
            alpha = 0.9f
        )
//
//        // Glow overlay
        drawPath(
            path = glowingLine,
            color = Color(0xFF00B4D8),
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round
            ),
            alpha = 0.5f,
            blendMode = BlendMode.Plus
        )
//
//        drawPath(
//            path = bottomGlowingLine,
//            brush = Brush.horizontalGradient(
//                colors = listOf(
//                    Color.Transparent,
//                    Color(0xFF00B4D8),
//                    Color.White.copy(alpha = 0.8f),
//                    Color.Transparent
//                )
//            ),
//            style = Stroke(width = 6f, cap = StrokeCap.Round),
//            alpha = 0.9f
//        )
//
//// Bottom glow
//        drawPath(
//            path = bottomGlowingLine,
//            color = Color(0xFF00B4D8),
//            style = Stroke(width = 12f, cap = StrokeCap.Round),
//            alpha = 0.2f,
//            blendMode = BlendMode.Plus
//        )
//        fun drawGlowingPath(
//            startY: Float,
//            controlY: Float,
//            endY: Float,
//            baseAlpha: Float = 0.9f,
//            glowAlpha: Float = 0f,
//            color: Color = Color(0xFF00B4D8) // Neon Blue
//        ) {
//            val path = Path().apply {
//                moveTo(0f, startY)
//                quadraticBezierTo(width * 0.5f, controlY, width, endY)
//            }
//
//            // Main glowing stroke
//            drawPath(
//                path = path,
//                brush = Brush.horizontalGradient(
//                    colors = listOf(
//                        Color.Transparent,
//                        color,
//                        Color.White.copy(alpha = 0.8f),
//                        Color.Transparent
//                    )
//                ),
//                style = Stroke(width = 6f, cap = StrokeCap.Round),
//                alpha = baseAlpha
//
//            )
//
//            // Soft glow overlay
//            drawPath(
//                path = path,
//                color = color,
//                style = Stroke(width = 12f, cap = StrokeCap.Round),
//                alpha = glowAlpha,
//                blendMode = BlendMode.Plus
//            )
//        }
//        drawGlowingPath(
//            startY = height * 0.1f,
//            controlY = height * 0.57f,
//            endY = height * 0.479f,
//            color = Color.DarkGray
//        )
//        drawGlowingPath(
//            startY = height * 0.2f,
//            controlY = height * 0.57f,
//            endY = height * 0.48f,
//            color = Color.Blue
//        )
//        drawGlowingPath(
//            startY = height * 0.25f,
//            controlY = height * 0.57f,
//            endY = height * 0.49f,
//            color = Color.Transparent
//        )
//        drawGlowingPath(
//            startY = height * 0.3f,
//            controlY = height * 0.57f,
//            endY = height * 0.495f,
//            color = Color.Blue
//        )
//        drawGlowingPath(
//            startY = height * 0.34f,
//            controlY = height * 0.57f,
//            endY = height * 0.5f,
//            color = Color.Gray
//        )
//
//        drawGlowingPath(
//            startY = height * 0.37f,
//            controlY = height * 0.57f,
//            endY = height * 0.5f,
//            color = Color.Blue
//        )
//        // Middle arc
//        drawGlowingPath(
//            startY = height * 0.4f,
//            controlY = height * 0.57f,
//            endY = height * 0.5f,
//            color = Color.LightGray
//        )
//        drawGlowingPath(
//            startY = height * 0.44f,
//            controlY = height * 0.57f,
//            endY = height * 0.5f,
//            color = Color.Blue
//        )
//        drawGlowingPath(
//            startY = height * 0.47f,
//            controlY = height * 0.57f,
//            endY = height * 0.5f,
//            color = Color.LightGray
//        )
//        // Bottom arc
//        drawGlowingPath(
//            startY = height * 0.8f,
//            controlY = height * 0.555f,
//            endY = height * 0.65f,
//            color = Color.DarkGray // Subtle lower line
//        )
//        drawGlowingPath(
//            startY = height * 0.83f,
//            controlY = height * 0.555f,
//            endY = height * 0.65f,
//            color = Color.LightGray // Subtle lower line
//        )
//        drawGlowingPath(
//            startY = height * 1f,
//            controlY = height * 0.555f,
//            endY = height * 0.67f,
//            color = Color.LightGray // Subtle lower line
//        )
//        // Bottom arc
//        drawGlowingPath(
//            startY = height * 0.9f,
//            controlY = height * 0.57f,
//            endY = height * 0.66f,
//            color = Color.LightGray // Subtle lower line
//        )
//        drawGlowingPath(
//            startY = height * 0.9f,
//            controlY = height * 0.56f,
//            endY = height * 0.66f,
//            color = Color.Blue // Subtle lower line
//        )
//        drawGlowingPath(
//            startY = height * 0.86f,
//            controlY = height * 0.56f,
//            endY = height * 0.66f,
//            color = Color.Blue // Subtle lower line
//        )
////        drawCircle(color = Color.Red)
//        drawGlowingPathFullControl(
//            startX =  width*0.5f,
//            startY = height * 0.64f,
//            controlX = width,
//            controlY = height* 0.6f,
//            endX = width,
//            endY = height * 0.67f,
//            color = Color.Transparent,
//            lineWidth = 10f,
//            glowWidth = 59f
//        )
//        drawGlowingPathAnimated(
//            startY = height * 0.1f,
//            controlY = height * 0.57f,
//            endY = height * 0.48f,
//            animatedOffset = animatedOffset,
//            color = Color.Cyan
//        )

    }
}

fun DrawScope.drawGlowingPathFullControl(
    startX: Float,
    startY: Float,
    controlX: Float,
    controlY: Float,
    endX: Float,
    endY: Float,
    color: Color = Color(0xFF00B4D8),
    lineWidth: Float = 6f,
    glowWidth: Float = 12f,
    lineAlpha: Float = 0.9f,
    glowAlpha: Float = 0.2f
) {
    val path = Path().apply {
        moveTo(startX, startY)
        quadraticBezierTo(controlX, controlY, endX, endY)
    }

    // Main glowing stroke
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                color,
                Color.White.copy(alpha = 0.8f),
                Color.Transparent
            )
        ),
        style = Stroke(width = lineWidth, cap = StrokeCap.Round),
        alpha = lineAlpha
    )

    // Glow overlay
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = glowWidth, cap = StrokeCap.Round),
        alpha = glowAlpha,
        blendMode = BlendMode.Plus
    )
}
fun DrawScope.drawGlowingPathAnimated(
    startY: Float,
    controlY: Float,
    endY: Float,
    animatedOffset: Float,
    color: Color = Color(0xFF00B4D8),
    baseAlpha: Float = 0.8f,
    glowAlpha: Float = 0.4f
) {
    val width = size.width

    val path = Path().apply {
        moveTo(0f, startY)
        quadraticBezierTo(width * 0.5f, controlY, width, endY)
    }

    // Flowing neon stroke
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.3f),
                Color.White.copy(alpha = 0.9f),
                color.copy(alpha = 0.3f),
                Color.Transparent
            ),
            startX = width * (animatedOffset - 0.1f).coerceIn(0f, 1f),
            endX = width * (animatedOffset + 0.1f).coerceIn(0f, 1f)
        ),
        style = Stroke(width = 6f, cap = StrokeCap.Round),
        alpha = baseAlpha
    )

    // Glow overlay
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 12f, cap = StrokeCap.Round),
        alpha = glowAlpha,
        blendMode = BlendMode.Plus
    )
}
