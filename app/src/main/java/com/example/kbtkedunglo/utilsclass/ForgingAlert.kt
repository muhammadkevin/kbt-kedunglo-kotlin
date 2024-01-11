package com.example.kbtkedunglo.utilsclass

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun showAlert(context:Context, message:String){
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Peringatan")
    builder.setMessage(message)
    builder.setPositiveButton("OK") { dialog, which ->
        dialog.dismiss()
    }
    val alertDialog: AlertDialog = builder.create()
    alertDialog.show()
}