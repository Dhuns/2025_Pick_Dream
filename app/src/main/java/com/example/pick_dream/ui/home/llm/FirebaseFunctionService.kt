package com.example.pick_dream.ui.home.llm

import android.os.Handler
import android.os.Looper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object FirebaseFunctionService {
    private val client = OkHttpClient()
    private val mainHandler = Handler(Looper.getMainLooper())  //UI 스레드용 핸들러

    private const val FUNCTION_URL =
        "https://ai-assistant-yaluz6ij5a-uc.a.run.app"

    fun sendMessageToFunction(
        message: String,
        idToken: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val json = JSONObject().apply { put("message", message) }
        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(FUNCTION_URL)
            .addHeader("Authorization", "Bearer $idToken")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                //실패도 UI 스레드로 전달
                mainHandler.post {
                    onFailure(e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val result = it.body?.string()
                    if (result != null) {
                        //UI 스레드에서 콜백 실행
                        mainHandler.post {
                            onSuccess(result)
                        }
                    } else {
                        mainHandler.post {
                            onFailure(IOException("응답이 없습니다."))
                        }
                    }
                }
            }
        })
    }
}
