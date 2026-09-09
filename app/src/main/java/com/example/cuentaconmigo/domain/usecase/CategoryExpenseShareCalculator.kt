package com.example.cuentaconmigo.domain.usecase

import com.example.cuentaconmigo.domain.model.AccountTotal
import com.example.cuentaconmigo.domain.model.CategoryExpenseCalculation
import com.example.cuentaconmigo.domain.model.CategoryExpenseShare

/**
 * Calcula el total de gastos y la participación (%) de cada categoría sobre ese total.
 *
 * Objeto puro, sin dependencias de Android ni corrutinas: es testeable con JUnit puro
 * (sin Robolectric). El informe personalizado ([com.example.cuentaconmigo.features.reports.FinancialReportViewModel])
 * lo usa para no duplicar esta fórmula cuando exista la exportación del informe.
 */
object CategoryExpenseShareCalculator {

    /**
     * @param categories totales de gasto por categoría, en el orden en que deben mostrarse.
     * @return el total de gasto del período junto con cada categoría y su porcentaje sobre
     *   ese total. Si el total es cero (o la lista está vacía), el porcentaje de cada fila
     *   es `0.0` en lugar de dividir por cero.
     */
    fun calculate(categories: List<AccountTotal>): CategoryExpenseCalculation {
        val totalExpense = categories.sumOf { it.total }
        val shares = categories.map { category ->
            CategoryExpenseShare(
                destinationAccountId = category.destinationAccountId,
                destinationAccountName = category.destinationAccountName,
                total = category.total,
                percentage = if (totalExpense > 0) category.total * 100.0 / totalExpense else 0.0
            )
        }
        return CategoryExpenseCalculation(totalExpense = totalExpense, shares = shares)
    }
}
