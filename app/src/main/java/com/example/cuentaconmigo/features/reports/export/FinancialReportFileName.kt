package com.example.cuentaconmigo.features.reports.export

import java.time.LocalDate

/**
 * Genera el nombre de archivo `.xlsx` del informe personalizado a partir del período
 * exportado.
 *
 * Usa `LocalDate.toString()` (formato ISO-8601, `yyyy-MM-dd`) en vez de
 * `String.format`: el locale por defecto de la JVM en el dispositivo puede no ser
 * `en-US` (en `es-CO`, por ejemplo, otros formateos numéricos cambian el separador),
 * y `LocalDate.toString()` es independiente del locale.
 */
object FinancialReportFileName {

    fun forPeriod(startDate: LocalDate, endDate: LocalDate): String =
        "informe-$startDate-a-$endDate.xlsx"
}
