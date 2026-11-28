package com.example.ui.components

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.findViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView

/**
 * Permite sobreescribir de forma determinista el estado de visibilidad del teclado (IME)
 * en pruebas unitarias de Compose/Robolectric sin alterar el comportamiento en producción.
 */
val LocalImeVisibleOverride = compositionLocalOf<Boolean?> { null }

/**
 * Hook opcional de pruebas para observar cuándo un modal solicita ocultar explícitamente el teclado.
 */
val LocalHideImeOverride = compositionLocalOf<(() -> Unit)?> { null }

/**
 * Observa si el teclado virtual (IME) está visible en la ventana actual.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun rememberIsImeVisible(): State<Boolean> {
    val override = LocalImeVisibleOverride.current
    val systemImeVisible = WindowInsets.isImeVisible
    return rememberUpdatedState(override ?: systemImeVisible)
}

/**
 * Devuelve una acción robusta para ocultar el teclado virtual (IME) y limpiar el foco
 * dentro de ventanas `Dialog` / `ModalBottomSheet` en Android 7–16.
 */
@Composable
fun rememberHideImeAction(): () -> Unit {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val override = LocalHideImeOverride.current
    val currentOverride by rememberUpdatedState(override)

    return remember(keyboardController, focusManager, view) {
        {
            keyboardController?.hide()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            focusManager.clearFocus(force = true)
            currentOverride?.invoke()
        }
    }
}

/**
 * Intercepta el botón/gesto físico Atrás del sistema directamente en el [OnBackPressedDispatcher]
 * propio de la ventana del modal ([androidx.activity.ComponentDialog] en `ModalBottomSheet` y `Dialog`),
 * en lugar del Activity subyacente.
 *
 * Requiere configurar `shouldDismissOnBackPress = false` (en `ModalBottomSheetProperties`) o
 * `dismissOnBackPress = false` (en `DialogProperties`) para que Android 13–16 (`OnBackInvokedCallback`)
 * y Android 7–12 (`Dialog.onBackPressed`) enruten todos los eventos Atrás a este manejador en orden LIFO.
 */
@Composable
fun ModalBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val currentOnBack by rememberUpdatedState(onBack)
    val view = LocalView.current
    val fallbackOwner = LocalOnBackPressedDispatcherOwner.current

    val backCallback = remember {
        object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
    }

    SideEffect {
        backCallback.isEnabled = enabled
    }

    DisposableEffect(view, fallbackOwner) {
        var registeredDispatcher: OnBackPressedDispatcher? = null

        fun bindTo(dispatcher: OnBackPressedDispatcher) {
            if (registeredDispatcher === dispatcher) return
            backCallback.remove()
            dispatcher.addCallback(backCallback)
            registeredDispatcher = dispatcher
        }

        val initialOwner = view.findViewTreeOnBackPressedDispatcherOwner() ?: fallbackOwner
        initialOwner?.onBackPressedDispatcher?.let { bindTo(it) }

        val attachListener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val windowOwner = v.findViewTreeOnBackPressedDispatcherOwner() ?: fallbackOwner
                windowOwner?.onBackPressedDispatcher?.let { bindTo(it) }
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        }
        view.addOnAttachStateChangeListener(attachListener)

        onDispose {
            view.removeOnAttachStateChangeListener(attachListener)
            backCallback.remove()
            registeredDispatcher = null
        }
    }
}
