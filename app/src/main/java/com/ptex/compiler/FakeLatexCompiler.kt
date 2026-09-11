package com.ptex.compiler

import com.ptex.document.Document
import kotlinx.coroutines.delay

class FakeLatexCompiler : LatexCompiler {

    override suspend fun compile(
        document: Document
    ): CompilationResult {

        delay(1000)

        return CompilationResult(
            success = true
        )
    }
}