package com.example.cuentaconmigo.domain.model

/**
 * Fila de gasto por categoría con su participación (%) sobre el total del período.
 *
 * A diferencia de [AccountPercentage] (usada por [com.example.cuentaconmigo.domain.usecase.GetReportUseCase]
 * con un porcentaje redondeado a [Int]), acá el porcentaje se guarda como [Double] con precisión
 * completa: el informe personalizado lo muestra con un decimal (`%.1f%%`) y perdería precisión
 * silenciosamente si reusara el modelo existente.
 */
data class CategoryExpenseShare(
    val destinationAccountId: Long,
    val destinationAccountName: String,
    val total: Long,
    val percentage: Double
)
