package com.example.pdfviewer

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PICK_PDF_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : BaseMultiplePermissionsListener() {

            })

        val btnViewStorage = findViewById<Button>(R.id.pdf_view_storage)
        btnViewStorage.setOnClickListener {

            val pdfIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            startActivityForResult(Intent.createChooser(pdfIntent, "Select PDF"), PICK_PDF_CODE)
        }

        val btnViewInternet = findViewById<Button>(R.id.pdf_view_internet)
        btnViewInternet.setOnClickListener {

            val link = findViewById<EditText>(R.id.link).text.toString()
            if (link.isEmpty())
            {
                val intent = Intent(this@MainActivity, LinksActivity::class.java)

                startActivity(intent)
            }
            else
            {
                val intent = Intent(this@MainActivity, ViewActivity::class.java).apply {
                    putExtra("ViewType", "internet")
                    putExtra("url", link)
                }

                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode : Int, data : Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_CODE && resultCode == Activity.RESULT_OK && data != null)
        {
            val selectedPDF = data.data

            val intent = Intent(this@MainActivity, ViewActivity::class.java).apply {
                putExtra("ViewType", "storage")
                putExtra("FileUri", selectedPDF.toString())
            }

            startActivity(intent)
        }
    }
}
