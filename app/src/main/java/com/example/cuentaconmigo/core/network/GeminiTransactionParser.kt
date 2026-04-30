package com.example.cuentaconmigo.core.network

import com.example.cuentaconmigo.domain.model.ParsedTransaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.google.ai.client.generativeai.GenerativeModel
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiTransactionParser @Inject constructor(
    private val model: GenerativeModel
) {
    suspend fun parseTranscript(
        transcript: String,
        partial: ParsedTransaction? = null,
        depositAccountNames: List<String>,
        destinationAccountNames: List<String>
    ): ParsedTransaction {
        val prompt = buildPrompt(transcript, partial, depositAccountNames, destinationAccountNames)
        val response = model.generateContent(prompt)
        val text = response.text ?: return ParsedTransaction()
        return parseJson(text)
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
            val clean = json.trim().removePrefix("```json").removeSuffix("```").trim()
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
