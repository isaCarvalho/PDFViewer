package com.example.pdfviewer.database

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import com.example.pdfviewer.model.Link

class DatabaseController(private val context: Context)
{
    private val dbHelper = DatabaseCreator(context)

    fun insert(url: String)
    {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseCreator.FeedReaderContract.FeedEntry.URL, url)
        }

        db?.insert(DatabaseCreator.FeedReaderContract.FeedEntry.TABLE_NAME, null, values)
    }

    fun delete(id : Int)
    {
        val db = dbHelper.writableDatabase

        db.execSQL("DELETE FROM ${DatabaseCreator.FeedReaderContract.FeedEntry.TABLE_NAME} " +
            "WHERE ${BaseColumns._ID} = ?", arrayOf(id)
        )
    }

    fun list() : ArrayList<Link>
    {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseCreator.FeedReaderContract.FeedEntry.URL
        )

        val cursor = db.query(
            DatabaseCreator.FeedReaderContract.FeedEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )

        val items = ArrayList<Link>()
        with(cursor) {
            while (moveToNext())
            {
                val id = getInt(getColumnIndexOrThrow(BaseColumns._ID))
                val url = getString(getColumnIndexOrThrow(DatabaseCreator.FeedReaderContract.FeedEntry.URL))

                val link = Link(id, url)
                items.add(link)
            }
        }

        cursor.close()
        return items
    }

    fun getByUrl(url : String) : Boolean
    {
        val db = dbHelper.readableDatabase

        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseCreator.FeedReaderContract.FeedEntry.URL
        )

        val cursor = db.query(
            DatabaseCreator.FeedReaderContract.FeedEntry.TABLE_NAME,
            projection,
            "${DatabaseCreator.FeedReaderContract.FeedEntry.URL} = ?",
            arrayOf(url),
            null,
            null,
            null
        )

        var hasItems = false
        with(cursor) {
            while (moveToNext())
                hasItems = true
        }

        cursor.close()
        return hasItems
    }
}