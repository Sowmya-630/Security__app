package com.sowmya.security.ui.theam

// Compose essentials
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment

// Animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image

import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.sowmya.security.R

@Composable
fun LightningStreaks(
    modifier: Modifier = Modifier,
    streakCount: Int = 6
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        repeat(streakCount) {
            val startX = Random.nextFloat() * width
            val startY = Random.nextFloat() * height / 2
            val endX = startX + Random.nextFloat() * 100f - 50f
            val endY = startY + Random.nextFloat() * 80f + 20f

            drawLine(
                color = Color.Cyan.copy(alpha = alpha),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}
@Composable
fun NeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .neumorphic(RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(hint, color = Color.Gray)
                }
                innerTextField()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
@Composable
fun NeumorphicButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(20.dp))
//            .background(darkGray)
            .clickable { onClick() }
            .neumorphic(RoundedCornerShape(20.dp))
            .padding(horizontal = 32.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MovingGlowingLinesBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "GlowLines")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "LineShift"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val width = size.width
        val height = size.height

        val paths = listOf(
            height * 0.2f,
            height * 0.4f,
            height * 0.6f,
            height * 0.8f
        )

        for (y in paths) {
            val path = Path().apply {
                moveTo(-width + animatedOffset * width * 2, y)
                quadraticBezierTo(
                    width / 2, y - 100,
                    width + animatedOffset * width * 2, y
                )
            }

            // Main glowing line
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00B4D8),
                        Color.White.copy(alpha = 0.6f),
                        Color.Transparent
                    )
                ),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // Soft glow around
            drawPath(
                path = path,
                color = Color(0xFF00B4D8),
                style = Stroke(width = 10f, cap = StrokeCap.Round),
                alpha = 0.1f,
                blendMode = BlendMode.Plus
            )
        }
    }
}

@Composable
fun DiagonalGlowingLinesBackground() {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = -600f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val height = size.height
        val width = size.width

        val lineColors = listOf(
            Color(0xFF00BFFF), // Blue glow
            Color.White.copy(alpha = 0.6f), // White glow
            Color(0xFF1E90FF) // Deeper blue
        )

        val lineThickness = listOf(3f, 6f, 4f)

        for (i in 0..5) {
            val yOffset = i * height / 6f + 50f
            val start = Offset(animatedOffset - i * 200f, yOffset)
            val end = Offset(animatedOffset + 500f, yOffset - 300f)

            drawLine(
                brush = Brush.linearGradient(
                    colors = lineColors.shuffled(),
                    start = start,
                    end = end
                ),
                start = start,
                end = end,
                strokeWidth = lineThickness.random(),
                cap = StrokeCap.Round,
                alpha = 0.8f
            )
        }
    }
}
@Composable
fun AnimatedECGLine() {
    val infiniteTransition = rememberInfiniteTransition()

    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing)
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Define the ECG-style line using relative offsets
        val path = Path().apply {
            moveTo(0f, canvasHeight / 2)
            lineTo(canvasWidth * 0.2f, canvasHeight / 2)
            lineTo(canvasWidth * 0.3f, canvasHeight / 2 - 100f)
            lineTo(canvasWidth * 0.4f, canvasHeight / 2 + 150f)
            lineTo(canvasWidth * 0.5f, canvasHeight / 2 - 50f)
            lineTo(canvasWidth * 0.6f, canvasHeight / 2 + 120f)
            lineTo(canvasWidth * 0.7f, canvasHeight / 2)
            lineTo(canvasWidth, canvasHeight / 2)
        }

        // Draw the base glowing ECG line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Cyan, Color.White, Color.Cyan)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // Animate a glowing dot traveling on the path
        val pathMeasure = android.graphics.PathMeasure(path.asAndroidPath(), false)
        val pos = FloatArray(2)
        pathMeasure.getPosTan(pathMeasure.length * animatedProgress, pos, null)

        drawCircle(
            color = Color.Cyan,
            center = Offset(pos[0], pos[1]),
            radius = 12f,
            alpha = 0.9f
        )
    }
}

@Composable
fun AnimatedECGLineWithGlow(trigger: Boolean) {
    // State to animate pulse when triggered
    var animate by remember { mutableStateOf(false) }

    // Launch pulse animation when trigger is true
    LaunchedEffect(trigger) {
        if (trigger) {
            animate = true
            delay(1000)  // Duration of one pulse
            animate = false
        }
    }

    // Animated progress of the pulse
    val animatedProgress by animateFloatAsState(
        targetValue = if (animate) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "pulseProgress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height / 2)
            lineTo(width * 0.2f, height / 2)
            lineTo(width * 0.3f, height / 2 - 100f)
            lineTo(width * 0.4f, height / 2 + 150f)
            lineTo(width * 0.5f, height / 2 - 50f)
            lineTo(width * 0.6f, height / 2 + 120f)
            lineTo(width * 0.7f, height / 2)
            lineTo(width, height / 2)
        }

        // 🔵 Glow behind ECG Line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(Color.Cyan.copy(alpha = 0.3f), Color.White.copy(alpha = 0.2f), Color.Cyan.copy(alpha = 0.3f))
            ),
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )

        // 🔹 Base ECG Line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                listOf(Color.Cyan, Color.White, Color.Cyan)
            ),
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        // ✨ Moving Pulse (if animation is active)
        if (animate) {
            val pathMeasure = android.graphics.PathMeasure(path.asAndroidPath(), false)
            val pos = FloatArray(2)
            pathMeasure.getPosTan(pathMeasure.length * animatedProgress, pos, null)

            drawCircle(
                color = Color.Cyan,
                center = Offset(pos[0], pos[1]),
                radius = 12f,
                alpha = 0.9f
            )
        }
    }
}

@Composable
fun RedShockSparkEffect(trigger: Boolean) {
    if (trigger) {
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(trigger) {
            alpha.snapTo(1f)
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 400)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.Red.copy(alpha = alpha.value),
                radius = size.minDimension / 3f,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
}

@Composable
fun ParticleBurstEffect(trigger: Boolean) {
    // Ensure particles list is stable across recompositions
    val particles = remember { List(20) { Animatable(0f) } }
    val scope = rememberCoroutineScope()

    // Use LocalDensity to get actual pixel dimensions
    val density = LocalDensity.current
    val screenWidth = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { LocalConfiguration.current.screenHeightDp.dp.toPx() }

    // Launch particle animation when trigger changes
    LaunchedEffect(trigger) {
        if (trigger) {
            particles.forEachIndexed { index, anim ->
                scope.launch {
                    val dx = Random.nextFloat() * 2f - 1f // Random value between -1 and 1
                    val dy = Random.nextFloat() * 2f - 1f // Random value between -1 and 1
                    anim.animateTo(
                        targetValue =dx * screenWidth,
                        animationSpec = tween(600, easing = LinearOutSlowInEasing)
                    )
                }
            }
        }
    }

    // Canvas to render particles
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach {
            // Use Offset for positioning of the particle
            drawCircle(
                color = Color.Cyan,
                radius = 6f,
                center = center, // Adding Offset to Offset
                blendMode = BlendMode.Plus
            )
        }
    }
}

@Composable
fun ECGScreenWithTrigger() {
    var triggerPulse by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedECGLineWithGlow(trigger = triggerPulse)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            // Toggle to trigger the animation
            triggerPulse = true
        }) {
            Text("Trigger Heartbeat")
        }
    }

    // Reset trigger after firing (for stateless triggering)
    LaunchedEffect(triggerPulse) {
        if (triggerPulse) {
            delay(100) // Small buffer
            triggerPulse = false
        }
    }
}

@Composable
fun SparkBurstBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val sparkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        for (i in 0..20) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * height
            val radius = Random.nextFloat() * 8f + 2f
            drawCircle(
                color = Color.Cyan.copy(alpha = sparkAlpha),
                radius = radius,
                center = Offset(x, y),
                blendMode = BlendMode.Screen
            )
        }
    }
}



@Composable
fun MoonGlowBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        // 🌌 Static night background
        Image(
            painter = painterResource(id = R.drawable.profile), // Your uploaded background
            contentDescription = "Night Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🌕 Moon Glow Animation (at fixed position)
        val infiniteTransition = rememberInfiniteTransition()
        val glowRadius by infiniteTransition.animateFloat(
            initialValue = 80f,
            targetValue = 110f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Canvas(modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp, end = 50.dp)
            .size(250.dp)
            .align(Alignment.TopEnd)) {
            drawCircle(
                color = Color(0xFFB0E0E6), // Pale glow color
                radius = glowRadius,
                center = Offset(x = size.width / 2, y = size.height / 2),
                alpha = 0.3f
            )
        }

        // ⚙️ Your foreground content (Scaffold, Screens, etc.)
        content()
    }
}
