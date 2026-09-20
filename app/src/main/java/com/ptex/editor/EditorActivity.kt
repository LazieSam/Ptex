package com.ptex.editor

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

import com.ptex.compiler.PdfExporter
import com.ptex.compiler.TectonicRunner
import com.ptex.debug.DebugActivity
import com.ptex.debug.DebugLog
import com.ptex.document.Document
import com.ptex.document.Project
import com.ptex.document.ProjectRepository
import com.ptex.editor.EditorController

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.text.Editable
import android.text.TextWatcher

import java.io.File

import com.ptex.pdf.PdfViewerActivity

class EditorActivity : ComponentActivity() {

    private lateinit var editor: EditText
    private lateinit var project: Project
    private lateinit var repository: ProjectRepository
    private lateinit var controller: EditorController
    private lateinit var pdfExporter: PdfExporter
    private lateinit var fileList: LinearLayout
    
    private var suppressEditorChanges = false

    private val scope = CoroutineScope(
        Dispatchers.Main
    )

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
                DebugLog.append(
                    "ERROR: No PDF waiting to be saved."
                )
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
                DebugLog.append(
                    "ERROR: Failed to save PDF."
                )

                Toast.makeText(
                    this,
                    "Failed to save PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = ProjectRepository(this)

        val projectName = intent.getStringExtra(
            "project_name"
        ) ?: "default"

        project = repository.getProject(projectName)

        controller = EditorController.create(
            repository = repository,
            project = project
        )

        pdfExporter = PdfExporter(this)

        createUi()
        loadDocumentIntoEditor()
    }

    private fun createUi() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
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
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        toolbar.addView(saveButton)
        toolbar.addView(compileButton)
        toolbar.addView(debugButton)

        editor = EditText(this).apply {
            setTypeface(Typeface.MONOSPACE)

            gravity = Gravity.TOP or Gravity.START

            setPadding(16, 16, 16, 16)

            isSingleLine = false
            setHorizontallyScrolling(false)

            inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        editor.addTextChangedListener(
        object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!suppressEditorChanges) {
                    controller.activeDocument?.isModified = true
                }
            }

            override fun afterTextChanged(
                s: Editable?
            ) {
            }
        }
    )

        root.addView(toolbar)
        
        val filePanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        fileList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        filePanel.addView(
            TextView(this).apply {
            text = "PROJECT"
            textSize = 14f
        }
        )
        val newFileButton = Button(this).apply {
            text = "+ New File"
            isAllCaps = false
        }
        
        filePanel.addView(newFileButton)

        filePanel.addView(
            fileList,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        content.addView(
            filePanel,
            LinearLayout.LayoutParams(
                220,
                -1
            )
        )

        content.addView(
            editor,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )

        root.addView(
            content,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)
        refreshFileList()
        
        newFileButton.setOnClickListener {
            showNewFileDialog()
        }

        debugButton.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    DebugActivity::class.java
                )
            )
        }

        saveButton.setOnClickListener {
            saveDocument()
        }

        compileButton.setOnClickListener {

            DebugLog.clear()

            controller.activeDocument?.source = editor.text.toString()

            scope.launch {

                val result = withContext(Dispatchers.IO) {
                    tectonicRunner.compile(project) { line ->
                        DebugLog.append(line)
                    }
                }

                if (result.success && result.pdfFile != null) {

                    openPdfViewer(result.pdfFile)

                } else {

                    DebugLog.append(
                        "Compilation failed."
                    )

                    result.errorMessage?.let { error ->
                        DebugLog.append(
                            "ERROR: $error"
                        )
                    }
                }
            }
        }
    }
    
    private fun showNewFileDialog() {

        val input = EditText(this).apply {
            hint = "chapter1.tex"
            setSingleLine(true)
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("New File")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Create") { _, _ ->

                var fileName = input.text
                    .toString()
                    .trim()

                if (fileName.isBlank()) {
                    return@setPositiveButton
                }

                if (!fileName.endsWith(".tex")) {
                    fileName += ".tex"
                }

                try {
                    controller.createDocument(fileName)

                    loadDocumentIntoEditor()
                    refreshFileList()

                } catch (e: Exception) {

                    Toast.makeText(
                        this,
                        e.message ?: "Failed to create file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        .show()
    }
    
    private fun refreshFileList() {

        fileList.removeAllViews()

        val files = repository.listFiles(project)

        for (file in files) {

            val fileName = file.relativeTo(
                project.rootDirectory
            ).path

            val button = Button(this).apply {
                text = fileName
                gravity = Gravity.START
            }

            button.setOnClickListener {
                controller.activeDocument?.let { currentDocument ->
                    currentDocument.source = editor.text.toString()
                }

                val document = controller.selectDocument(fileName)
                
               /* Toast.makeText(
                    this,
                    "Selected: ${document?.fileName}",
                    Toast.LENGTH_SHORT
                ).show()*/
    
                
                if (document != null) {

                    suppressEditorChanges = true

                    editor.setText(document.source)

                    suppressEditorChanges = false

                    refreshFileList()
                }
            }

            fileList.addView(button)
        }
    }

    private fun exportPdf(pdfFile: File) {

        val uri = pdfExporter.savedUri

        if (uri == null) {

            pendingPdfFile = pdfFile

            savePdfLauncher.launch(
                pdfFile.name
            )

            return
        }

        if (pdfExporter.save(pdfFile, uri)) {

            DebugLog.append(
                "PDF saved successfully."
            )

            DebugLog.append(
                "Destination: $uri"
            )

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

            savePdfLauncher.launch(
                pdfFile.name
            )
        }
    }

    private fun loadDocumentIntoEditor() {

        suppressEditorChanges = true

        editor.setText(
            controller.activeDocument?.source ?: ""
        )

        suppressEditorChanges = false
    }

    private fun saveDocument() {

        val document = controller.activeDocument
            ?: return

        document.source =
            editor.text.toString()

        repository.saveDocument(
            project,
            document
        )

        Toast.makeText(
            this,
            "Saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()

        controller.activeDocument?.let { document ->

            document.source = editor.text.toString()

            repository.saveDocument(
                project,
                document
            )
        }
    }
    private fun openPdfViewer(pdfFile: File) {
    val intent = Intent(
        this,
        PdfViewerActivity::class.java
    )

    intent.putExtra(
        "pdf_path",
        pdfFile.absolutePath
    )

    startActivity(intent)
    }
}