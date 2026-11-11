package com.example.data.downloader

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ModuleDownloader(private val context: Context) {

    suspend fun downloadFile(
        downloadUrl: String,
        targetFileName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Manejo de codificación de caracteres especiales (apóstrofes como en NIV'11)
            val sanitizedUrl = downloadUrl.replace("'", "%27")
            var currentUrl = sanitizedUrl
            var connection: HttpURLConnection
            var redirects = 0

            // Seguir redirecciones 302/307 hacia los servidores de almacenamiento de GitHub
            while (true) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile)")
                }
                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == 307 || status == 308
                ) {
                    val newUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    currentUrl = newUrl
                    redirects++
                    if (redirects > 5) throw Exception("Demasiadas redirecciones de red")
                } else if (status in 200..299) {
                    break
                } else {
                    throw Exception("Respuesta del servidor no válida: HTTP $status")
                }
            }

            val totalBytes = connection.contentLengthLong
            val targetFile = context.getDatabasePath(targetFileName)
            val tempFile = File(targetFile.parentFile ?: context.filesDir, "$targetFileName.tmp")

            tempFile.parentFile?.mkdirs()

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(128 * 1024)
                    var bytesRead: Int
                    var totalDownloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead
                        if (totalBytes > 0) {
                            val progress = (totalDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }

            // Traslado atómico del archivo completado
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (tempFile.renameTo(targetFile)) {
                onProgress(1f)
                Result.success(targetFile)
            } else {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
                onProgress(1f)
                Result.success(targetFile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteDownloadedFile(fileName: String): Boolean {
        val file = context.getDatabasePath(fileName)
        return if (file.exists()) file.delete() else true
    }
}