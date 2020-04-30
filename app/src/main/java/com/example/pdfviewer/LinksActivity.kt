package com.example.pdfviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfviewer.adapter.LinkAdapter
import com.example.pdfviewer.database.DatabaseController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception

class LinksActivity : AppCompatActivity() {

    var viewManager : LinearLayoutManager? = null
    private lateinit var viewAdapter : LinkAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabSave : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        val databaseController = DatabaseController(this)

        val links = databaseController.list()

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

                        viewAdapter = LinkAdapter(DatabaseController(this).list())
                        recyclerView.adapter = viewAdapter
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
}
