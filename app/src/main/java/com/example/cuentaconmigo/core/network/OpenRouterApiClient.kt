package com.example.cuentaconmigo.core.network

import android.util.Log
import com.example.cuentaconmigo.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OpenRouterApiClient @Inject constructor(
    private val client: OkHttpClient,
    @Named("openrouter_api_key") private val apiKey: String
) {
    suspend fun complete(prompt: String): String? {
        val bodyJson = JSONObject().apply {
            put("model", BuildConfig.OPENROUTER_MODEL)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }.toString()

        val request = Request.Builder()
            .url("https://openrouter.ai/api/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("X-Title", "CuentaConMigo")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        val responseText = client.newCall(request).execute().use { response ->
            response.body?.string()
        } ?: return null

        Log.d("OpenRouter", "Response: $responseText")

        val json = JSONObject(responseText)
        if (json.has("error")) {
            val error = json.getJSONObject("error")
            val code = error.optInt("code")
            Log.e("OpenRouter", "API error [$code]: ${error.optString("message")}")
            if (code == 429) throw RateLimitException()
            return null
        }

        return json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
