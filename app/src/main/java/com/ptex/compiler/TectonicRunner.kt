package com.ptex.compiler

import android.content.Context
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
    "libpcre2-8.so"
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

    fun test(onOutput: (String) -> Unit): Int {

        prepare()

        onOutput("Tectonic executable: ${executable.absolutePath}")
        onOutput("Exists: ${executable.exists()}")
        onOutput("Executable: ${executable.canExecute()}")
        onOutput("Size: ${executable.length()} bytes")
        onOutput("Starting Tectonic...")

        val processBuilder = ProcessBuilder(
            executable.absolutePath,
            "--version"
        )
            .directory(context.filesDir)
            .redirectErrorStream(true)

        processBuilder.environment()["LD_LIBRARY_PATH"] =
            context.filesDir.absolutePath

        val process = processBuilder.start()

        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                onOutput(line)
            }
        }

        val exitCode = process.waitFor()

        onOutput("Tectonic exited with code: $exitCode")

        return exitCode
    }
}