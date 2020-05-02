package com.example.pdfviewer

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.pdfviewer.database.DatabaseController
import com.example.pdfviewer.fragment.PageDialogFragment
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.krishna.fileloader.FileLoader
import com.krishna.fileloader.listener.FileRequestListener
import com.krishna.fileloader.pojo.FileResponse
import com.krishna.fileloader.request.FileLoadRequest
import java.io.File
import java.util.*

class ViewActivity : AppCompatActivity() , PageDialogFragment.PageDialogListener {

    lateinit var mTextToSpeech: TextToSpeech
    lateinit var pdfView : PDFView
    var input = ""
    var fileUri: String? = null

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        pdfView = findViewById(R.id.pdf_view)

        if (intent != null)
        {
            val viewType = intent.getStringExtra("ViewType")
            fileUri = null

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
                    fileUri = url

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

        mTextToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR)
                mTextToSpeech.language = Locale.UK
        })
    }

    override fun onDialogNegativeClick(dialogFragment: DialogFragment) {
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment, nPage : Int) {
        val pdfView = findViewById<PDFView>(R.id.pdf_view)
        pdfView.jumpTo(nPage, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomRead)

        bottomNavigationView.menu.clear()
        bottomNavigationView.inflateMenu(R.menu.pages_menu)

        menuInflater.inflate(R.menu.save_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
            R.id.saveMenu -> {
                if (!fileUri.isNullOrEmpty()) {
                    DatabaseController(this@ViewActivity).insert(fileUri!!)

                    Toast.makeText(this, "Link salvo", Toast.LENGTH_SHORT).show()
                }
                else
                    Toast.makeText(this, "Não há link para salvar", Toast.LENGTH_SHORT).show()

                true
            }

            else -> true
        }
    }

    fun nextPage(v : MenuItem) {
        pdfView.jumpTo(pdfView.currentPage + 1, true)
    }

    fun previousPage(v : MenuItem) {
        pdfView.jumpTo(pdfView.currentPage - 1, true)
    }

    fun showDialog(v : MenuItem) {
        val dialog = PageDialogFragment()
        dialog.show(supportFragmentManager, "PageDialogFragment")
    }
}
