package com.ptex.compiler

import com.ptex.document.Document

interface LatexCompiler {

    suspend fun compile(
        document: Document
    ): CompilationResult
}