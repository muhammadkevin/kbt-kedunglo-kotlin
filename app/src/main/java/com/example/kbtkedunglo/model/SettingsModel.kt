package com.example.kbtkedunglo.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SettingsModel(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "medal.db"
        private const val DATABASE_VERSION = 1

        private const val CREATE_TABLE_SETTINGS = """
            CREATE TABLE settings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT,
                value TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SETTINGS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS employee")
        onCreate(db)
    }
}