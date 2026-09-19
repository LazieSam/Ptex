package com.ptex.compiler

import android.content.Context
import android.net.Uri
import java.io.File

class PdfExporter(
    private val context: Context
) {

    private val preferences by lazy {
        context.getSharedPreferences(
            "ptex_preferences",
            Context.MODE_PRIVATE
        )
    }

    var savedUri: Uri?
        get() {
            return preferences
                .getString("pdf_uri", null)
                ?.let { Uri.parse(it) }
        }
        set(value) {
            preferences.edit()
                .putString("pdf_uri", value?.toString())
                .apply()
        }

    fun save(pdfFile: File, uri: Uri): Boolean {
        return try {
            context.contentResolver
                .openOutputStream(uri)
                ?.use { output ->
                    pdfFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                ?: throw IllegalStateException(
                    "Could not open destination"
                )

            true

        } catch (e: Exception) {
            false
        }
    }
}