package com.example.ui.util

object StringsLocale {

    fun get(key: String, language: String): String {
        val isEn = language.contains("English", ignoreCase = true)
        val isEsEs = language.contains("España", ignoreCase = true)

        return when (key) {
            // Navigation
            "nav_home" -> if (isEn) "Home" else "Inicio"
            "nav_bible" -> if (isEn) "Bible" else "Biblia"
            "nav_modules" -> if (isEn) "Modules" else "Módulos"
            "nav_search" -> if (isEn) "Search" else "Buscar"
            "nav_settings" -> if (isEn) "Settings" else "Ajustes"

            // Home Screen
            "app_title" -> if (isEn) "Study Bible" else "Biblia de Estudio"
            "greeting_morning" -> if (isEn) "Good morning" else if (isEsEs) "Buenos días" else "Buenos días"
            "greeting_afternoon" -> if (isEn) "Good afternoon" else if (isEsEs) "Buenas tardes" else "Buenas tardes"
            "greeting_night" -> if (isEn) "Good evening" else if (isEsEs) "Buenas noches" else "Buenas noches"
            "home_subtitle" -> if (isEn) "Take a moment for the Word of God today" else "Dedica un momento a la Palabra de Dios hoy"
            "verse_of_the_day" -> if (isEn) "Verse of the Day" else "Versículo del día"
            "read_full_chapter" -> if (isEn) "Read full chapter" else "Leer capítulo completo"
            "continue_where_left" -> if (isEn) "Continue where you left off" else "Continúa donde quedaste"
            "continue_reading" -> if (isEn) "Continue reading" else "Continuar leyendo"
            "offline_ready" -> if (isEn) "Offline mode ready" else "Modo sin conexión listo"
            "copied_clipboard" -> if (isEn) "Copied to clipboard" else "Copiado al portapapeles"
            "share_verse" -> if (isEn) "Share verse" else "Compartir versículo"

            // Bible Screen
            "version" -> if (isEn) "Version" else "Versión"
            "parallel_mode" -> if (isEn) "Parallel Mode" else "Modo Paralelo"
            "audio_reading" -> if (isEn) "Audio Reading" else "Lectura de voz"
            "commentary_badge" -> if (isEn) "commentary" else "comentario"
            "cross_ref_badge" -> if (isEn) "cf." else "ref."
            "cross_references_title" -> if (isEn) "Cross References" else "Referencias Cruzadas"
            "select_passage" -> if (isEn) "Select Passage" else "Seleccionar Pasaje"
            "step_of" -> if (isEn) "Step %d of 3" else "Paso %d de 3"
            "select_book" -> if (isEn) "Select Book" else "Selecciona Libro"
            "select_chapter" -> if (isEn) "Chapter" else "Capítulo"
            "select_verse" -> if (isEn) "Verse" else "Versículo"
            "old_testament" -> if (isEn) "Old Testament (39)" else "Antiguo T. (39)"
            "new_testament" -> if (isEn) "New Testament (27)" else "Nuevo T. (27)"

            // Commentary & Dictionary Sheets
            "commentary_title" -> if (isEn) "Exegetical Commentary" else "Comentario Exegético"
            "dictionary_title" -> if (isEn) "Biblical Dictionary" else "Diccionario Bíblico"
            "lexicon_subtitle" -> if (isEn) "Strong's Concordance & Lexicon" else "Concordancia Strong y Léxico"
            "original_language" -> if (isEn) "Original language:" else "Idioma original:"
            "pronunciation" -> if (isEn) "Pronunciation:" else "Pronunciación:"
            "theological_def" -> if (isEn) "Theological Definition" else "Definición Teológica"
            "notes_refs" -> if (isEn) "Notes and References:" else "Notas y Referencias:"
            "available_commentaries" -> if (isEn) "Available Commentaries" else "Comentarios Disponibles"
            "all_languages" -> if (isEn) "All Languages" else "Todos los idiomas"

            // Search Screen
            "search_placeholder" -> if (isEn) "Search verses, topics, and questions..." else "Busca versículos, temas y preguntas..."
            "recent_searches" -> if (isEn) "Recent Searches" else "Búsquedas Recientes"
            "clear" -> if (isEn) "Clear" else "Borrar"
            "no_recent_searches" -> if (isEn) "No recent searches." else "No hay búsquedas recientes."
            "results_found" -> if (isEn) "%d results found" else "%d resultados encontrados"
            "filter_all" -> if (isEn) "All" else "Todos"
            "filter_verses" -> if (isEn) "Verses" else "Versículos"
            "filter_commentaries" -> if (isEn) "Commentaries" else "Comentarios"
            "filter_dictionary" -> if (isEn) "Dictionary" else "Diccionario"

            // Modules Screen
            "modules_title" -> if (isEn) "Modules & Resources" else "Módulos y Recursos"
            "modules_subtitle" -> if (isEn) "Manage offline translations, commentaries, and dictionaries" else "Gestiona traducciones, comentarios y diccionarios offline"
            "language_label" -> if (isEn) "Language" else "Idioma"
            "tab_versions" -> if (isEn) "Versions" else "Versiones"
            "tab_commentaries" -> if (isEn) "Commentaries" else "Comentarios"
            "tab_dictionaries" -> if (isEn) "Dictionaries" else "Diccionarios"
            "installed" -> if (isEn) "Installed" else "Instalado"
            "download" -> if (isEn) "Download" else "Descargar"
            "remove" -> if (isEn) "Remove" else "Eliminar"
            "size" -> if (isEn) "Size" else "Tamaño"
            "author" -> if (isEn) "Author" else "Autor"

            // Settings Screen
            "settings_title" -> if (isEn) "Settings" else "Ajustes"
            "settings_subtitle" -> if (isEn) "Customize your Bible study experience" else "Personaliza tu experiencia de estudio de la Palabra"
            "edit_profile" -> if (isEn) "Edit Profile" else "Editar Perfil"
            "edit_profile_desc" -> if (isEn) "Update your personal details" else "Actualiza tus datos personales"
            "full_name" -> if (isEn) "Full Name" else "Nombre Completo"
            "location_city" -> if (isEn) "Location / City" else "Ubicación / Ciudad"
            "username" -> if (isEn) "Username" else "Nombre de usuario"
            "password" -> if (isEn) "Password" else "Contraseña"
            "save_changes" -> if (isEn) "Save Changes" else "Guardar Cambios"
            "profile_updated" -> if (isEn) "Profile updated successfully" else "Perfil actualizado correctamente"
            "favorites_option" -> if (isEn) "Favorites" else "Favoritos"
            "favorites_subtitle" -> if (isEn) "View and manage your saved verses" else "Ver y gestionar tus versículos guardados"
            "interface_language" -> if (isEn) "Interface Language" else "Idioma de la Interfaz"
            "interface_language_sub" -> if (isEn) "Language for menus and resources" else "Idioma para menús y recursos"
            "app_theme" -> if (isEn) "Application Theme" else "Tema de la Aplicación"
            "theme_light" -> if (isEn) "Light" else "Claro"
            "theme_dark" -> if (isEn) "Dark" else "Oscuro"
            "theme_auto" -> if (isEn) "Automatic" else "Automático"
            "typography_title" -> if (isEn) "Typography and reading" else "Tipografía y lectura"
            "font_size" -> if (isEn) "Font Size" else "Tamaño de Fuente"
            "font_range" -> if (isEn) "Range 8 - 20 pt" else "Rango 8 - 20 pt"
            "reader_preview" -> if (isEn) "Reader preview:" else "Vista previa en lector:"
            "logout" -> if (isEn) "Log Out" else "Cerrar Sesión"
            "logout_confirm_title" -> if (isEn) "Log Out" else "Cerrar Sesión"
            "logout_confirm_desc" -> if (isEn) "Are you sure you want to log out of Bible App? Your saved notes and modules will remain on this device." else "¿Estás seguro de que deseas cerrar tu sesión en App Bíblica? Tus notas y módulos guardados localmente permanecerán en el dispositivo."
            "cancel" -> if (isEn) "Cancel" else "Cancelar"

            // Favorites Screen
            "no_favorites_yet" -> if (isEn) "You have not saved any favorite verses yet." else "Aún no has guardado versículos favoritos."
            "favorites_screen_title" -> if (isEn) "Saved Favorites" else "Versículos Favoritos"
            "saved_favorites_count" -> if (isEn) "%d saved verses" else "%d versículos guardados"

            // Overflow Menu
            "overflow_audio" -> if (isEn) "Audio Reading" else "Lectura en voz alta"
            "overflow_split" -> if (isEn) "Split Screen" else "Pantalla dividida"
            "overflow_notes" -> if (isEn) "My Notes" else "Mis Notas"

            // Notes System
            "notes_title" -> if (isEn) "My Notes" else "Mis Notas"
            "new_note" -> if (isEn) "New note" else "Nueva nota"
            "new_note_clean_title" -> if (isEn) "New note" else "Nueva nota"
            "btn_new_note" -> if (isEn) "+ New note" else "+ Nueva nota"
            "create_note" -> if (isEn) "+ Create note" else "+ Crear nota"
            "edit_note" -> if (isEn) "Edit note" else "Editar nota"
            "note_title_placeholder" -> if (isEn) "Title (e.g., Reflection, Promise...)" else "Título (ej. Reflexión, Promesa...)"
            "note_content_placeholder" -> if (isEn) "Write your thoughts and biblical reflection here..." else "Escribe tus reflexiones y notas bíblicas aquí..."
            "paste_verse" -> if (isEn) "Paste verse" else "Pegar versículo"
            "paste_commentary" -> if (isEn) "Paste commentary" else "Pegar comentario"
            "paste_definition" -> if (isEn) "Paste definition" else "Pegar definición"
            "save_note" -> if (isEn) "Save" else "Guardar"
            "delete_note" -> if (isEn) "Delete" else "Eliminar"
            "view_all_notes" -> if (isEn) "View all my notes" else "Ver todas mis notas"
            "no_notes_home" -> if (isEn) "You haven't taken any notes yet. Tap here to write a reflection." else "Aún no has tomado notas. Toca aquí para escribir una reflexión."
            "no_notes_sheet" -> if (isEn) "No notes saved yet. Write key thoughts, prayers, or study points." else "Aún no has guardado notas. Anota puntos clave, oraciones o reflexiones de estudio."

            // Onboarding
            "onboarding_skip" -> if (isEn) "Skip" else "Saltar"
            "onboarding_next" -> if (isEn) "Next" else "Siguiente"
            "onboarding_start" -> if (isEn) "Start" else "Comenzar"
            "onboarding_step1_title" -> if (isEn) "Explore the Scriptures" else "Explora las Escrituras"
            "onboarding_step1_desc" -> if (isEn) "Tap words to open dictionary definitions and tap verse abbreviations to read commentaries." else "Toca palabras para abrir diccionarios y toca abreviaturas para leer comentarios exegéticos."
            "onboarding_step2_title" -> if (isEn) "Study Tools" else "Herramientas de Estudio"
            "onboarding_step2_desc" -> if (isEn) "Use the top overflow menu (⋮) to access text-to-speech audio, split screen reading, and notes." else "Usa el menú (⋮) para acceder a lectura en voz alta, pantalla dividida y tus notas personales."
            "onboarding_step3_title" -> if (isEn) "Personalization" else "Personalización"
            "onboarding_step3_desc" -> if (isEn) "Customize font size and reading typography, switch between dark and light themes, and manage offline biblical versions and dictionaries." else "Ajusta la tipografía y tamaño de fuente para lectura cómoda, alterna entre modo oscuro y claro, y gestiona módulos sin conexión desde recursos."

            else -> key
        }
    }
}
