package com.example.pdfviewer

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.krishna.fileloader.FileLoader
import com.krishna.fileloader.listener.FileRequestListener
import com.krishna.fileloader.pojo.FileResponse
import com.krishna.fileloader.request.FileLoadRequest
import java.io.File

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        if (intent != null) {
            val viewType = intent.getStringExtra("ViewType")
            val pdfView = findViewById<com.github.barteksc.pdfviewer.PDFView>(R.id.pdf_view)

            if (!TextUtils.isEmpty(viewType) || viewType != null)
            {
                if (viewType!! == "storage")
                {
                    val selectedPdf = Uri.parse(intent.getStringExtra("FileUri"))

                    pdfView.fromUri(selectedPdf)
                        .password(null)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                            Log.d("onDraw", "$canvas $pageHeight $pageWidth $displayedPage")
                        }.onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                            Log.d("onDrawAll", "$canvas $pageHeight $pageWidth $displayedPage")
                        }
                        .onPageChange{ page, pageCount ->
                            Log.d("onPageChange", "$page $pageCount")
                        }
                        .onPageError {page, t->
                            Toast.makeText(this@ViewActivity, "Error while opening page $page", Toast.LENGTH_SHORT).show()
                            Log.d("ERROR", "" + t.localizedMessage)
                        }
                        .onTap{ false }
                        .onRender { nbPages, pageWidth, pageHeight ->
                            Log.d("onRender", "$nbPages $pageWidth $pageHeight")
                            pdfView.fitToWidth()
                        }
                        .enableAnnotationRendering(true)
                        .invalidPageColor(Color.RED)
                        .load()
                }
                else if (viewType == "internet")
                {
                    val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
                    progressBar.visibility = View.VISIBLE

                    val url = intent.getStringExtra("url")

                    try {
                        FileLoader.with(this)
                            .load(url, false)
                            .fromDirectory("PDFFile", FileLoader.DIR_INTERNAL)
                            .asFile(object : FileRequestListener<File> {
                                override fun onLoad(p0: FileLoadRequest?, p1: FileResponse<File>?) {

                                    val pdfFile = p1!!.body

                                    pdfView.fromFile(pdfFile)
                                        .password(null)
                                        .defaultPage(0)
                                        .enableSwipe(true)
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .onDraw { canvas, pageWidth, pageHeight, displayedPage ->
                                            Log.d(
                                                "onDraw",
                                                "$canvas $pageHeight $pageWidth $displayedPage"
                                            )
                                        }
                                        .onDrawAll { canvas, pageWidth, pageHeight, displayedPage ->
                                            Log.d(
                                                "onDrawAll",
                                                "$canvas $pageHeight $pageWidth $displayedPage"
                                            )
                                        }
                                        .onPageChange { page, pageCount ->
                                            Log.d("onPageChange", "$page $pageCount")
                                        }
                                        .onPageError { page, t ->
                                            Toast.makeText(
                                                this@ViewActivity,
                                                "Error while opening page $page",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.d("ERROR", "" + t.localizedMessage)
                                        }
                                        .onTap { false }
                                        .onRender { nbPages, pageWidth, pageHeight ->
                                            Log.d("onRender", "$nbPages $pageWidth $pageHeight")
                                            pdfView.fitToWidth()
                                        }
                                        .enableAnnotationRendering(true)
                                        .invalidPageColor(Color.RED)
                                        .load()
                                }

                                override fun onError(p0: FileLoadRequest?, p1: Throwable) {
                                    Toast.makeText(
                                        this@ViewActivity,
                                        "" + p1.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@ViewActivity,
                            "" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        progressBar.visibility = View.GONE
                    }
                }

            }
        }
    }
}
