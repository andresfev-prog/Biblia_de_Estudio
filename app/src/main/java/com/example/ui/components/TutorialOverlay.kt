package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.StringsLocale
import com.example.ui.viewmodel.AppDestination

/**
 * 9 Pasos secuenciales del Tour Interactivo en Vivo.
 */
enum class TutorialStep(
    val stepNumber: Int,
    val destination: AppDestination,
    val titleKey: String,
    val descKey: String
) {
    HOME_VERSE_OF_DAY(
        stepNumber = 1,
        destination = AppDestination.HOME,
        titleKey = "tutorial_step1_title",
        descKey = "tutorial_step1_desc"
    ),
    HOME_CONTINUE_NOTES(
        stepNumber = 2,
        destination = AppDestination.HOME,
        titleKey = "tutorial_step2_title",
        descKey = "tutorial_step2_desc"
    ),
    BIBLE_TOP_CONTROLS(
        stepNumber = 3,
        destination = AppDestination.BIBLE,
        titleKey = "tutorial_step3_title",
        descKey = "tutorial_step3_desc"
    ),
    BIBLE_VERSES_INTERACTION(
        stepNumber = 4,
        destination = AppDestination.BIBLE,
        titleKey = "tutorial_step4_title",
        descKey = "tutorial_step4_desc"
    ),
    MODULES_TABS_FILTERS(
        stepNumber = 5,
        destination = AppDestination.MODULES,
        titleKey = "tutorial_step5_title",
        descKey = "tutorial_step5_desc"
    ),
    SEARCH_BAR_FILTERS(
        stepNumber = 6,
        destination = AppDestination.SEARCH,
        titleKey = "tutorial_step6_title",
        descKey = "tutorial_step6_desc"
    ),
    SETTINGS_FAVORITES(
        stepNumber = 7,
        destination = AppDestination.SETTINGS,
        titleKey = "tutorial_step7_title",
        descKey = "tutorial_step7_desc"
    ),
    SETTINGS_THEMES(
        stepNumber = 8,
        destination = AppDestination.SETTINGS,
        titleKey = "tutorial_step8_title",
        descKey = "tutorial_step8_desc"
    ),
    SETTINGS_TYPOGRAPHY(
        stepNumber = 9,
        destination = AppDestination.SETTINGS,
        titleKey = "tutorial_step9_title",
        descKey = "tutorial_step9_desc"
    );

    fun next(): TutorialStep? = entries.getOrNull(ordinal + 1)
    fun previous(): TutorialStep? = entries.getOrNull(ordinal - 1)
}

/**
 * Registro reactivo de coordenadas para los controles objetivo del tutorial.
 */
val LocalTutorialTargetRegistry = staticCompositionLocalOf<SnapshotStateMap<TutorialStep, Rect>> {
    mutableStateMapOf()
}

/**
 * Extensión de modificador para registrar el Rect en coordenadas raíz de cada elemento clave.
 */
fun Modifier.tutorialTarget(step: TutorialStep): Modifier = composed {
    val registry = LocalTutorialTargetRegistry.current
    this.onGloballyPositioned { coords ->
        if (coords.isAttached) {
            registry[step] = coords.boundsInRoot()
        }
    }
}

/**
 * Overlay de pantalla completa con efecto foco (Spotlight) y globo explicativo flotante.
 */
@Composable
fun TutorialOverlay(
    activeStep: TutorialStep,
    targets: SnapshotStateMap<TutorialStep, Rect>,
    interfaceLanguage: String,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val targetRect = targets[activeStep]
    val primaryColor = MaterialTheme.colorScheme.primary

    val marginPx = with(density) { 6.dp.toPx() }
    val cornerRadiusPx = with(density) { 20.dp.toPx() }
    val strokeWidthPx = with(density) { 2.dp.toPx() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("tutorial_overlay_root")
    ) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        val screenHeightDp = maxHeight
        val statusBarInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        // 1. Lienzo con velo oscuro y recorte transparente acelerado por hardware
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .pointerInput(activeStep) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            // Fondo oscuro (74% opacidad)
            drawRect(color = Color.Black.copy(alpha = 0.74f))

            if (targetRect != null) {
                val cutoutLeft = targetRect.left - marginPx
                val cutoutTop = targetRect.top - marginPx
                val cutoutWidth = targetRect.width + (marginPx * 2f)
                val cutoutHeight = targetRect.height + (marginPx * 2f)

                // Recorte transparente
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(cutoutLeft, cutoutTop),
                    size = Size(cutoutWidth, cutoutHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    blendMode = BlendMode.Clear
                )

                // Borde luminoso primario
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cutoutLeft, cutoutTop),
                    size = Size(cutoutWidth, cutoutHeight),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }

        // 2. Globo Explicativo Flotante (Tooltip Card) con posicionamiento inteligente
        val isTopHalf = if (targetRect != null) {
            targetRect.center.y < (screenHeightPx / 2f)
        } else {
            false
        }

        val cardAlignment: Alignment
        val cardModifier: Modifier

        if (targetRect != null) {
            if (isTopHalf) {
                // Ubica el globo debajo del recorte
                val rawTopDp = with(density) { (targetRect.bottom + marginPx).toDp() } + 14.dp
                val minTop = statusBarInset + 8.dp
                val maxTop = (screenHeightDp - navBarInset - 240.dp).coerceAtLeast(minTop)
                val topPadding = rawTopDp.coerceIn(minTop, maxTop)

                cardAlignment = Alignment.TopCenter
                cardModifier = Modifier
                    .padding(top = topPadding, start = 16.dp, end = 16.dp)
            } else {
                // Ubica el globo encima del recorte
                val rawBottomDp = with(density) { (screenHeightPx - targetRect.top + marginPx).toDp() } + 14.dp
                val minBottom = navBarInset + 8.dp
                val maxBottom = (screenHeightDp - statusBarInset - 240.dp).coerceAtLeast(minBottom)
                val bottomPadding = rawBottomDp.coerceIn(minBottom, maxBottom)

                cardAlignment = Alignment.BottomCenter
                cardModifier = Modifier
                    .padding(bottom = bottomPadding, start = 16.dp, end = 16.dp)
            }
        } else {
            cardAlignment = Alignment.Center
            cardModifier = Modifier.padding(horizontal = 16.dp)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(cardModifier),
            contentAlignment = cardAlignment
        ) {
            AnimatedContent(
                targetState = activeStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tutorial_step_transition"
            ) { step ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 8.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tutorial_tooltip_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Encabezado con píldora indicadora y botón Omitir
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clip(CircleShape)
                            ) {
                                Text(
                                    text = "${step.stepNumber} / 9",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }

                            TextButton(
                                onClick = onSkip,
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("tutorial_skip_button")
                            ) {
                                Text(
                                    text = StringsLocale.get("tutorial_skip", interfaceLanguage),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Título en negrita (16.sp)
                        Text(
                            text = StringsLocale.get(step.titleKey, interfaceLanguage),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Descripción clara (13.5.sp, máx 3-4 líneas)
                        Text(
                            text = StringsLocale.get(step.descKey, interfaceLanguage),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.5.sp, lineHeight = 19.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Barra inferior de botones de navegación
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (step.stepNumber > 1) {
                                OutlinedButton(
                                    onClick = onPrevious,
                                    shape = CircleShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("tutorial_prev_button")
                                ) {
                                    Text(
                                        text = StringsLocale.get("tutorial_previous", interfaceLanguage),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                onClick = onNext,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("tutorial_next_button")
                            ) {
                                Text(
                                    text = if (step.stepNumber == 9) {
                                        StringsLocale.get("tutorial_finish", interfaceLanguage)
                                    } else {
                                        StringsLocale.get("tutorial_next", interfaceLanguage)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
