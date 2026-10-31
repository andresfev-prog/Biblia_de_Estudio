package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AppTheme
import com.example.ui.util.StringsLocale

@Composable
fun OnboardingDialog(
    interfaceLanguage: String,
    onComplete: () -> Unit
) {
    val appColors = AppTheme.colors
    var currentStep by remember { mutableIntStateOf(1) } // 1, 2

    Dialog(
        onDismissRequest = onComplete,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("onboarding_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.cardBg),
            border = if (appColors.isDark) androidx.compose.foundation.BorderStroke(1.dp, appColors.border) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step Indicator Pills (Exactly 2 steps)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..2) {
                        val isSelected = i == currentStep
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) appColors.accent else appColors.surfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Step Content Animated
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_step_content"
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icon Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(appColors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (step) {
                                1 -> Icons.Filled.AutoStories
                                else -> Icons.Filled.MoreVert
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = appColors.accent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val title = when (step) {
                            1 -> StringsLocale.get("onboarding_step1_title", interfaceLanguage)
                            else -> StringsLocale.get("onboarding_step2_title", interfaceLanguage)
                        }

                        val desc = when (step) {
                            1 -> StringsLocale.get("onboarding_step1_desc", interfaceLanguage)
                            else -> StringsLocale.get("onboarding_step2_desc", interfaceLanguage)
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = appColors.titleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Buttons: "Saltar" & "Siguiente" / "Comenzar"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onComplete,
                        modifier = Modifier.testTag("onboarding_skip_button"),
                        shape = CircleShape
                    ) {
                        Text(
                            text = StringsLocale.get("onboarding_skip", interfaceLanguage),
                            color = appColors.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            if (currentStep < 2) {
                                currentStep++
                            } else {
                                onComplete()
                            }
                        },
                        modifier = Modifier.testTag("onboarding_next_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.pillBg,
                            contentColor = appColors.pillText
                        )
                    ) {
                        Text(
                            text = if (currentStep == 2) {
                                StringsLocale.get("onboarding_start", interfaceLanguage)
                            } else {
                                StringsLocale.get("onboarding_next", interfaceLanguage)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
