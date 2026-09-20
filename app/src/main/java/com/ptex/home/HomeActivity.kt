package com.ptex.home

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.ptex.MainActivity

class HomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUi()
    }

    private fun createUi() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Ptex"
            textSize = 32f
            gravity = Gravity.CENTER
        }

        val newProjectButton = Button(this).apply {
            text = "New Project"
        }

        val openProjectButton = Button(this).apply {
            text = "Open Project"
        }

        val settingsButton = Button(this).apply {
            text = "Settings"
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            newProjectButton,
            LinearLayout.LayoutParams(
                -1,
                -2
            ).apply {
                topMargin = 48
            }
        )

        root.addView(
            openProjectButton,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            settingsButton,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        setContentView(root)

        newProjectButton.setOnClickListener {
            openEditor()
        }

        openProjectButton.setOnClickListener {
            openEditor()
        }
    }

    private fun openEditor() {
        startActivity(
            Intent(this, MainActivity::class.java)
        )
    }
}
