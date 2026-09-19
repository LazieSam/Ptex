package com.ptex.document

import java.io.File

data class Project(
    val name: String,
    val rootDirectory: File,
    val mainFile: String = "main.tex"
)