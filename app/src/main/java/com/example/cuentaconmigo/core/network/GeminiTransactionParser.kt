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
{"type":"INCOME|EXPENSE|TRANSFER","depositAccount":"nombre","toDepositAccount":"nombre o null","destinationAccount":"nombre o null","amount":123456,"description":"texto o null"}

Tipos posibles:
- "INCOME": entra dinero a una cuenta de depósito (ej: "recibí sueldo en Bancolombia")
- "EXPENSE": sale dinero de una cuenta de depósito hacia una categoría de gasto (ej: "gasté en mercado")
- "TRANSFER": se mueve dinero entre dos cuentas de depósito (ej: "pasé plata de Bancolombia a Nequi", "transferí de efectivo a Davivienda")

Reglas por tipo:
- INCOME  → depositAccount: cuenta que recibe | toDepositAccount: null | destinationAccount: null
- EXPENSE → depositAccount: cuenta que paga   | toDepositAccount: null | destinationAccount: categoría de gasto
- TRANSFER→ depositAccount: cuenta origen     | toDepositAccount: cuenta destino | destinationAccount: null

Cuentas de depósito disponibles: ${depositAccounts.joinToString()}
Cuentas de destino/categorías disponibles: ${destinationAccounts.joinToString()}
${if (partial != null) buildPartialHint(partial) else ""}

Transcripción: $transcript
""".trimIndent()

    private fun buildPartialHint(partial: ParsedTransaction): String {
        val parts = mutableListOf<String>()
        partial.type?.let { parts.add("tipo=${it.name}") }
        partial.depositAccountName?.let { parts.add("cuentaOrigen=$it") }
        partial.toDepositAccountName?.let { parts.add("cuentaDestino=$it") }
        partial.destinationAccountName?.let { parts.add("categoría=$it") }
        partial.amount?.let { parts.add("monto=$it") }
        return if (parts.isEmpty()) "" else "Datos ya conocidos: ${parts.joinToString()}"
    }

    private fun parseJson(json: String): ParsedTransaction {
        return try {
            val clean = json.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val obj = JSONObject(clean)
            ParsedTransaction(
                type = when (obj.optString("type")) {
                    "INCOME"   -> TransactionType.INCOME
                    "EXPENSE"  -> TransactionType.EXPENSE
                    "TRANSFER" -> TransactionType.TRANSFER
                    else       -> null
                },
                depositAccountName    = obj.optString("depositAccount").ifBlank { null },
                toDepositAccountName  = obj.optString("toDepositAccount").ifBlank { null },
                destinationAccountName = obj.optString("destinationAccount").ifBlank { null },
                amount      = obj.optLong("amount").takeIf { it > 0 },
                description = obj.optString("description").ifBlank { null }
            )
        } catch (e: Exception) {
            ParsedTransaction()
        }
    }
}