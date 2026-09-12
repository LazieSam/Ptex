package com.ptex.debug

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DebugActivity : Activity() {

    private lateinit var outputView: TextView
    private lateinit var scrollView: ScrollView

    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        outputView = TextView(this).apply {
            textSize = 14f
            typeface = Typeface.MONOSPACE
            setPadding(16, 16, 16, 16)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
        }

        scrollView = ScrollView(this).apply {
            addView(outputView)
        }

        setContentView(scrollView)

        scope.launch {
            DebugLog.text.collectLatest { text ->

                outputView.text = text

                scrollView.post {
                    scrollView.fullScroll(
                        ScrollView.FOCUS_DOWN
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}