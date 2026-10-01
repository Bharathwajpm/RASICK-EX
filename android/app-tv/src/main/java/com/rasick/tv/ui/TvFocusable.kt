package com.rasick.tv.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.tvFocusable(
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp),
    onClick: () -> Unit
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "scale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.Transparent,
        label = "borderColor"
    )
    val elevation by animateFloatAsState(
        targetValue = if (isFocused) 12f else 2f,
        label = "elevation"
    )

    this
        .scale(scale)
        .onFocusChanged { isFocused = it.isFocused }
        .border(
            width = if (isFocused) 3.dp else 0.dp,
            color = borderColor,
            shape = shape
        )
        .shadow(
            elevation = elevation.dp,
            shape = shape,
            clip = false
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
        .focusable(interactionSource = interactionSource)
}
