package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType

/**
 * Single source of truth for how the description of the expense linked to a credit card
 * transaction relates to the card transaction itself.
 */

/** Description given to a card payment (and its linked expense) at registration time. */
fun paymentDescription(cardName: String): String = "Pago TC: $cardName"

/**
 * Description the linked expense must have after [tx] is edited.
 *
 * PURCHASE / INTEREST / FEE: the linked expense is created with the card transaction's
 * description, so it follows it. PAYMENT: the linked expense description is built from the
 * card name ([paymentDescription]) and is not editable, so [currentLinkedDescription] is kept.
 */
fun linkedExpenseDescription(tx: CreditCardTransaction, currentLinkedDescription: String?): String? =
    when (tx.type) {
        CreditCardTransactionType.PAYMENT -> currentLinkedDescription
        CreditCardTransactionType.PURCHASE,
        CreditCardTransactionType.INTEREST,
        CreditCardTransactionType.FEE -> tx.description
    }
