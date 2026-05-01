package com.example.cuentaconmigo.core.network

import android.util.Log
import com.example.cuentaconmigo.BuildConfig
import com.example.cuentaconmigo.domain.model.ParsedTransaction
import com.example.cuentaconmigo.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class RateLimitException : Exception("rate_limited")

@Singleton
class GeminiTransactionParser @Inject constructor(
    private val client: OkHttpClient,
    @Named("openrouter_api_key") private val apiKey: String
) {
    suspend fun parseTranscript(
        transcript: String,
        partial: ParsedTransaction? = null,
        depositAccountNames: List<String>,
        destinationAccountNames: List<String>
    ): ParsedTransaction = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(transcript, partial, depositAccountNames, destinationAccountNames)

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
            response.body?.string() ?: return@withContext ParsedTransaction()
        }

        Log.d("OpenRouter", "Response: $responseText")

        val json = JSONObject(responseText)
        if (json.has("error")) {
            val error = json.getJSONObject("error")
            val code = error.optInt("code")
            Log.e("OpenRouter", "API error [$code]: ${error.optString("message")}")
            if (code == 429) throw RateLimitException()
            return@withContext ParsedTransaction()
        }

        val content = json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        parseJson(content)
    }

    private fun buildPrompt(
        transcript: String,
        partial: ParsedTransaction?,
        depositAccounts: List<String>,
        destinationAccounts: List<String>
    ): String = """
Eres un asistente de finanzas personales colombiano. Extrae los datos de la transacción del texto de voz.
Responde ÚNICAMENTE con JSON válido usando exactamente este formato:
{"type":"INCOME|EXPENSE","depositAccount":"nombre","destinationAccount":"nombre o null","amount":123456,"description":"texto o null"}

Reglas:
- type: "INCOME" si el dinero entra, "EXPENSE" si sale
- depositAccount: nombre de la cuenta bancaria o efectivo (debe coincidir con una de la lista)
- destinationAccount: categoría donde va el gasto (solo para EXPENSE, null para INCOME)
- amount: número entero positivo en pesos colombianos
- description: nota breve opcional

Cuentas de depósito disponibles: ${depositAccounts.joinToString()}
Cuentas de destino disponibles: ${destinationAccounts.joinToString()}
${if (partial != null) "Datos ya conocidos: tipo=${partial.type}, cuenta=${partial.depositAccountName}, monto=${partial.amount}" else ""}

Transcripción: $transcript
""".trimIndent()

    private fun parseJson(json: String): ParsedTransaction {
        return try {
            val clean = json.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val obj = JSONObject(clean)
            ParsedTransaction(
                type = when (obj.optString("type")) {
                    "INCOME"  -> TransactionType.INCOME
                    "EXPENSE" -> TransactionType.EXPENSE
                    else      -> null
                },
                depositAccountName = obj.optString("depositAccount").ifBlank { null },
                destinationAccountName = obj.optString("destinationAccount").ifBlank { null },
                amount = obj.optLong("amount").takeIf { it > 0 },
                description = obj.optString("description").ifBlank { null }
            )
        } catch (e: Exception) {
            ParsedTransaction()
        }
    }
}