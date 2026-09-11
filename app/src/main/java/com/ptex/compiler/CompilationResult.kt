package com.ptex.compiler

data class CompilationResult(
    val success: Boolean,
    val pdfFile: java.io.File? = null,
    val errorMessage: String? = null
)