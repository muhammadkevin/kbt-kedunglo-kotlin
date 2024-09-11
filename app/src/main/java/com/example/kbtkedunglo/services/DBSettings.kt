package com.example.kbtkedunglo.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.example.kbtkedunglo.model.MedalOfflineModel

class SettingsDatabase(context: Context) {

    private val databaseHelper = MedalOfflineModel(context)

    fun insert(name: String, value: String):SettingsDt? {
        try {
            val db = databaseHelper.writableDatabase
            val values = ContentValues().apply {
                put("name", name)
                put("value", value)
            }
            db.insert("settings", null, values)
            db.close()
            return getSettingByName(name)
        }catch (e:Exception){
            Log.e("KBTAPP", "ERROR INSERT DB Settings: ${e.message}")
            return null
        }
    }

    @SuppressLint("Range")
    fun getAll(): List<SettingsDt> {
        val list = mutableListOf<SettingsDt>()
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM medal_offline", null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            val value = cursor.getString(cursor.getColumnIndex("value"))
            val idx = cursor.getInt(cursor.getColumnIndex("id"))
            list.add(SettingsDt(idx, name, value))
        }
        cursor.close()
        db.close()
        return list
    }

    @SuppressLint("Range")
    fun getSettingByName(name: String): SettingsDt? {
        val db = databaseHelper.readableDatabase
        val cursor = db.query(
            "settings",
            arrayOf("id", "name", "value"),
            "name = ?",
            arrayOf(name),
            null,
            null,
            null
        )
        var setting: SettingsDt? = null
        if (cursor.moveToFirst()) {
            val idx = cursor.getInt(cursor.getColumnIndex("id"))
            val value = cursor.getString(cursor.getColumnIndex("value"))
            setting = SettingsDt(idx, name, value)
        }
        cursor.close()
        db.close()
        return setting
    }

    fun update(name: String, value: String):SettingsDt? {
        val db = databaseHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("value", value)
        }
        db.update("settings", values, "name = ?", arrayOf(name))
        db.close()
        return getSettingByName(name)
    }

    fun updateOrInsert(name: String, value: String):SettingsDt? {
        val existingSetting = getSettingByName(name)
        if (existingSetting != null) {
            return update(name, value)
        } else {
            return insert(name, value)
        }
    }

    fun delete(id: Int) {
        val db = databaseHelper.writableDatabase
        db.delete("settings", "id = ?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteByName(name: String) {
        Log.i("DELETE_LOG", "Trying to delete row with name: $name")
        val db = databaseHelper.writableDatabase
        db.delete("settings", "name = ?", arrayOf(name.toString()))
        db.close()
    }

    data class SettingsDt(val idx:Int, val name:String, val value: String)
}