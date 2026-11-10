package com.example.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Catálogo centralizado de iconografía y glifos canónicos de la aplicación
 * renderizados mediante la fuente vectorial auténtica appicons.ttf.
 *
 * Constantes Unicode de glifos:
 * - Ventana Inicio: \uE921
 * - Ventana Biblia: \uE913
 * - Ventana Módulos: \uEA12
 * - Buscar / Descubrir: \uE91E
 * - Configuración: \uE96F
 * - Lectura en voz alta (TTS): \uE9DF (o \uE90E)
 * - Pantalla dividida / Paralelo: \uE95A
 * - Mis notas: \uE9B8
 * - Botón tres puntos: \uEA1F
 * - Flecha atrás: \uE947
 * - Flecha adelante: \uE94B
 * - Modo claro: \uE9E8
 */
object AppIcons {
    const val Home: String = "\uE921"
    const val Bible: String = "\uE913"
    const val Modules: String = "\uEA12"
    const val Search: String = "\uE91E"
    const val Settings: String = "\uE96F"
    const val TtsAudio: String = "\uE9DF"
    const val SplitScreen: String = "\uE95A"
    const val MyNotes: String = "\uE9B8"
    const val ThreeDotsMenu: String = "\uEA1F"
    const val BackArrow: String = "\uE947"
    const val ForwardArrow: String = "\uE94B"
    const val LightMode: String = "\uE9E8"

    @Composable
    fun TtsIcon(
        contentDescription: String? = null,
        modifier: Modifier = Modifier.size(24.dp),
        tint: Color = LocalContentColor.current
    ) {
        AppIcon(glyph = TtsAudio, contentDescription = contentDescription, modifier = modifier, tint = tint)
    }

    @Composable
    fun SplitScreenIcon(
        contentDescription: String? = null,
        modifier: Modifier = Modifier.size(24.dp),
        tint: Color = LocalContentColor.current
    ) {
        AppIcon(glyph = SplitScreen, contentDescription = contentDescription, modifier = modifier, tint = tint)
    }

    @Composable
    fun NotesIcon(
        contentDescription: String? = null,
        modifier: Modifier = Modifier.size(24.dp),
        tint: Color = LocalContentColor.current
    ) {
        AppIcon(glyph = MyNotes, contentDescription = contentDescription, modifier = modifier, tint = tint)
    }

    @Composable
    fun BibleIcon(
        contentDescription: String? = null,
        modifier: Modifier = Modifier.size(24.dp),
        tint: Color = LocalContentColor.current
    ) {
        AppIcon(glyph = Bible, contentDescription = contentDescription, modifier = modifier, tint = tint)
    }
}

/**
 * Composable centralizado para renderizar iconos y glifos vectoriales
 * usando la tipografía maestra de iconos (AppIconsFontFamily) sin tofu ni fallbacks distorsionados.
 */
@Composable
fun AppIcon(
    glyph: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = LocalContentColor.current,
    fontSize: TextUnit = 22.sp
) {
    Box(
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = glyph,
            fontFamily = AppIconsFontFamily,
            color = tint,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            lineHeight = fontSize
        )
    }
}
