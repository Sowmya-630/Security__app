package com.sowmya.security.ui.theam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


// File: NeumorphicStyle.kt
val darkGray = Color(0xFF2C2C2C)
val lightShadow = Color(0xFF3A3A3A)
val darkShadow = Color(0xFF1C1C1C)

fun Modifier.neumorphic(
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 8.dp,
    lightColor: Color = lightShadow,
    darkColor: Color = darkShadow
): Modifier = this
    .shadow(elevation, shape, ambientColor = lightColor, spotColor = darkColor)
    .background(darkGray, shape)

