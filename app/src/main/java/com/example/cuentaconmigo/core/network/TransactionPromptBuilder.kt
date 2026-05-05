package com.example.cuentaconmigo.core.network

import com.example.cuentaconmigo.domain.model.ParsedTransaction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionPromptBuilder @Inject constructor() {

    fun build(
        transcript: String,
        partial: ParsedTransaction?,
        depositAccountNames: List<String>,
        destinationAccountNames: List<String>
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

Cuentas de depósito disponibles: ${depositAccountNames.joinToString()}
Cuentas de destino/categorías disponibles: ${destinationAccountNames.joinToString()}
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
}