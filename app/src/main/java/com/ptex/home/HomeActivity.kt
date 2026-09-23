package com.ptex.home

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.ptex.editor.EditorActivity
import com.ptex.settings.SettingsActivity

import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast
import com.ptex.document.ProjectRepository
import com.ptex.document.Project

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
            showNewProjectDialog()
        }

        openProjectButton.setOnClickListener {
            showOpenProjectDialog()
        }
        settingsButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }
    }

    private fun openEditor(project: Project) {

    val intent = Intent(
        this,
        EditorActivity::class.java
    )

    intent.putExtra(
        "project_name",
        project.name
    )

    startActivity(intent)
    }
    
    private fun showNewProjectDialog() {

    val input = EditText(this).apply {
        hint = "My Project"
        setSingleLine(true)
    }

    AlertDialog.Builder(this)
        .setTitle("New Project")
        .setView(input)
        .setNegativeButton("Cancel", null)
        .setPositiveButton("Create") { _, _ ->

            val name = input.text
                .toString()
                .trim()

            if (name.isBlank()) {
                Toast.makeText(
                    this,
                    "Project name cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()

                return@setPositiveButton
            }

            try {
                val repository = ProjectRepository(this)

                val project = repository.createProject(name)

                openEditor(project)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    e.message ?: "Failed to create project",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        .show()
    }
    private fun showOpenProjectDialog() {
    val repository = ProjectRepository(this)
    val projects = repository.listProjects()

    if (projects.isEmpty()) {
        Toast.makeText(
            this,
            "No projects found",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val projectNames = projects
        .map { it.name }
        .toTypedArray()

    AlertDialog.Builder(this)
        .setTitle("Open Project")
        .setItems(projectNames) { _, which ->
            openEditor(projects[which])
        }
        .setNegativeButton("Cancel", null)
        .show()
    }
}
