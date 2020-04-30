package com.example.pdfviewer.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DatabaseCreator(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    companion object
    {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "PDFViewer.db"
    }

    object FeedReaderContract
    {
        object FeedEntry : BaseColumns
        {
            const val TABLE_NAME = "links"
            const val URL = "url"
        }
    }

    override fun onCreate(db: SQLiteDatabase?)
    {
        val string = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY NOT NULL, " +
                "${FeedReaderContract.FeedEntry.URL} TEXT NOT NULL UNIQUE )"

        db!!.execSQL(string)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME}")
        onCreate(db)
    }
}