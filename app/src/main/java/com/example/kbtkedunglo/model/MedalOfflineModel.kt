package com.example.kbtkedunglo.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MedalOfflineModel(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "medal.db"
        private const val DATABASE_VERSION = 1

        private const val CREATE_TABLE_LIST_MEDAL_OFFLINE = """
            CREATE TABLE medal_offline (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_id INTEGER,
                event_nama TEXT,
                event_file TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_LIST_MEDAL_OFFLINE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS employee")
        onCreate(db)
    }
}