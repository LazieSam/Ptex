package com.ptex.document

import android.content.Context
import java.io.File

class DocumentRepository(
    private val context: Context
) {

    private val documentFile: File
        get() = File(context.filesDir, "main.tex")

    fun load(): Document {
        if (!documentFile.exists()) {
            return Document(
                source = defaultSource()
            )
        }

        return Document(
            source = documentFile.readText(),
            fileName = "main.tex",
            isModified = false
        )
    }

    fun save(document: Document) {
        documentFile.writeText(document.source)
        document.isModified = false
    }

    private fun defaultSource(): String {
        return """
            \documentclass{article}

            \begin{document}

            Hello, Ptex!

            \end{document}
        """.trimIndent()
    }
}