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

        val mainFile = File(
            projectDirectory,
            "main.tex"
        )

        if (!mainFile.exists()) {
            mainFile.writeText(defaultSource())
        }

        return Project(
            name = "Default",
            rootDirectory = projectDirectory,
            mainFile = "main.tex"
        )
    }

    fun loadDocument(
        project: Project,
        fileName: String
    ): Document {
        val file = File(
            project.rootDirectory,
            fileName
        )

        if (!file.exists()) {
            return Document(
                fileName = fileName
            )
        }

        return Document(
            source = file.readText(),
            fileName = fileName,
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
    fun listFiles(project: Project): List<File> {
    return project.rootDirectory
        .walkTopDown()
        .filter { it.isFile }
        .toList()
    }
    fun createFile(
        project: Project,
        fileName: String
    ): Document {
        val file = File(
            project.rootDirectory,
            fileName
        )

        if (file.exists()) {
            throw IllegalStateException(
                "File already exists: $fileName"
            )
        }

        file.parentFile?.mkdirs()
        file.createNewFile()

        return Document(fileName = fileName)
    }
}