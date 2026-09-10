package com.example.cuentaconmigo.features.reports.export

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialReportFileNameTest {

    @Test
    fun `un periodo normal produce el nombre con ambas fechas ISO`() {
        val start = LocalDate.of(2026, 8, 1)
        val end = LocalDate.of(2026, 8, 31)

        val result = FinancialReportFileName.forPeriod(start, end)

        assertEquals("informe-2026-08-01-a-2026-08-31.xlsx", result)
    }

    @Test
    fun `un periodo de un solo dia repite la misma fecha dos veces`() {
        val day = LocalDate.of(2026, 8, 15)

        val result = FinancialReportFileName.forPeriod(day, day)

        assertEquals("informe-2026-08-15-a-2026-08-15.xlsx", result)
    }

    @Test
    fun `mes y dia de un solo digito quedan con cero a la izquierda`() {
        val start = LocalDate.of(2026, 1, 5)
        val end = LocalDate.of(2026, 1, 9)

        val result = FinancialReportFileName.forPeriod(start, end)

        assertEquals("informe-2026-01-05-a-2026-01-09.xlsx", result)
    }
}
