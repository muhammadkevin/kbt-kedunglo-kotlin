package com.example.kbtkedunglo
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

interface ApiResponseCallback {
    fun onSuccess(jsonObject: JSONObject)
    fun onFailure(error: String)
}

interface ApiResponseGet{
    fun onSuccess(response: String)
    fun onFailure(error: String)
}

class ApiClient {
    private val client = OkHttpClient()
    fun postData(url: String, requestBody: String, callback: ApiResponseCallback) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                try {
                    val jsonObject = JSONObject(responseData)
                    callback.onSuccess(jsonObject)
                } catch (e: Exception) {
                    callback.onFailure("Error parsing JSON: ${e.message}")
                }
            }
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback.onFailure("Error: ${e.message}")
            }
        })
    }

    fun getData(url:String, callback: ApiResponseGet){
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object: okhttp3.Callback{
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                callback.onSuccess(responseData.toString())
            }
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback.onFailure("Error: ${e.message}")
            }
        })
    }
}