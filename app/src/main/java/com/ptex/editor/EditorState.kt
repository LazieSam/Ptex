package com.ptex.editor

import com.ptex.document.Document
import com.ptex.document.Project

data class EditorState(
    val project: Project,
    val openDocuments: List<Document> = emptyList(),
    val activeFileName: String? = null
){
    fun openDocument(document: Document): EditorState {
        val alreadyOpen = openDocuments.any {
            it.fileName == document.fileName
        }

        val updatedDocuments =
            if (alreadyOpen) {
                openDocuments
            } else {
                openDocuments + document
            }

        return copy(
            openDocuments = updatedDocuments,
            activeFileName = document.fileName
        )
    }

    fun closeDocument(fileName: String): EditorState {
        val updatedDocuments = openDocuments.filter {
            it.fileName != fileName
        }

        val newActiveFile =
            if (activeFileName == fileName) {
                updatedDocuments.lastOrNull()?.fileName
            } else {
                activeFileName
            }

        return copy(
            openDocuments = updatedDocuments,
            activeFileName = newActiveFile
        )
    }

    fun activateDocument(fileName: String): EditorState {
        if (openDocuments.none { it.fileName == fileName }) {
            return this
        }

        return copy(
            activeFileName = fileName
        )
    }
}