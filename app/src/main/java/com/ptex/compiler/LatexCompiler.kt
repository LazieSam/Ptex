package com.ptex.compiler

import com.ptex.document.Project

interface LatexCompiler {

    suspend fun compile(
        project: Project,
        onOutput: (String) -> Unit
    ): CompilationResult
}