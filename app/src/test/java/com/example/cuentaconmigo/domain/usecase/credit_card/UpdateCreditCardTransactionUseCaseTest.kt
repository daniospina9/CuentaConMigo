package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.CreditCardExtract
import com.example.cuentaconmigo.domain.model.CreditCardTransaction
import com.example.cuentaconmigo.domain.model.CreditCardTransactionType
import com.example.cuentaconmigo.domain.model.Transaction
import com.example.cuentaconmigo.domain.model.TransactionType
import com.example.cuentaconmigo.domain.repository.CreditCardRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class UpdateCreditCardTransactionUseCaseTest {

    private val cardRepo = FakeCreditCardRepository()
    private val txRepo = FakeTransactionRepository()
    private val useCase = UpdateCreditCardTransactionUseCase(cardRepo, txRepo)

    private val linkedExpense = Transaction(
        id = 50, userId = 1, depositAccountId = null, destinationAccountId = 10,
        type = TransactionType.EXPENSE, amount = 100_00L,
        date = LocalDate.of(2026, 1, 5), description = "Old description"
    )

    private fun cardTx(
        type: CreditCardTransactionType = CreditCardTransactionType.PURCHASE,
        linkedId: Long? = 50
    ) = CreditCardTransaction(
        id = 7, creditCardId = 3, userId = 1, type = type, amount = 100_00L,
        description = "Old description", date = 0L, destinationAccountId = 10,
        linkedTransactionId = linkedId
    )

    private fun run(tx: CreditCardTransaction) = runBlocking { useCase(tx) }

    // ---- characterization of existing behaviour ----

    @Test
    fun `updates the card transaction`() {
        txRepo.stored[50] = linkedExpense
        val edited = cardTx().copy(amount = 250_00L)
        run(edited)
        assertEquals(listOf(edited), cardRepo.updated)
    }

    @Test
    fun `mirrors amount to the linked transaction`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(amount = 250_00L))
        assertEquals(250_00L, txRepo.stored[50]!!.amount)
    }

    @Test
    fun `mirrors destination to the linked transaction`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(destinationAccountId = 11))
        assertEquals(11L, txRepo.stored[50]!!.destinationAccountId)
    }

    @Test
    fun `null destination keeps the linked destination`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(destinationAccountId = null))
        assertEquals(10L, txRepo.stored[50]!!.destinationAccountId)
    }

    @Test
    fun `without linked id only the card row is updated`() {
        run(cardTx(linkedId = null).copy(amount = 1L))
        assertEquals(1, cardRepo.updated.size)
        assertTrue(txRepo.updated.isEmpty())
    }

    @Test
    fun `missing linked transaction is tolerated`() {
        run(cardTx())
        assertEquals(1, cardRepo.updated.size)
        assertTrue(txRepo.updated.isEmpty())
    }

    // ---- description mirroring ----

    @Test
    fun `purchase description is mirrored to the linked expense`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(description = "New description"))
        assertEquals("New description", txRepo.stored[50]!!.description)
    }

    @Test
    fun `purchase cleared description clears the linked description`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(description = null))
        assertNull(txRepo.stored[50]!!.description)
    }

    @Test
    fun `interest description is mirrored to the linked expense`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx(CreditCardTransactionType.INTEREST).copy(description = "Interest May"))
        assertEquals("Interest May", txRepo.stored[50]!!.description)
    }

    @Test
    fun `fee description is mirrored to the linked expense`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx(CreditCardTransactionType.FEE).copy(description = "Annual fee"))
        assertEquals("Annual fee", txRepo.stored[50]!!.description)
    }

    @Test
    fun `payment keeps the linked description untouched`() {
        txRepo.stored[50] = linkedExpense.copy(description = "Pago TC: ITAU")
        run(cardTx(CreditCardTransactionType.PAYMENT).copy(description = "Something else", amount = 5_00L))
        assertEquals("Pago TC: ITAU", txRepo.stored[50]!!.description)
        assertEquals(5_00L, txRepo.stored[50]!!.amount)
    }

    @Test
    fun `other linked fields are preserved`() {
        txRepo.stored[50] = linkedExpense
        run(cardTx().copy(description = "New"))
        val result = txRepo.stored[50]!!
        assertEquals(linkedExpense.date, result.date)
        assertEquals(linkedExpense.type, result.type)
        assertEquals(linkedExpense.userId, result.userId)
    }
}

private class FakeTransactionRepository : TransactionRepository {
    val stored = mutableMapOf<Long, Transaction>()
    val updated = mutableListOf<Transaction>()

    override suspend fun getById(id: Long): Transaction? = stored[id]
    override suspend fun update(transaction: Transaction) {
        updated += transaction
        stored[transaction.id] = transaction
    }

    override fun getByUser(userId: Long): Flow<List<Transaction>> = TODO()
    override suspend fun insert(transaction: Transaction): Long = TODO()
    override suspend fun delete(transaction: Transaction) = TODO()
    override suspend fun insertTransfer(
        userId: Long, fromAccountId: Long, toAccountId: Long,
        amount: Long, date: LocalDate, description: String?
    ) = TODO()
    override suspend fun deleteTransfer(transferGroupId: String) = TODO()
    override fun getDepositAccountBalance(depositAccountId: Long): Flow<Long> = TODO()
    override fun getExpenseTotalsByDestination(
        userId: Long, startDay: Long, endDay: Long
    ): Flow<List<AccountTotal>> = TODO()
    override fun getStatementForAccount(
        depositAccountId: Long, startDay: Long, endDay: Long
    ): Flow<List<Transaction>> = TODO()
    override fun getByDestinationAccount(
        destinationAccountId: Long, startDay: Long, endDay: Long
    ): Flow<List<Transaction>> = TODO()
    override suspend fun getOpeningBalance(depositAccountId: Long, beforeDay: LocalDate): Long = TODO()
    override suspend fun getPeriodIncome(depositAccountId: Long, startDay: LocalDate, endDay: LocalDate): Long = TODO()
    override suspend fun getPeriodExpense(depositAccountId: Long, startDay: LocalDate, endDay: LocalDate): Long = TODO()
    override suspend fun getNonTransferTransactions(
        userId: Long, startDay: LocalDate, endDay: LocalDate
    ): List<Transaction> = TODO()
    override suspend fun getTotalInvestedInAccount(accountId: Long): Long = TODO()
    override fun getByDestinationAccountAll(destinationAccountId: Long): Flow<List<Transaction>> = TODO()
    override fun getByParentInvestmentAccount(
        parentAccountId: Long, startDay: Long, endDay: Long
    ): Flow<List<Transaction>> = TODO()
    override fun getTotalExpensesForAccountFlow(accountId: Long): Flow<Long> = TODO()
    override fun getAllByDepositAccount(depositAccountId: Long): Flow<List<Transaction>> = TODO()
    override suspend fun hasTransactions(accountId: Long): Boolean = TODO()
    override suspend fun deleteAllByDestinationAccount(accountId: Long) = TODO()
    override suspend fun deleteAllByAccountOrParentId(accountId: Long) = TODO()
    override fun getUserIncome(userId: Long, startDay: Long, endDay: Long): Flow<Long> = TODO()
    override fun getUserExpenses(userId: Long, startDay: Long, endDay: Long): Flow<Long> = TODO()
}

private class FakeCreditCardRepository : CreditCardRepository {
    val updated = mutableListOf<CreditCardTransaction>()

    override suspend fun updateTransaction(tx: CreditCardTransaction) {
        updated += tx
    }

    override fun getActiveCards(userId: Long): Flow<List<CreditCard>> = TODO()
    override fun getById(id: Long): Flow<CreditCard?> = TODO()
    override fun getTransactions(cardId: Long): Flow<List<CreditCardTransaction>> = TODO()
    override fun getCurrentDebt(cardId: Long): Flow<Long> = TODO()
    override suspend fun createCard(card: CreditCard): Long = TODO()
    override suspend fun updateCard(card: CreditCard) = TODO()
    override suspend fun deleteCard(card: CreditCard) = TODO()
    override suspend fun insertTransaction(tx: CreditCardTransaction): Long = TODO()
    override suspend fun deleteTransaction(tx: CreditCardTransaction) = TODO()
    override suspend fun getTransactionByLinkedId(linkedTransactionId: Long): CreditCardTransaction? = TODO()
    override suspend fun hasTransactions(cardId: Long): Boolean = TODO()
    override fun getExtracts(cardId: Long): Flow<List<CreditCardExtract>> = TODO()
    override suspend fun insertExtract(extract: CreditCardExtract): Long = TODO()
    override suspend fun updateExtract(extract: CreditCardExtract) = TODO()
    override suspend fun deleteExtract(extract: CreditCardExtract) = TODO()
    override suspend fun getTransactionsByExtractId(extractId: Long): List<CreditCardTransaction> = TODO()
    override suspend fun getExtractLinkedTransactionIds(): Set<Long> = TODO()
    override suspend fun getTcPurchaseLinkedTransactionIds(): Set<Long> = TODO()
    override suspend fun deleteTransactionsByExtractId(extractId: Long) = TODO()
}
