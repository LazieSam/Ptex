package com.ptex.compiler

import com.ptex.debug.DebugLog
import com.ptex.document.Document
import kotlinx.coroutines.delay

class FakeLatexCompiler : LatexCompiler {

    override suspend fun compile(
        document: Document,
        onOutput: (String) -> Unit
    ): CompilationResult {

        fun log(message: String) {
            onOutput(message)
        }

        log("Starting compilation...")
        delay(500)

        log("note: generating format \"latex\"")
        delay(500)

        log("note: downloading article.cls")
        delay(700)

        log("note: downloading size10.clo")
        delay(700)

        log("Running TeX ...")
        delay(700)

        log("Rerunning TeX because \"main.aux\" changed ...")
        delay(700)

        log("Running xdvipdfmx ...")
        delay(700)

        log("Writing `main.pdf`")
        delay(500)

        log("Process exited with code 0")

        return CompilationResult(success = true)
    }
}