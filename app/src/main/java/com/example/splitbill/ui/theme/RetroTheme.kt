package com.example.splitbill.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class RetroColors(
    val xpBlue: Color = Color(0xFF245EDC),
    val xpBlueDark: Color = Color(0xFF1941A5),
    val silver: Color = Color(0xFFECE9D8),
    val panelGray: Color = Color(0xFFD4D0C8),
    val silverDark: Color = Color(0xFFACA899),
    val textDark: Color = Color(0xFF303030),
    val online: Color = Color(0xFF4CAF50),
    val offline: Color = Color(0xFF9E9E9E),
    val green: Color = Color(0xFF2E7D32),
    val red: Color = Color(0xFFC62828)
)

@Immutable
data class RetroShapes(
    val beveled: Shape = RoundedCornerShape(6.dp),
    val panel: Shape = RoundedCornerShape(10.dp)
)

val LocalRetroColors = staticCompositionLocalOf { RetroColors() }
val LocalRetroShapes = staticCompositionLocalOf { RetroShapes() }

object RetroTheme {
    val colors: RetroColors
        @Composable
        get() = LocalRetroColors.current

    val shapes: RetroShapes
        @Composable
        get() = LocalRetroShapes.current
}

@Composable
fun RetroTheme(
    colors: RetroColors = RetroColors(),
    shapes: RetroShapes = RetroShapes(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalRetroColors provides colors,
        LocalRetroShapes provides shapes,
        content = content
    )
}
