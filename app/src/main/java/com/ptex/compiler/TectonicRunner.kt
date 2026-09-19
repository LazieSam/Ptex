package com.ptex.compiler

import android.content.Context
import com.ptex.document.Project
import java.io.File

class TectonicRunner(
    private val context: Context
) {
    private val nativeLibraries = listOf(
    "libc++_shared.so",
    "libicuuc.so.78",
    "libfontconfig.so",
    "libfreetype.so",
    "libharfbuzz.so",
    "libgraphite2.so",
    "libpng16.so",
    "libz.so.1",
    "libicudata.so.78",
    "libexpat.so.1",
    "libbz2.so.1.0",
    "libbrotlidec.so",
    "libbrotlicommon.so",
    "libglib-2.0.so.0",
    "libandroid-support.so",
    "libiconv.so",
    "libpcre2-8.so",
    "cert.pem"
    )
    private val executable: File
    get() = File(context.filesDir, "tectonic")
        
    private fun copyAssetIfMissing(name: String) {

        val file = File(context.filesDir, name)

        if (file.exists()) {
            return
        }

        context.assets.open(name).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun prepare() {
        copyAssetIfMissing("tectonic")

        for (library in nativeLibraries) {
            copyAssetIfMissing(library)
        }

        executable.setExecutable(true)
    }
    fun compile(
    project: Project,
    onOutput: (String) -> Unit
    ): CompilationResult {

        prepare()

        val external = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("External storage unavailable")

        val outputDir = File(external, "pdf")
        outputDir.mkdirs()

        val workDir = project.rootDirectory

        val homeDir = File(context.filesDir, "tectonic-home")
        homeDir.mkdirs()

        val texFile = File(
            project.rootDirectory,
            project.mainFile
        )

        onOutput("Working directory: ${workDir.absolutePath}")
        onOutput("Output directory: ${outputDir.absolutePath}")
        onOutput("Writing ${texFile.name}...")
        onOutput("Starting Tectonic...")

        return try {
            val process = ProcessBuilder(
                executable.absolutePath,
                texFile.name
            )
                .directory(workDir)
                .redirectErrorStream(true)

            process.environment()["LD_LIBRARY_PATH"] =
                context.filesDir.absolutePath
                
            process.environment()["SSL_CERT_FILE"] =
                File(context.filesDir, "cert.pem").absolutePath

            process.environment()["HOME"] =
                homeDir.absolutePath

            val processInstance = process.start()

            processInstance.inputStream
                .bufferedReader()
                .useLines { lines ->
                    lines.forEach { line ->
                        onOutput(line)
                    }
                }

            val exitCode = processInstance.waitFor()

            onOutput("Tectonic exited with code: $exitCode")

            if (exitCode != 0) {
                CompilationResult(
                    success = false,
                    errorMessage = "Compilation failed."
                )
            } else {
                val generatedPdf = File(
                    workDir,
                    texFile.nameWithoutExtension + ".pdf"
                )

                if (!generatedPdf.exists()) {
                    CompilationResult(
                        success = false,
                        errorMessage = "Compilation failed."
                    )
                } else {
                    val finalPdf = File(
                        outputDir,
                        generatedPdf.name
                    )

                    generatedPdf.copyTo(
                        finalPdf,
                        overwrite = true
                    )

                    onOutput("PDF generated!")
                    onOutput("PDF: ${finalPdf.absolutePath}")
                    onOutput("Size: ${finalPdf.length()} bytes")

                    CompilationResult(
                        success = true,
                        pdfFile = finalPdf
                    )
                }
            }
        } catch (e: Exception) {
            onOutput("ERROR: ${e::class.simpleName}")
            onOutput("ERROR: ${e.message}")

            CompilationResult(
                success = false,
                errorMessage = e.message
            )
        }
    }
}