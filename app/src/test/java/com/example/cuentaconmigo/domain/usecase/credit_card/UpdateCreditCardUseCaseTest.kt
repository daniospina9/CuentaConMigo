package com.example.cuentaconmigo.domain.usecase.credit_card

import com.example.cuentaconmigo.domain.model.CreditCard
import com.example.cuentaconmigo.domain.model.MinPaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class UpdateCreditCardUseCaseTest {

    private val existing = CreditCard(
        id = 7,
        userId = 3,
        name = "ITAU",
        lastFourDigits = "1234",
        creditLimit = 500_000_00L,
        interestRateAnnual = 28.5,
        cutOffDay = 15,
        paymentDueDay = 30,
        minPaymentType = MinPaymentType.PERCENTAGE,
        minPaymentPercent = 5.0,
        minPaymentFixed = 0L,
        monthlyFee = 10_000_00L,
        isActive = true
    )

    private val edits = CreditCardEdits(
        name = "RAPPI",
        lastFourDigits = "9876",
        creditLimit = 800_000_00L,
        interestRateAnnual = 30.0,
        minPaymentType = MinPaymentType.FIXED,
        minPaymentPercent = 0.0,
        minPaymentFixed = 50_000_00L,
        monthlyFee = 0L
    )

    @Test
    fun `applies editable fields`() {
        val result = UpdateCreditCardUseCase.buildUpdatedCard(existing, edits)

        assertEquals("RAPPI", result.name)
        assertEquals("9876", result.lastFourDigits)
        assertEquals(800_000_00L, result.creditLimit)
        assertEquals(30.0, result.interestRateAnnual, 0.0)
        assertEquals(MinPaymentType.FIXED, result.minPaymentType)
        assertEquals(0.0, result.minPaymentPercent, 0.0)
        assertEquals(50_000_00L, result.minPaymentFixed)
        assertEquals(0L, result.monthlyFee)
    }

    @Test
    fun `preserves id, user, cut-off day, due day and active flag`() {
        val result = UpdateCreditCardUseCase.buildUpdatedCard(existing.copy(isActive = false), edits)

        assertEquals(7L, result.id)
        assertEquals(3L, result.userId)
        assertEquals(15, result.cutOffDay)
        assertEquals(30, result.paymentDueDay)
        assertEquals(false, result.isActive)
    }

    @Test
    fun `trims the name`() {
        val result = UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(name = "  RAPPI  "))
        assertEquals("RAPPI", result.name)
    }

    @Test
    fun `blank last four digits become null`() {
        val result = UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(lastFourDigits = "  "))
        assertNull(result.lastFourDigits)
    }

    @Test
    fun `rejects blank name`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(name = "   "))
        }
    }

    @Test
    fun `rejects zero or negative limit`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(creditLimit = 0L))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(creditLimit = -1L))
        }
    }

    @Test
    fun `rejects negative rate, percent, fixed minimum and fee`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(interestRateAnnual = -0.1))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(minPaymentPercent = -1.0))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(minPaymentFixed = -1L))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(monthlyFee = -1L))
        }
    }

    @Test
    fun `rejects last four digits that are not up to four digits`() {
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(lastFourDigits = "12345"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(lastFourDigits = "12ab"))
        }
    }

    @Test
    fun `allows lowering the limit below any used amount`() {
        val result = UpdateCreditCardUseCase.buildUpdatedCard(existing, edits.copy(creditLimit = 1L))
        assertEquals(1L, result.creditLimit)
    }
}
