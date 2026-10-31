package com.example.ui.util

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import java.util.Locale

private const val MENU_ITEM_TRANSLATE = 1001

@Composable
fun rememberTranslateTextToolbar(): TextToolbar {
    val context = LocalContext.current
    val view = LocalView.current
    return remember(view) {
        TranslateTextToolbar(view) { selectedText ->
            launchSystemTranslation(context, selectedText)
        }
    }
}

class TranslateTextToolbar(
    private val view: View,
    private val onTranslateRequested: (String) -> Unit
) : TextToolbar {

    private var actionMode: ActionMode? = null
    private var currentRect: Rect = Rect.Zero
    private var onCopy: (() -> Unit)? = null
    private var onSelectAll: (() -> Unit)? = null

    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    private val callback = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.clear()
            var order = 0

            // 1. Copiar
            if (onCopy != null) {
                menu.add(Menu.NONE, android.R.id.copy, order++, android.R.string.copy)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            // 2. Seleccionar todo
            if (onSelectAll != null) {
                menu.add(Menu.NONE, android.R.id.selectAll, order++, android.R.string.selectAll)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }

            // 3. Traducir
            val isEnglish = Locale.getDefault().language.startsWith("en", ignoreCase = true)
            val translateLabel = if (isEnglish) "Translate" else "Traducir"
            menu.add(Menu.NONE, MENU_ITEM_TRANSLATE, order++, translateLabel)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                android.R.id.copy -> {
                    onCopy?.invoke()
                    mode.finish()
                    return true
                }
                android.R.id.selectAll -> {
                    onSelectAll?.invoke()
                    return true
                }
                MENU_ITEM_TRANSLATE -> {
                    onCopy?.invoke()
                    mode.finish()

                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = clipboard.primaryClip
                    val text = clip?.getItemAt(0)?.text?.toString()?.trim() ?: ""

                    if (text.isNotBlank()) {
                        onTranslateRequested(text)
                    }
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            actionMode = null
            status = TextToolbarStatus.Hidden
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: android.graphics.Rect) {
            val left = currentRect.left.toInt()
            val top = currentRect.top.toInt()
            val right = currentRect.right.toInt().coerceAtLeast(left + 1)
            val bottom = currentRect.bottom.toInt().coerceAtLeast(top + 1)
            outRect.set(left, top, right, bottom)
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        currentRect = rect
        onCopy = onCopyRequested
        onSelectAll = onSelectAllRequested

        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode = view.startActionMode(callback, ActionMode.TYPE_FLOATING)
        } else {
            actionMode?.invalidateContentRect()
        }
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }
}

fun launchSystemTranslation(context: Context, text: String) {
    val cleanText = text.trim()
    if (cleanText.isEmpty()) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val translateIntent = Intent(Intent.ACTION_TRANSLATE).apply {
            putExtra(Intent.EXTRA_TEXT, cleanText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (translateIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(translateIntent)
            return
        }
    }

    val processIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_PROCESS_TEXT, cleanText)
        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val pm = context.packageManager
    val activities = pm.queryIntentActivities(processIntent, 0)

    val translateApp = activities.firstOrNull {
        it.activityInfo.packageName.contains("translate", ignoreCase = true) ||
                it.loadLabel(pm).toString().contains("traduc", ignoreCase = true) ||
                it.loadLabel(pm).toString().contains("translat", ignoreCase = true)
    }

    if (translateApp != null) {
        processIntent.setClassName(translateApp.activityInfo.packageName, translateApp.activityInfo.name)
        context.startActivity(processIntent)
        return
    }

    if (activities.isNotEmpty()) {
        val chooser = Intent.createChooser(processIntent, "Traducir con...").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return
    }

    try {
        val encoded = Uri.encode(cleanText)
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://translate.google.com/?sl=auto&tl=es&text=$encoded")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    } catch (_: Exception) {
        Toast.makeText(context, "No se encontró aplicación de traducción.", Toast.LENGTH_SHORT).show()
    }
}