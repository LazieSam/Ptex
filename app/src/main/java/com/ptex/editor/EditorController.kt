package com.ptex.editor

import com.ptex.document.Document
import com.ptex.document.Project
import com.ptex.document.ProjectRepository

class EditorController(
    private val repository: ProjectRepository,
    initialState: EditorState
) {

    var state: EditorState = initialState
        private set
    
    val activeDocument: Document?
    get() {
        val activeFileName = state.activeFileName
            ?: return null

        return state.openDocuments.firstOrNull {
            it.fileName == activeFileName
        }
    }

    fun openDocument(fileName: String): Document {

        val existingDocument = state.openDocuments.firstOrNull {
            it.fileName == fileName
        }

        if (existingDocument != null) {
            state = state.activateDocument(fileName)
            return existingDocument
        }

        val document = repository.loadDocument(
            state.project,
            fileName
        )

        state = state.openDocument(document)

        return document
    }
    fun selectDocument(fileName: String): Document? {
        return openDocument(fileName)
    }

    fun createDocument(fileName: String) {
        val document = repository.createFile(
            state.project,
            fileName
        )

        state = state.openDocument(document)
    }

    fun activateDocument(fileName: String) {
        state = state.activateDocument(fileName)
    }

    fun closeDocument(fileName: String) {
        state = state.closeDocument(fileName)
    }

    fun saveDocument(document: Document) {
        repository.saveDocument(
            state.project,
            document
        )
    }

    companion object {

        fun create(
            repository: ProjectRepository,
            project: Project
        ): EditorController {

            val controller = EditorController(
                repository = repository,
                initialState = EditorState(
                    project = project
                )
            )

            controller.openDocument(project.mainFile)

            return controller
        }
    }
}