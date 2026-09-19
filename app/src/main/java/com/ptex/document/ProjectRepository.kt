package com.ptex.document

import android.content.Context
import java.io.File

class ProjectRepository(
    private val context: Context
) {

    private val projectsDirectory: File
        get() = File(context.filesDir, "projects")

    fun getDefaultProject(): Project {
        val projectDirectory = File(
            projectsDirectory,
            "default"
        )

        projectDirectory.mkdirs()

        return Project(
            name = "Default",
            rootDirectory = projectDirectory,
            mainFile = "main.tex"
        )
    }

    fun loadMainDocument(project: Project): Document {
        val file = File(
            project.rootDirectory,
            project.mainFile
        )

        if (!file.exists()) {
            return Document(
                source = defaultSource(),
                fileName = project.mainFile
            )
        }

        return Document(
            source = file.readText(),
            fileName = project.mainFile,
            isModified = false
        )
    }

    fun saveDocument(
        project: Project,
        document: Document
    ) {
        val file = File(
            project.rootDirectory,
            document.fileName
        )

        file.writeText(document.source)
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