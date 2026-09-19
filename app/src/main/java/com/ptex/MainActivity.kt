package com.ptex

import androidx.activity.ComponentActivity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.ptex.document.Document
import com.ptex.document.Project
import com.ptex.document.ProjectRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


import android.content.Intent
import com.ptex.debug.DebugActivity
import com.ptex.debug.DebugLog

import com.ptex.compiler.TectonicRunner

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

import com.ptex.compiler.PdfExporter

class MainActivity : ComponentActivity() {

    private lateinit var editor: EditText
    private lateinit var project: Project
    private lateinit var document: Document
    private lateinit var repository: ProjectRepository
    
    private val scope = CoroutineScope(
        Dispatchers.Main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = ProjectRepository(this)
        project = repository.getDefaultProject()
        document = repository.loadMainDocument(project)
        pdfExporter = PdfExporter(this)

        createUi()
        loadDocumentIntoEditor()
    }

    private fun createUi() {

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

        val saveButton = Button(this).apply {
            text = "Save"
        }

        val compileButton = Button(this).apply {
            text = "Compile"
        }
        val debugButton = Button(this).apply {
            text = "Debug"
        }
        val tectonicRunner = TectonicRunner(this)

        toolbar.addView(
            title,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        toolbar.addView(saveButton)
        toolbar.addView(compileButton)
        toolbar.addView(debugButton)

        editor = EditText(this).apply {
            setTypeface(Typeface.MONOSPACE)
            gravity = Gravity.TOP or Gravity.START
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
        
        debugButton.setOnClickListener {
            startActivity(
                Intent(this, DebugActivity::class.java)
            )
        }

        saveButton.setOnClickListener {
            saveDocument()
        }
    compileButton.setOnClickListener {

        DebugLog.clear()

    // Make sure the latest editor contents are compiled
        document.source = editor.text.toString()

        scope.launch {

            val result = withContext(Dispatchers.IO) {
                tectonicRunner.compile(project) { line ->
                    DebugLog.append(line)
                }
            }

            if (result.success && result.pdfFile != null) {
                exportPdf(result.pdfFile)
            } else {
                DebugLog.append(
                    "Compilation failed."
                )

                result.errorMessage?.let { error ->
                    DebugLog.append("ERROR: $error")
                }
            }
        }
    }
 
        debugButton.setOnClickListener {
            startActivity(
                Intent(this, DebugActivity::class.java)
            )
        }
    }
    
    private lateinit var pdfExporter: PdfExporter
    private var pendingPdfFile: File? = null
    
    private val savePdfLauncher =
    registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->

        if (uri == null) {
            DebugLog.append("PDF save cancelled.")
            pendingPdfFile = null
            return@registerForActivityResult
        }

        val pdfFile = pendingPdfFile
        pendingPdfFile = null

        if (pdfFile == null) {
            DebugLog.append("ERROR: No PDF waiting to be saved.")
            return@registerForActivityResult
        }

        pdfExporter.savedUri = uri

        if (pdfExporter.save(pdfFile, uri)) {
            DebugLog.append("PDF saved successfully.")
            DebugLog.append("Destination: $uri")

            Toast.makeText(
                this,
                "PDF saved",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            DebugLog.append("ERROR: Failed to save PDF.")

            Toast.makeText(
                this,
                "Failed to save PDF",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun exportPdf(pdfFile: File) {

        val uri = pdfExporter.savedUri

        if (uri == null) {
            pendingPdfFile = pdfFile
            savePdfLauncher.launch(pdfFile.name)
            return
        }

        if (pdfExporter.save(pdfFile, uri)) {
            DebugLog.append("PDF saved successfully.")
            DebugLog.append("Destination: $uri")

            Toast.makeText(
                this,
                "PDF saved",
                Toast.LENGTH_SHORT
            ).show()

        } else {
            DebugLog.append(
                "Saved PDF location is no longer available."
            )

            pdfExporter.savedUri = null
            pendingPdfFile = pdfFile

            savePdfLauncher.launch(pdfFile.name)
        }
    }

    private fun loadDocumentIntoEditor() {
        editor.setText(document.source)
    }

    private fun saveDocument() {
        document.source = editor.text.toString()

        repository.saveDocument(project, document)

        Toast.makeText(
            this,
            "Saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()

        document.source = editor.text.toString()
        repository.saveDocument(project, document)
    }
}