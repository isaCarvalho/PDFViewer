package com.example.pdfviewer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfviewer.adapter.LinkAdapter
import com.example.pdfviewer.database.DatabaseController
import com.example.pdfviewer.model.Link
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.lang.StringBuilder

class LinksActivity : AppCompatActivity() {

    companion object {
        const val READ_FILE = 1000
    }

    var viewManager : LinearLayoutManager? = null
    private lateinit var viewAdapter : LinkAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabSave : FloatingActionButton

    private lateinit var links : ArrayList<Link>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        val databaseController = DatabaseController(this)
        links = databaseController.list()

        viewManager = LinearLayoutManager(this)
        viewAdapter = LinkAdapter(links)

        recyclerView = findViewById<RecyclerView>(R.id.listLink).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        fabSave = findViewById<FloatingActionButton>(R.id.saveFab)

        fabSave.setOnClickListener {
            val url = findViewById<TextView>(R.id.saveLink).text.toString()

            if (url.isEmpty())
                Toast.makeText(this, "Por favor, insira um link", Toast.LENGTH_SHORT).show()
            else
            {
                try {
                    if (databaseController.getByUrl(url))
                        Toast.makeText(this, "Link já cadastrado!", Toast.LENGTH_SHORT).show()
                    else
                    {
                        databaseController.insert(url)

                        links = DatabaseController(this).list()
                        viewAdapter = LinkAdapter(links)
                        recyclerView.adapter = viewAdapter

                        findViewById<TextView>(R.id.saveLink).text = null
                    }
                } catch (e : Exception) {
                    Log.e("ERROR", e.message!!)
                    Toast.makeText(this, "Erro ao salvar link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.search_menu, menu)

        val searchItem = menu!!.findItem(R.id.app_bar_search)
        val searchView : SearchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewAdapter.filter.filter(newText)
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId)
        {
            R.id.exportMenu -> {
                var string = ""
                links.forEach { elem ->
                    string += "${elem.url}\n"
                }

                if (string.isEmpty())
                    Toast.makeText(this, "Não existem links para exportar", Toast.LENGTH_SHORT).show()
                else
                {
                    var success = true
                    val path = applicationContext.filesDir

                    val pdfViewerDirectory = File(path, "PDFViewer")
                    if (!pdfViewerDirectory.exists())
                        success = pdfViewerDirectory.mkdir()

                    if (success)
                    {
                        val file = File(pdfViewerDirectory, "links.txt")
                        if (!file.exists())
                            success = file.createNewFile()

                        if (success)
                            file.appendText(string).also {
                                Toast.makeText(this, "Links exportados para o armazenamento interno", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                true
            }

            R.id.importMenu -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)

                    type = "text/plain"
                }

                startActivityForResult(intent, READ_FILE)
                true
            }

            else -> true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == READ_FILE && resultCode == Activity.RESULT_OK) {
            data!!.data.also { uri ->
                val stringBuilder = StringBuilder()

                contentResolver.openInputStream(uri!!).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream!!)).use { reader ->
                        val lines = reader.readLines()

                        lines.forEach { line ->
                            DatabaseController(this@LinksActivity).insert(line)
                        }

                        links = DatabaseController(this@LinksActivity).list()
                        recyclerView.adapter = LinkAdapter(links)
                    }
                }
            }
        }
    }
}
