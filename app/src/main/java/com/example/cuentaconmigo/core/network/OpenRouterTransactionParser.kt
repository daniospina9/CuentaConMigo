package com.example.cuentaconmigo.core.network

import com.example.cuentaconmigo.domain.model.ParsedTransaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.TransactionParserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenRouterTransactionParser @Inject constructor(
    private val apiClient: OpenRouterApiClient,
    private val promptBuilder: TransactionPromptBuilder
) : TransactionParserRepository {

    override suspend fun parseTranscript(
        transcript: String,
        partial: ParsedTransaction?,
        depositAccountNames: List<String>,
        destinationAccountNames: List<String>
    ): ParsedTransaction = withContext(Dispatchers.IO) {
        val prompt = promptBuilder.build(transcript, partial, depositAccountNames, destinationAccountNames)
        val content = apiClient.complete(prompt) ?: return@withContext ParsedTransaction()
        parseJson(content)
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
                depositAccountName     = obj.optString("depositAccount").ifBlank { null },
                toDepositAccountName   = obj.optString("toDepositAccount").ifBlank { null },
                destinationAccountName = obj.optString("destinationAccount").ifBlank { null },
                amount      = obj.optLong("amount").takeIf { it > 0 },
                description = obj.optString("description").ifBlank { null }
            )
        } catch (_: Exception) {
            ParsedTransaction()
        }
    }
}