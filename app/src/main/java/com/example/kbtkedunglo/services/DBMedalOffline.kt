package com.example.kbtkedunglo.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.kbtkedunglo.model.MedalOfflineModel

class MedalOfflineDatabase(context: Context) {

    private val databaseHelper = MedalOfflineModel(context)

    fun insert(eventId:Int?, eventNama: String, eventFile: String) {
        try {
            val db = databaseHelper.writableDatabase
            val values = ContentValues().apply {
                put("event_id", eventId)
                put("event_nama", eventNama)
                put("event_file", eventFile)
            }
            db.insert("medal_offline", null, values)
            db.close()
        }catch (e:Exception){
            Log.e("KBTAPP", "ERROR INSERT DB Medal Offline: ${e.message}")
        }
    }

    @SuppressLint("Range")
    fun getAll(): List<MedalOffline> {
        val list = mutableListOf<MedalOffline>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM medal_offline", null)
        while (cursor.moveToNext()) {
            val eventNama = cursor.getString(cursor.getColumnIndex("event_nama"))
            val eventId = cursor.getString(cursor.getColumnIndex("event_id"))
            val eventFile = cursor.getString(cursor.getColumnIndex("event_file"))
            val idx = cursor.getInt(cursor.getColumnIndex("id"))
            list.add(MedalOffline(idx, eventId, eventNama, eventFile))
        }

        // close the cursor and database connection
        cursor.close()
        db.close()

        return list
    }

    fun update(id: Int, eventId: Int, eventNama: String, eventFile: String) {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("event_id", eventId)
            put("event_nama", eventNama)
            put("event_file", eventFile)
        }

        db.update("medal_offline", values, "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun delete(id: Int) {
        val db = databaseHelper.writableDatabase
        db.delete("medal_offline", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteByEvent(id: Int) {
        Log.i("DELETE_LOG", "Trying to delete row with id_event: $id")
        val db = databaseHelper.writableDatabase
        db.delete("medal_offline", "event_id = ?", arrayOf(id.toString()))
        db.close()
    }

data class MedalOffline(val idx:Int, val eventId:String, val eventNama: String, val eventFile: String)
}