package com.example.kbtkedunglo.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ProfilModel(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "medal.db"
        private const val CREATE_TABLE_SETTINGS = """
            CREATE TABLE profil (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT,
                password TEXT,
                foto TEXT,
                group_kbt TEXT,
                nama TEXT,
                julukan TEXT,
                tgl_lahir TEXT,
                jns_kelamin TEXT,
                no_wa TEXT,
                email TEXT,
                access_token TEXT,
                refresh_token TEXT
            )
        """
        private const val DATABASE_VERSION = 1

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SETTINGS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS employee")
        onCreate(db)
    }
}