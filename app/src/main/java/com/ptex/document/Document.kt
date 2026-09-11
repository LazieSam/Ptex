package com.ptex.document

data class Document(
    var source: String = "",
    var fileName: String = "main.tex",
    var isModified: Boolean = false
)