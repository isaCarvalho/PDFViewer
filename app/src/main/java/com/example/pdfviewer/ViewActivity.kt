package com.example.pdfviewer

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
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
import android.view.MenuItem

class ViewActivity : AppCompatActivity() , PageDialogFragment.PageDialogListener {

    private lateinit var pdfView : PDFView
    private lateinit var bottomNavigationView : BottomNavigationView
    private var fileUri: String? = null

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        pdfView = findViewById(R.id.pdf_view)

        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        if (intent != null)
        {
            val viewType = intent.getStringExtra("ViewType")
            fileUri = null

            if (!TextUtils.isEmpty(viewType) || viewType != null)
            {
                if (viewType!! == "storage")
                {
                    val selectedPdf = Uri.parse(intent.getStringExtra("FileUri"))

                    getConfiguration(pdfView, pdfView.fromUri(selectedPdf))
                }
                else if (viewType == "internet")
                {
                    val url = intent.getStringExtra("url")
                    fileUri = url

                    try {
                        FileLoader.with(this)
                            .load(url, false)
                            .fromDirectory("PDFFile", FileLoader.DIR_INTERNAL)
                            .asFile(object : FileRequestListener<File> {
                                override fun onLoad(p0: FileLoadRequest?, p1: FileResponse<File>?) {

                                    val pdfFile = p1!!.body
                                    getConfiguration(pdfView, pdfView.fromFile(pdfFile))
                                }

                                override fun onError(p0: FileLoadRequest?, p1: Throwable) {
                                    Toast.makeText(this@ViewActivity, p1.message, Toast.LENGTH_SHORT).show()
                                }
                            })
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

                progressBar.visibility = View.GONE
            }
        }
    }

    private fun getConfiguration(pdfView : PDFView, pdfViewConfigurator: PDFView.Configurator)
    {
        pdfViewConfigurator.
            password(null)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(true)
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

    override fun onDialogNegativeClick(dialogFragment: DialogFragment) {
    }

    override fun onDialogPositiveClick(dialogFragment: DialogFragment, nPage : Int) {
        val pdfView = findViewById<PDFView>(R.id.pdf_view)
        pdfView.jumpTo(nPage, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        bottomNavigationView = findViewById(R.id.bottomRead)

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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus)
            hideSystemUI()
        else
            showSystemUI()
    }

    private fun hideSystemUI()
    {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI()
    {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
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
