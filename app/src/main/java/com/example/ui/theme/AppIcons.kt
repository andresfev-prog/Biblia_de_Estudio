package com.example.ui.theme

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.BibleClosedCrossIcon

/**
 * Catálogo centralizado de iconografía y glifos canónicos de la aplicación.
 *
 * Constantes Unicode de glifos:
 * - Lectura en voz alta (TTS): \uE9DF (Bocina / Altavoz)
 * - Pantalla dividida: \uE95A (División vertical de lectura paralela)
 * - Mis notas: \uE9B8 (Libreta / Nota editorial)
 * - Biblia: \uE913 (Libro cerrado con cruz canónico)
 */
object AppIcons {
    const val TtsAudio: String = "\uE9DF"
    const val SplitScreen: String = "\uE95A"
    const val MyNotes: String = "\uE9B8"
    const val Bible: String = "\uE913"

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
 * Composable centralizado para renderizar iconos y glifos de la aplicación
 * a partir de sus códigos Unicode canónicos o ImageVectors de alta fidelidad.
 */
@Composable
fun AppIcon(
    glyph: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = LocalContentColor.current
) {
    when (glyph) {
        AppIcons.TtsAudio -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        AppIcons.SplitScreen -> {
            Icon(
                imageVector = Icons.Filled.VerticalSplit,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        AppIcons.MyNotes -> {
            Icon(
                imageVector = Icons.Filled.EditNote,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
        AppIcons.Bible -> {
            BibleClosedCrossIcon(
                tint = tint,
                modifier = modifier
            )
        }
        else -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        }
    }
}
