package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.MinPaymentType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import javax.inject.Inject

/** Editable subset of a [CreditCard]. Cut-off and due days are intentionally excluded. */
data class CreditCardEdits(
    val name: String,
    val lastFourDigits: String?,
    val creditLimit: Long,
    val interestRateAnnual: Double,
    val minPaymentType: MinPaymentType,
    val minPaymentPercent: Double,
    val minPaymentFixed: Long,
    val monthlyFee: Long
)

class UpdateCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    suspend operator fun invoke(existing: CreditCard, edits: CreditCardEdits) =
        repository.updateCard(buildUpdatedCard(existing, edits))

    companion object {
        /**
         * Validates [edits] and returns [existing] with the edited fields applied.
         * id, userId, cutOffDay, paymentDueDay and isActive are carried over untouched.
         * Lowering the limit below the used amount is allowed on purpose.
         */
        fun buildUpdatedCard(existing: CreditCard, edits: CreditCardEdits): CreditCard {
            val name = edits.name.trim()
            require(name.isNotEmpty()) { "El nombre de la tarjeta es obligatorio" }
            require(edits.creditLimit > 0) { "El límite de crédito debe ser mayor a 0" }
            require(edits.interestRateAnnual >= 0) { "La tasa no puede ser negativa" }
            require(edits.minPaymentPercent >= 0) { "El porcentaje mínimo no puede ser negativo" }
            require(edits.minPaymentFixed >= 0) { "El monto fijo mínimo no puede ser negativo" }
            require(edits.monthlyFee >= 0) { "La cuota de manejo no puede ser negativa" }
            val lastFour = edits.lastFourDigits?.trim()?.ifEmpty { null }
            require(lastFour == null || (lastFour.length <= 4 && lastFour.all { it.isDigit() })) {
                "Los últimos dígitos deben ser máximo 4 números"
            }
            return existing.copy(
                name = name,
                lastFourDigits = lastFour,
                creditLimit = edits.creditLimit,
                interestRateAnnual = edits.interestRateAnnual,
                minPaymentType = edits.minPaymentType,
                minPaymentPercent = edits.minPaymentPercent,
                minPaymentFixed = edits.minPaymentFixed,
                monthlyFee = edits.monthlyFee
            )
        }
    }
}
