package com.example.ui.components // Asegúrate de que coincida con el paquete de tu carpeta

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun BibleClosedCrossIcon(
    tint: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Cubierta del libro cerrado con bordes y esquinas suaves
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.16f, h * 0.10f),
            size = Size(w * 0.68f, h * 0.80f),
            cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
        )

        // 2. Detalle lateral de las páginas
        drawRoundRect(
            color = Color.White.copy(alpha = 0.40f),
            topLeft = Offset(w * 0.74f, h * 0.16f),
            size = Size(w * 0.07f, h * 0.68f),
            cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
        )

        // 3. Cruz en la portada
        val crossColor = Color.White
        val strokeW = w * 0.075f

        // Línea vertical
        drawLine(
            color = crossColor,
            start = Offset(w * 0.45f, h * 0.28f),
            end = Offset(w * 0.45f, h * 0.64f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Línea horizontal
        drawLine(
            color = crossColor,
            start = Offset(w * 0.32f, h * 0.40f),
            end = Offset(w * 0.58f, h * 0.40f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

