package com.ptex

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "Ptex"
            textSize = 20f
            setPadding(24, 16, 0, 16)
        }

        val compileButton = Button(this).apply {
            text = "Compile"
        }

        toolbar.addView(
            title,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        toolbar.addView(compileButton)

        val editor = EditText(this).apply {
            setTypeface(Typeface.MONOSPACE)
            gravity = Gravity.TOP or Gravity.START
            setText(
                "\\documentclass{article}\n\n" +
                "\\begin{document}\n\n" +
                "Hello, Ptex!\n\n" +
                "\\end{document}"
            )
            setPadding(16, 16, 16, 16)
            isSingleLine = false
            setHorizontallyScrolling(true)
        }

        root.addView(toolbar)

        root.addView(
            editor,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        compileButton.setOnClickListener {
            // Tectonic compilation will go here.
        }
    }
}
