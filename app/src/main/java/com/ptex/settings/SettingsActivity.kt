package com.ptex.settings

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.ptex.debug.DebugActivity
import android.widget.Toast

import android.app.AlertDialog
import com.ptex.document.ProjectRepository

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUi()
    }

private fun createUi() {
    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(24), dp(24), dp(24), dp(24))
    }

    val title = TextView(this).apply {
        text = "Settings"
        textSize = 32f
        gravity = Gravity.CENTER
    }

    root.addView(
        title,
        LinearLayout.LayoutParams(
            -1,
            -2
        ).apply {
            bottomMargin = dp(32)
        }
    )

    val clearCacheButton = createSettingsButton("Clear PDF Cache")
    val editorButton = createSettingsButton("Editor Settings")
    val debugButton = createSettingsButton("Debug")
    val aboutButton = createSettingsButton("About Ptex")

    root.addView(clearCacheButton)
    root.addView(editorButton)
    root.addView(debugButton)
    root.addView(aboutButton)

    debugButton.setOnClickListener {
        startActivity(
            Intent(
                this,
                DebugActivity::class.java
            )
        )
    }

    clearCacheButton.setOnClickListener {
        clearCacheButton.setOnClickListener {
            confirmClearPdfCache()
        }
    }

    editorButton.setOnClickListener {
        Toast.makeText(
            this,
            "Editor settings coming soon",
            Toast.LENGTH_SHORT
        ).show()
    }

    aboutButton.setOnClickListener {
        Toast.makeText(
            this,
            "Ptex 0.8.1",
            Toast.LENGTH_SHORT
        ).show()
    }

    setContentView(root)
}
private fun createSettingsButton(
    text: String
): Button {
    return Button(this).apply {
        this.text = text
        isAllCaps = false

        layoutParams = LinearLayout.LayoutParams(
            dp(320),
            dp(64)
        ).apply {
            gravity = Gravity.CENTER
            bottomMargin = dp(12)
        }
    }
}
private fun dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
}
private fun confirmClearPdfCache() {
    AlertDialog.Builder(this)
        .setTitle("Clear PDF Cache")
        .setMessage(
            "This will delete all generated PDF previews. " +
                "Your .tex files and projects will not be affected."
        )
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Clear") { _, _ ->

            val repository = ProjectRepository(this)
            val deletedCount = repository.clearPdfCache()

            Toast.makeText(
                this,
                "$deletedCount PDF preview(s) cleared",
                Toast.LENGTH_SHORT
            ).show()
        }
        .show()
}
}