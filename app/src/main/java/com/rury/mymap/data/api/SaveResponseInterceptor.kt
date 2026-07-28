package com.rury.mymap.data.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream

class SaveResponseInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isSuccessful && request.url.toString().contains("maps/api/directions/json")) {
            val responseBodyCopy = response.peekBody(Long.MAX_VALUE)
            saveToFile(responseBodyCopy.string())
        }

        return response
    }

    private fun saveToFile(json: String) {
        try {
            val file = File(context.filesDir, "direction_response.json")
            FileOutputStream(file).use { it.write(json.toByteArray()) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
