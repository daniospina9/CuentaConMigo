package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class RegisterPaymentUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepository,
    private val transactionRepository: TransactionRepository
) {
    /**
     * Registers a credit card payment:
     * 1. Creates a TransactionEntity EXPENSE to deduct from the deposit account.
     * 2. Creates a CreditCardTransactionEntity PAYMENT linked to that expense.
     *
     * @param creditCardId   ID of the credit card being paid
     * @param cardName       Name used in the expense description
     * @param userId         Owner user
     * @param amount         Payment amount in centavos (always positive)
     * @param depositAccountId Account from which the payment is debited
     * @param date           Payment date as epoch millis
     */
    suspend operator fun invoke(
        creditCardId: Long,
        cardName: String,
        userId: Long,
        amount: Long,
        depositAccountId: Long,
        date: Long
    ) {
        val localDate = Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val expenseId = transactionRepository.insert(
            Transaction(
                id = 0,
                userId = userId,
                depositAccountId = depositAccountId,
                destinationAccountId = null,
                type = TransactionType.EXPENSE,
                amount = amount,
                date = localDate,
                description = "Pago TC: $cardName",
                transferGroupId = null
            )
        )

        creditCardRepository.insertTransaction(
            CreditCardTransaction(
                id = 0,
                creditCardId = creditCardId,
                userId = userId,
                type = CreditCardTransactionType.PAYMENT,
                amount = amount,
                description = "Pago TC: $cardName",
                date = date,
                destinationAccountId = null,
                linkedTransactionId = expenseId
            )
        )
    }
}
