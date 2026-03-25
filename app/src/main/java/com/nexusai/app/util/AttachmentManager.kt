package com.nexusai.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Utility to process attachments (Images, Documents, Audio) picked by the user.
 * Copies files into a safe cache directory to be sent to the Node.js CLI process.
 */
class AttachmentManager @Inject constructor() {

    /**
     * Extracts files from Android Saf/ContentProviders to local cache for JNI processing.
     */
    fun cacheAttachment(context: Context, uri: Uri): File? {
        val fileName = getFileName(context, uri) ?: "unknown_file_\${System.currentTimeMillis()}"
        val cacheFile = File(context.cacheDir, fileName)
        
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(cacheFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}
