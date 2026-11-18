package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.model.BibleBooksData
import com.example.data.model.FavoriteVerse
import com.example.data.model.UserNote
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    ERROR
}

data class CloudUser(
    val uid: String,
    val displayName: String,
    val email: String
)

data class CloudMergeResult(
    val favoritesToInsert: List<FavoriteVerse>,
    val notesToUpsert: List<UserNote>
)

class UserCancelledSignInException : Exception("Google sign-in cancelled by user")

class CloudSyncRepository {

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private fun getAuthOrNull(): FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("BIBLE_DEBUG", "FirebaseAuth no disponible en este entorno", e)
        null
    }

    private fun getFirestoreOrNull(): FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w("BIBLE_DEBUG", "FirebaseFirestore no disponible en este entorno", e)
        null
    }

    fun getCurrentUser(): CloudUser? {
        val user = getAuthOrNull()?.currentUser ?: return null
        val email = user.email.orEmpty()
        val displayName = user.displayName
            ?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: ""
        return CloudUser(
            uid = user.uid,
            displayName = displayName,
            email = email
        )
    }

    suspend fun signInWithGoogle(context: Context): Result<CloudUser> {
        return try {
            val auth = getAuthOrNull()
                ?: return Result.failure(IllegalStateException("Firebase Auth no está inicializado"))

            val uiContext = resolveActivityContext(context)
            val webClientId = uiContext.getString(R.string.default_web_client_id)
            val credentialManager = CredentialManager.create(uiContext)

            val response = try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                credentialManager.getCredential(uiContext, request)
            } catch (_: NoCredentialException) {
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .build()
                val fallbackRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()
                credentialManager.getCredential(uiContext, fallbackRequest)
            }

            val credential = response.credential
            if (credential !is CustomCredential ||
                (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL &&
                        credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL)
            ) {
                return Result.failure(IllegalStateException("Credencial de Google no compatible"))
            }

            val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdCredential.idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).awaitResult()
            val firebaseUser = authResult.user
                ?: return Result.failure(IllegalStateException("Usuario de Firebase nulo tras autenticación"))

            val resolvedEmail = firebaseUser.email?.ifBlank { googleIdCredential.id } ?: googleIdCredential.id
            val resolvedName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
                ?: googleIdCredential.displayName?.takeIf { it.isNotBlank() }
                ?: resolvedEmail.substringBefore("@")

            Result.success(
                CloudUser(
                    uid = firebaseUser.uid,
                    displayName = resolvedName,
                    email = resolvedEmail
                )
            )
        } catch (e: GetCredentialCancellationException) {
            Log.i("BIBLE_DEBUG", "Inicio de sesión con Google cancelado por el usuario")
            Result.failure(UserCancelledSignInException())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("BIBLE_DEBUG", "Error durante inicio de sesión con Google", e)
            Result.failure(e)
        }
    }

    suspend fun unlinkAccount(context: Context) {
        try {
            getAuthOrNull()?.signOut()
        } catch (e: Exception) {
            Log.w("BIBLE_DEBUG", "Error cerrando sesión de FirebaseAuth", e)
        }
        try {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w("BIBLE_DEBUG", "Error limpiando estado de CredentialManager", e)
        }
        _syncStatus.value = SyncStatus.IDLE
    }

    suspend fun pushFavorite(uid: String, favorite: FavoriteVerse, canonicalKey: String) {
        pushFavorites(uid, listOf(canonicalKey to favorite))
    }

    suspend fun pushFavorites(uid: String, items: List<Pair<String, FavoriteVerse>>) {
        if (items.isEmpty()) return
        val db = getFirestoreOrNull() ?: return
        try {
            _syncStatus.value = SyncStatus.SYNCING
            val favCol = db.collection("users").document(uid).collection("favorites")
            items.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { (key, fav) ->
                    val docId = sanitizeDocId(key)
                    batch.set(favCol.document(docId), favoriteToMap(key, fav), SetOptions.merge())
                }
                batch.commit().awaitResult()
            }
            _syncStatus.value = SyncStatus.SYNCED
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleReplicationError("Error subiendo favoritos a Firestore", e)
        }
    }

    suspend fun deleteFavorite(uid: String, canonicalKey: String) {
        deleteFavorites(uid, listOf(canonicalKey))
    }

    suspend fun deleteFavorites(uid: String, canonicalKeys: List<String>) {
        if (canonicalKeys.isEmpty()) return
        val db = getFirestoreOrNull() ?: return
        try {
            _syncStatus.value = SyncStatus.SYNCING
            val favCol = db.collection("users").document(uid).collection("favorites")
            canonicalKeys.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { key ->
                    batch.delete(favCol.document(sanitizeDocId(key)))
                }
                batch.commit().awaitResult()
            }
            _syncStatus.value = SyncStatus.SYNCED
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleReplicationError("Error eliminando favoritos en Firestore", e)
        }
    }

    suspend fun pushNote(uid: String, note: UserNote) {
        val db = getFirestoreOrNull() ?: return
        val cloudId = note.cloudId.ifBlank { UUID.randomUUID().toString() }
        try {
            _syncStatus.value = SyncStatus.SYNCING
            db.collection("users")
                .document(uid)
                .collection("notes")
                .document(sanitizeDocId(cloudId))
                .set(noteToMap(note.copy(cloudId = cloudId)), SetOptions.merge())
                .awaitResult()
            _syncStatus.value = SyncStatus.SYNCED
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleReplicationError("Error subiendo nota a Firestore", e)
        }
    }

    suspend fun deleteNote(uid: String, cloudId: String) {
        if (cloudId.isBlank()) return
        val db = getFirestoreOrNull() ?: return
        try {
            _syncStatus.value = SyncStatus.SYNCING
            db.collection("users")
                .document(uid)
                .collection("notes")
                .document(sanitizeDocId(cloudId))
                .delete()
                .awaitResult()
            _syncStatus.value = SyncStatus.SYNCED
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleReplicationError("Error eliminando nota en Firestore", e)
        }
    }

    suspend fun syncAndMerge(
        uid: String,
        localFavorites: List<FavoriteVerse>,
        localNotes: List<UserNote>,
        isUserInitiated: Boolean = true
    ): Result<CloudMergeResult> {
        val db = getFirestoreOrNull()
            ?: return Result.failure(IllegalStateException("Firestore no está inicializado"))

        return try {
            _syncStatus.value = SyncStatus.SYNCING

            val userDoc = db.collection("users").document(uid)
            val favCol = userDoc.collection("favorites")
            val notesCol = userDoc.collection("notes")

            val source = if (isUserInitiated) Source.SERVER else Source.DEFAULT
            val remoteFavSnapshot = favCol.get(source).awaitResult()
            val remoteNotesSnapshot = notesCol.get(source).awaitResult()

            // 1. Mezcla de Favoritos por clave canónica
            val localFavByKey = linkedMapOf<String, FavoriteVerse>()
            localFavorites.forEach { fav ->
                val key = BibleBooksData.buildFavoriteKey(
                    bookName = fav.bookName,
                    chapter = fav.chapter,
                    verse = fav.verse,
                    version = fav.version,
                    text = fav.text
                )
                localFavByKey.putIfAbsent(key, fav)
            }

            val remoteFavByKey = linkedMapOf<String, FavoriteVerse>()
            remoteFavSnapshot.documents.forEach { doc ->
                val bookName = doc.getString("bookName")?.trim().orEmpty()
                val chapter = (doc.getLong("chapter") ?: 0L).toInt()
                val verse = (doc.getLong("verse") ?: 0L).toInt()
                val version = doc.getString("version")?.trim().orEmpty()
                val text = doc.getString("text").orEmpty()
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                if (bookName.isNotBlank() && chapter > 0 && verse > 0) {
                    val normalizedVersion = BibleBooksData.normalizeVersionId(version).ifBlank { version }
                    val fav = FavoriteVerse(
                        id = 0L,
                        version = normalizedVersion,
                        bookName = bookName,
                        chapter = chapter,
                        verse = verse,
                        text = BibleBooksData.cleanVerseTextForFavorites(text),
                        timestamp = timestamp
                    )
                    val key = BibleBooksData.buildFavoriteKey(
                        bookName = fav.bookName,
                        chapter = fav.chapter,
                        verse = fav.verse,
                        version = fav.version,
                        text = fav.text
                    )
                    remoteFavByKey.putIfAbsent(key, fav)
                }
            }

            val favoritesToInsert = remoteFavByKey
                .filterKeys { it !in localFavByKey }
                .values
                .toList()

            val favoritesToUpload = localFavByKey
                .filterKeys { it !in remoteFavByKey }
                .toList()

            // 2. Mezcla de Notas por cloudId + huella de contenido para evitar duplicados
            val normalizedLocalNotes = localNotes.map { note ->
                if (note.cloudId.isBlank()) {
                    note.copy(cloudId = "legacy_${note.id}_${note.timestamp}")
                } else {
                    note
                }
            }
            val notesNeedingCloudIdMigration = normalizedLocalNotes.filterIndexed { idx, note ->
                localNotes[idx].cloudId.isBlank()
            }

            val localNotesByCloudId = normalizedLocalNotes.associateBy { it.cloudId }
            val localSignatures = normalizedLocalNotes.map { noteSignature(it) }.toMutableSet()

            val notesToUpsertLocally = mutableListOf<UserNote>()
            notesToUpsertLocally.addAll(notesNeedingCloudIdMigration)

            val notesToUpload = mutableListOf<UserNote>()
            val matchedRemoteCloudIds = mutableSetOf<String>()

            remoteNotesSnapshot.documents.forEach { doc ->
                val remoteCloudId = doc.getString("cloudId")?.takeIf { it.isNotBlank() } ?: doc.id
                val title = doc.getString("title").orEmpty()
                val content = doc.getString("content").orEmpty()
                val biblicalReference = doc.getString("biblicalReference").orEmpty()
                val remoteTimestamp = doc.getLong("timestamp") ?: 0L

                matchedRemoteCloudIds.add(remoteCloudId)

                val existingLocal = localNotesByCloudId[remoteCloudId]
                if (existingLocal == null) {
                    val candidate = UserNote(
                        id = 0L,
                        title = title,
                        content = content,
                        biblicalReference = biblicalReference,
                        timestamp = remoteTimestamp,
                        cloudId = remoteCloudId
                    )
                    if (localSignatures.add(noteSignature(candidate))) {
                        notesToUpsertLocally.add(candidate)
                    }
                } else if (remoteTimestamp > existingLocal.timestamp) {
                    val updatedLocal = existingLocal.copy(
                        title = title,
                        content = content,
                        biblicalReference = biblicalReference,
                        timestamp = remoteTimestamp
                    )
                    notesToUpsertLocally.removeAll { it.id == existingLocal.id }
                    notesToUpsertLocally.add(updatedLocal)
                } else if (existingLocal.timestamp > remoteTimestamp) {
                    notesToUpload.add(existingLocal)
                }
            }

            normalizedLocalNotes.forEach { local ->
                if (local.cloudId !in matchedRemoteCloudIds) {
                    notesToUpload.add(local)
                }
            }

            // 3. Subir diferencias locales en lotes (Batch <= 400 ops)
            val allWrites = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Map<String, Any>>>()
            favoritesToUpload.forEach { (key, fav) ->
                allWrites.add(favCol.document(sanitizeDocId(key)) to favoriteToMap(key, fav))
            }
            notesToUpload.forEach { note ->
                allWrites.add(notesCol.document(sanitizeDocId(note.cloudId)) to noteToMap(note))
            }

            allWrites.chunked(400).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { (ref, payload) ->
                    batch.set(ref, payload, SetOptions.merge())
                }
                batch.commit().awaitResult()
            }

            _syncStatus.value = SyncStatus.SYNCED
            Result.success(
                CloudMergeResult(
                    favoritesToInsert = favoritesToInsert,
                    notesToUpsert = notesToUpsertLocally
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (!isUserInitiated && isOfflineFirestoreException(e)) {
                Log.i("BIBLE_DEBUG", "Sin conexión al iniciar; usando datos locales offline")
                _syncStatus.value = SyncStatus.IDLE
            } else {
                Log.e("BIBLE_DEBUG", "Error en sincronización bidireccional con Firestore", e)
                _syncStatus.value = SyncStatus.ERROR
            }
            Result.failure(e)
        }
    }

    private fun handleReplicationError(message: String, error: Exception) {
        if (isOfflineFirestoreException(error)) {
            Log.w("BIBLE_DEBUG", "$message (sin conexión; pendiente en caché local)")
            _syncStatus.value = SyncStatus.IDLE
        } else {
            Log.e("BIBLE_DEBUG", message, error)
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    private fun isOfflineFirestoreException(error: Exception): Boolean {
        return error is FirebaseFirestoreException &&
                error.code == FirebaseFirestoreException.Code.UNAVAILABLE
    }

    private fun favoriteToMap(key: String, fav: FavoriteVerse): Map<String, Any> = mapOf(
        "key" to key,
        "version" to fav.version,
        "bookName" to fav.bookName,
        "chapter" to fav.chapter,
        "verse" to fav.verse,
        "text" to fav.text,
        "timestamp" to fav.timestamp
    )

    private fun noteToMap(note: UserNote): Map<String, Any> = mapOf(
        "cloudId" to note.cloudId,
        "title" to note.title,
        "content" to note.content,
        "biblicalReference" to note.biblicalReference,
        "timestamp" to note.timestamp
    )

    private fun noteSignature(note: UserNote): String {
        return "${note.title.trim()}|${note.content.trim()}|${note.biblicalReference.trim()}|${note.timestamp}"
    }

    private fun sanitizeDocId(raw: String): String {
        return raw.trim().replace("/", "_").ifBlank { UUID.randomUUID().toString() }
    }

    private tailrec fun resolveActivityContext(context: Context): Context {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> {
                val base = context.baseContext
                if (base == context) context else resolveActivityContext(base)
            }
            else -> context
        }
    }

    private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { e ->
            if (cont.isActive) cont.resumeWithException(e)
        }
        addOnCanceledListener {
            cont.cancel()
        }
    }
}
