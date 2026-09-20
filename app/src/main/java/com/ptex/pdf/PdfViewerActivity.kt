package com.ptex.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.File

class PdfViewerActivity : ComponentActivity() {

    private lateinit var imageView: ImageView
    private lateinit var pageLabel: TextView

    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null

    private var currentPageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pdfPath = intent.getStringExtra("pdf_path")

        if (pdfPath == null) {
            Toast.makeText(
                this,
                "No PDF provided",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val pdfFile = File(pdfPath)

        if (!pdfFile.exists()) {
            Toast.makeText(
                this,
                "PDF not found",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        createUi()
        openPdf(pdfFile)
    }

    private fun createUi() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val previousButton = Button(this).apply {
            text = "‹"
        }

        val nextButton = Button(this).apply {
            text = "›"
        }

        pageLabel = TextView(this).apply {
            gravity = Gravity.CENTER
        }

        toolbar.addView(previousButton)
        toolbar.addView(
            pageLabel,
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )
        toolbar.addView(nextButton)

        imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                -1,
                -2
            )
        )

        root.addView(
            imageView,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        setContentView(root)

        previousButton.setOnClickListener {
            showPage(currentPageIndex - 1)
        }

        nextButton.setOnClickListener {
            showPage(currentPageIndex + 1)
        }
    }

    private fun openPdf(file: File) {
        try {
            parcelFileDescriptor =
                ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )

            pdfRenderer = PdfRenderer(
                parcelFileDescriptor!!
            )

            if (pdfRenderer!!.pageCount == 0) {
                Toast.makeText(
                    this,
                    "PDF has no pages",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return
            }

            showPage(0)

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to open PDF",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun showPage(index: Int) {

        val renderer = pdfRenderer ?: return

        if (index !in 0 until renderer.pageCount) {
            return
        }

        currentPage?.close()

        currentPage = renderer.openPage(index)
        currentPageIndex = index

        val page = currentPage!!

        val bitmap = Bitmap.createBitmap(
            page.width,
            page.height,
            Bitmap.Config.ARGB_8888
        )

        page.render(
            bitmap,
            null,
            null,
            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
        )

        imageView.setImageBitmap(bitmap)

        pageLabel.text =
            "Page ${index + 1} / ${renderer.pageCount}"
    }

    override fun onDestroy() {
        currentPage?.close()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()

        super.onDestroy()
    }
}