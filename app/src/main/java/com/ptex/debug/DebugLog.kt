package com.ptex.debug

import kotlinx.coroutines.flow.MutableStateFlow

object DebugLog {

    val text = MutableStateFlow("")

    fun append(line: String) {
        text.value += line + "\n"
    }

    fun clear() {
        text.value = ""
    }
}