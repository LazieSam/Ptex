package com.ptex

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.ptex.document.Document
import com.ptex.document.DocumentRepository

import com.ptex.compiler.LatexCompiler
import com.ptex.compiler.FakeLatexCompiler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.ptex.compiler.ProcessTestCompiler

import android.content.Intent
import com.ptex.debug.DebugActivity
import com.ptex.debug.DebugLog

import com.ptex.compiler.TectonicRunner

class MainActivity : Activity() {

    private lateinit var editor: EditText
    private lateinit var document: Document
    private lateinit var repository: DocumentRepository
    
    private lateinit var compiler: LatexCompiler
    private val scope = CoroutineScope(
        Dispatchers.Main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        repository = DocumentRepository(this)
        document = repository.load()
        compiler = ProcessTestCompiler()

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

    scope.launch {

        withContext(Dispatchers.IO) {
            tectonicRunner.test { line ->
                DebugLog.append(line)
            }
        }
    }
}

        /*compileButton.setOnClickListener {

            document.source = editor.text.toString()

            DebugLog.clear()

            scope.launch {

                val result = withContext(Dispatchers.Default) {
                    compiler.compile(document) { line ->
                        DebugLog.append(line)
                    }
                }

                if (result.success) {
                    Toast.makeText(
                        this@MainActivity,
                        "Compilation successful",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }*/
 
        debugButton.setOnClickListener {
            startActivity(
                Intent(this, DebugActivity::class.java)
            )
        }
    }

    private fun loadDocumentIntoEditor() {
        editor.setText(document.source)
    }

    private fun saveDocument() {
        document.source = editor.text.toString()

        repository.save(document)

        Toast.makeText(
            this,
            "Saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()

        document.source = editor.text.toString()
        repository.save(document)
    }
}