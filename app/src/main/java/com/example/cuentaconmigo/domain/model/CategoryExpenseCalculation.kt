package com.example.cuentaconmigo.domain.model

/**
 * Resultado de calcular la participación (%) de cada categoría de gasto sobre el total
 * del período. Ver [com.example.cuentaconmigo.domain.usecase.CategoryExpenseShareCalculator].
 */
data class CategoryExpenseCalculation(
    val totalExpense: Long,
    val shares: List<CategoryExpenseShare>
)
