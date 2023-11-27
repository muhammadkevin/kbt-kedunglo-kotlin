package com.example.kbtkedunglo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername: EditText = findViewById(R.id.editTextUsername)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidLogin(username, password)) {
                val apiclient = ApiClient()
                apiclient.postData(
                    "https://kbt.us.to/api/token/",
                    """{"username": "$username", "password": "$password"}""",
                    object :ApiResponseCallback{
                        override fun onSuccess(jsonObject: JSONObject) {
                            Log.i("KBTAPP", "Response: $jsonObject")
                            val editor = sharedPreferences.edit()
                            editor.putString("username", jsonObject.getString("username"))
                            editor.putString("password", jsonObject.getString("password"))
                            editor.putString("foto", jsonObject.getString("foto"))
                            editor.putString("group_kbt", jsonObject.getString("group_kbt"))
                            editor.putString("nama", jsonObject.getString("nama"))
                            editor.putString("julukan", jsonObject.getString("julukan"))
                            editor.putString("tgl_lahir", jsonObject.getString("tgl_lahir"))
                            editor.putString("jns_kelamin", jsonObject.getString("jns_kelamin"))
                            editor.putString("no_wa", jsonObject.getString("no_wa"))
                            editor.putString("email", jsonObject.getString("email"))
                            editor.putString("id", jsonObject.getString("id"))
                            editor.putString("access_token", jsonObject.getString("access_token"))
                            editor.putString("refresh_token", jsonObject.getString("refresh_token"))
                            editor.apply()
                            showToast("Login Berhasil")
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        override fun onFailure(error: String) {
                            Log.e("KBTPAPP","Error: $error")
                        }
                    }
                )
            } else {
                showToast("Login Gagal. Periksa username dan password.")
            }
        }
    }

    private fun isValidLogin(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}