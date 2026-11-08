package com.example.data.util

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
        fileName: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        val destFile = context.getDatabasePath(fileName)
        val tempFile = File(destFile.parentFile, "$fileName.tmp")

        try {
            destFile.parentFile?.mkdirs()

            var currentUrl = downloadUrl
            var redirectCount = 0
            val maxRedirects = 5

            // Manejo de redirecciones HTTP 302/301 de GitHub Releases a AWS S3
            while (redirectCount < maxRedirects) {
                val url = URL(currentUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 25000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == 307 || status == 308
                ) {
                    currentUrl = connection.getHeaderField("Location")
                    connection.disconnect()
                    redirectCount++
                } else if (status == HttpURLConnection.HTTP_OK) {
                    break
                } else {
                    return@withContext Result.failure(Exception("HTTP Error: $status"))
                }
            }

            val totalBytes = connection!!.contentLength.toFloat()

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            onProgress(totalRead / totalBytes)
                        }
                    }
                    output.flush()
                }
            }

            // Si se descarga completamente, se renombra a su nombre definitivo .SQLite3
            if (tempFile.exists()) {
                if (destFile.exists()) destFile.delete()
                tempFile.renameTo(destFile)
            }

            Result.success(destFile)
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun deleteDownloadedFile(fileName: String): Boolean {
        val file = context.getDatabasePath(fileName)
        return if (file.exists()) file.delete() else false
    }
}

