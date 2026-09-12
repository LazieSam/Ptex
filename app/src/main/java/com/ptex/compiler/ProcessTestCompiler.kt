package com.ptex.compiler

import com.ptex.document.Document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProcessTestCompiler : LatexCompiler {

    override suspend fun compile(
        document: Document,
        onOutput: (String) -> Unit
    ): CompilationResult = withContext(Dispatchers.IO) {

        try {
            onOutput("Starting test process...")

            val process = ProcessBuilder(
                "/system/bin/sh",
                "-c",
                "echo 'Hello from Android process'; echo 'Second line'; sleep 1; echo 'Still running...'; sleep 1; echo 'Finished!'"
            )
                .redirectErrorStream(true)
                .start()

            process.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    onOutput(line)
                }
            }

            val exitCode = process.waitFor()

            onOutput("Process exited with code: $exitCode")

            CompilationResult(
                success = exitCode == 0
            )

        } catch (e: Exception) {

            onOutput("ERROR: ${e.message}")

            CompilationResult(
                success = false,
                errorMessage = e.message
            )
        }
    }
}