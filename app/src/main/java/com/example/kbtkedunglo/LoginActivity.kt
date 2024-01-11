package com.example.kbtkedunglo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kbtkedunglo.utilsclass.ApiClient
import com.example.kbtkedunglo.utilsclass.ApiResponseCallback
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private var showPass:Boolean = false
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextUsername: EditText = findViewById(R.id.editTextUsernameLogin)
        val editTextPassword: EditText = findViewById(R.id.editTextPasswordLogin)
        val errorPassword: TextView = findViewById(R.id.errorMessagePasswordLogin)
        val errorUsername: TextView = findViewById(R.id.errorMessageUsernameLogin)
        val btShowPass:ImageView = findViewById(R.id.eyeIconLogin)
        val btnLogin: Button = findViewById(R.id.btnSubmitLogin)

        btShowPass.setOnClickListener{
            if(showPass){
                btShowPass.setImageResource(R.drawable.ic_eye)
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                showPass = false
            }else{
                btShowPass.setImageResource(R.drawable.ic_eye_slash)
                editTextPassword.transformationMethod = SingleLineTransformationMethod.getInstance()
                showPass = true
            }
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidLogin(username, password)) {
                val apiclient = ApiClient()
                apiclient.postData(
                    "https://kbt.us.to/api/token/",
                    """{"username": "$username", "password": "$password"}""",
                    object : ApiResponseCallback {
                        override fun onSuccess(jsonObject: JSONObject, codeRes:Int) {
                            Log.i("KBTAPP", "Response: ${codeRes}")
                            if(codeRes == 200){
                                saveDtAndRedirect(jsonObject)
                            }else{
                                if(jsonObject.getString("detail") == "Incorrect password."){
                                    runOnUiThread {
                                        errorPassword.text = "password yang anda masukkan salah"
                                        errorUsername.text = ""
                                        errorUsername.layoutParams.height = 0
                                        btnLogin.isEnabled = true
                                    }
                                }else if(jsonObject.getString("detail") == "User not found."){
                                    runOnUiThread{
                                        errorUsername.text = "username tidak terdaftar"
                                        errorPassword.text = ""
                                        errorUsername.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                        btnLogin.isEnabled = true
                                    }
                                }
                            }
                        }
                        override fun onFailure(error: String) {
                            Log.e("KBTPAPP","Error: $error")
                        }
                    }
                )
            } else if(username.isEmpty()) {
                errorUsername.text = "username tidak boleh kosong"
                errorUsername.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                errorPassword.text = ""
                showToast("username tidak boleh kosong")
            }else {
                errorPassword.text = "password tidak boleh kosong"
                errorUsername.text = ""
                errorUsername.layoutParams.height = 0
                showToast("username tidak boleh kosong")
            }
            btnLogin.isEnabled = true
        }

        editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val enteredText = s.toString()
                if(enteredText == "") {
                    errorUsername.text = "username tidak boleh kosong"
                    errorUsername.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                }else {
                    errorUsername.layoutParams.height = 0
                    errorUsername.text = ""
                }
            }
        })
    }

    private fun saveDtAndRedirect(jsonObject: JSONObject){
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

    private fun isValidLogin(username: String, password: String): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}